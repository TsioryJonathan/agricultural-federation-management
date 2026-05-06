package com.hei.agriculturalfederationmanagement.repository;

import com.hei.agriculturalfederationmanagement.entity.dto.CollectivityLocalStatistics;
import com.hei.agriculturalfederationmanagement.entity.dto.CollectivityOverallStatistics;
import com.hei.agriculturalfederationmanagement.entity.dto.CollectivityInformation;
import com.hei.agriculturalfederationmanagement.entity.dto.MemberDescription;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@AllArgsConstructor
public class StatisticsRepository {
    private final Connection connection;

    /**
     * Get local statistics for a collectivity:
     * - Earned amount per member
     * - Unpaid amount per member (based on active cotisations)
     */
    public List<CollectivityLocalStatistics> getLocalStatistics(String collectivityId, Instant from, Instant to) {
        String sql = """
            WITH active_cotisations AS (
                SELECT id, amount, frequency, eligible_from
                FROM cotisation_plan
                WHERE id_collectivity = ?
                AND status = 'ACTIVE'
            ),
            member_payments AS (
                SELECT 
                    t.id_member,
                    COALESCE(SUM(t.amount), 0) as earned_amount
                FROM transaction t
                WHERE t.id_collectivity = ?
                AND t.transaction_type = 'IN'
                AND t.transaction_date >= ?
                AND t.transaction_date <= ?
                GROUP BY t.id_member
            ),
            collectivity_members AS (
                SELECT 
                    m.id,
                    m.first_name,
                    m.last_name,
                    m.email,
                    mc.occupation
                FROM member m
                JOIN member_collectivity mc ON m.id = mc.id_member
                WHERE mc.id_collectivity = ?
                AND mc.end_date IS NULL
            ),
            total_active_cotisation AS (
                SELECT COALESCE(SUM(amount), 0) as total_amount
                FROM active_cotisations
                WHERE eligible_from <= ?::date
            )
            SELECT 
                cm.id,
                cm.first_name,
                cm.last_name,
                cm.email,
                cm.occupation,
                COALESCE(mp.earned_amount, 0) as earned_amount,
                CASE 
                    WHEN tac.total_amount - COALESCE(mp.earned_amount, 0) < 0 THEN 0
                    ELSE tac.total_amount - COALESCE(mp.earned_amount, 0)
                END as unpaid_amount
            FROM collectivity_members cm
            CROSS JOIN total_active_cotisation tac
            LEFT JOIN member_payments mp ON cm.id = mp.id_member
            ORDER BY cm.id
        """;

        List<CollectivityLocalStatistics> statistics = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            stmt.setString(2, collectivityId);
            stmt.setTimestamp(3, Timestamp.from(from));
            stmt.setTimestamp(4, Timestamp.from(to));
            stmt.setString(5, collectivityId);
            stmt.setDate(6, Date.valueOf(LocalDate.ofInstant(to, java.time.ZoneId.systemDefault())));

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                MemberDescription memberDescription = MemberDescription.builder()
                        .id(rs.getString("id"))
                        .firstName(rs.getString("first_name"))
                        .lastName(rs.getString("last_name"))
                        .email(rs.getString("email"))
                        .occupation(rs.getString("occupation"))
                        .build();

                CollectivityLocalStatistics stat = CollectivityLocalStatistics.builder()
                        .memberDescription(memberDescription)
                        .earnedAmount(rs.getDouble("earned_amount"))
                        .unpaidAmount(rs.getDouble("unpaid_amount"))
                        .assiduityPercentage(0.0) // Will be filled later when activities are implemented
                        .build();

                statistics.add(stat);
            }

            return statistics;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get local statistics for collectivity: " + collectivityId, e);
        }
    }

    /**
     * Get overall statistics for all collectivities:
     * - Percentage of members current with their dues
     * - Number of new members in period
     */
    public List<CollectivityOverallStatistics> getOverallStatistics(Instant from, Instant to) {
        String sql = """
            WITH collectivity_list AS (
                SELECT id, number, name
                FROM collectivity
            ),
            active_cotisations AS (
                SELECT 
                    cp.id_collectivity,
                    cp.amount,
                    cp.frequency
                FROM cotisation_plan cp
                WHERE cp.status = 'ACTIVE'
                AND cp.eligible_from <= ?::date
            ),
            member_payments AS (
                SELECT 
                    mc.id_collectivity,
                    mc.id_member,
                    COALESCE(SUM(t.amount), 0) as paid_amount
                FROM member_collectivity mc
                LEFT JOIN transaction t ON t.id_member = mc.id_member 
                    AND t.id_collectivity = mc.id_collectivity
                    AND t.transaction_type = 'IN'
                    AND t.transaction_date >= ?
                    AND t.transaction_date <= ?
                WHERE mc.end_date IS NULL
                GROUP BY mc.id_collectivity, mc.id_member
            ),
            total_cotisation_per_collectivity AS (
                SELECT 
                    id_collectivity,
                    SUM(amount) as total_cotisation
                FROM active_cotisations
                GROUP BY id_collectivity
            ),
            member_current_status AS (
                SELECT 
                    mp.id_collectivity,
                    mp.id_member,
                    CASE 
                        WHEN tc.total_cotisation IS NULL THEN TRUE
                        WHEN mp.paid_amount >= tc.total_cotisation THEN TRUE
                        ELSE FALSE
                    END as is_current
                FROM member_payments mp
                LEFT JOIN total_cotisation_per_collectivity tc ON mp.id_collectivity = tc.id_collectivity
            ),
            collectivity_stats AS (
                SELECT 
                    cl.id,
                    cl.number,
                    cl.name,
                    COUNT(DISTINCT mcs.id_member) as total_members,
                    COUNT(DISTINCT CASE WHEN mcs.is_current THEN mcs.id_member END) as current_members,
                    COUNT(DISTINCT CASE 
                        WHEN m.enrolment_date >= ? AND m.enrolment_date <= ? 
                        THEN m.id 
                    END) as new_members
                FROM collectivity_list cl
                JOIN member_collectivity mc ON cl.id = mc.id_collectivity AND mc.end_date IS NULL
                JOIN member m ON mc.id_member = m.id
                LEFT JOIN member_current_status mcs ON cl.id = mcs.id_collectivity 
                    AND mc.id_member = mcs.id_member
                GROUP BY cl.id, cl.number, cl.name
            )
            SELECT 
                id,
                number,
                name,
                total_members,
                current_members,
                new_members,
                CASE 
                    WHEN total_members > 0 THEN 
                        ROUND((current_members::decimal / total_members::decimal) * 100, 2)
                    ELSE 0
                END as current_due_percentage
            FROM collectivity_stats
            ORDER BY id
        """;

        List<CollectivityOverallStatistics> statistics = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(LocalDate.ofInstant(to, java.time.ZoneId.systemDefault())));
            stmt.setTimestamp(2, Timestamp.from(from));
            stmt.setTimestamp(3, Timestamp.from(to));
            stmt.setTimestamp(4, Timestamp.from(from));
            stmt.setTimestamp(5, Timestamp.from(to));

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                CollectivityInformation info = new CollectivityInformation();
                info.setName(rs.getString("name"));
                info.setNumber(rs.getString("number"));

                CollectivityOverallStatistics stat = CollectivityOverallStatistics.builder()
                        .collectivityInformation(info)
                        .newMembersNumber(rs.getInt("new_members"))
                        .overallMemberCurrentDuePercentage(rs.getDouble("current_due_percentage"))
                        .overallMemberAssiduityPercentage(0.0) // Will be filled for bonus
                        .build();

                statistics.add(stat);
            }

            return statistics;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get overall statistics", e);
        }
    }
}