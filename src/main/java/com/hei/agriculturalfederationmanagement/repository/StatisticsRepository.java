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
import java.time.ZoneId;
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
     * - Assiduity percentage per member (based on activity attendance)
     */
    public List<CollectivityLocalStatistics> getLocalStatistics(String collectivityId, LocalDate from, LocalDate to) {
        String sql = """
        WITH active_cotisations AS (
            SELECT id, amount, frequency, eligible_from
            FROM cotisation_plan
            WHERE id_collectivity = ?
            AND status = 'ACTIVE'
            AND eligible_from <= ?::date
        ),
        member_payments AS (
            SELECT t.id_member, COALESCE(SUM(t.amount), 0) as earned_amount
            FROM transaction t
            WHERE t.id_collectivity = ?
            AND t.transaction_type = 'IN'
            AND t.transaction_date >= ?::date
            AND t.transaction_date <= ?::date
            GROUP BY t.id_member
        ),
        collectivity_members AS (
            SELECT m.id, m.first_name, m.last_name, m.email, mc.occupation
            FROM member m
            JOIN member_collectivity mc ON m.id = mc.id_member
            WHERE mc.id_collectivity = ? AND mc.end_date IS NULL
        ),
        total_active_cotisation AS (
            SELECT COALESCE(SUM(
                CASE
                    WHEN frequency = 'WEEKLY' THEN
                        amount * GREATEST(FLOOR((?::date - eligible_from) / 7.0) + 1, 0)
                    WHEN frequency = 'MONTHLY' THEN
                        amount * GREATEST(
                            (DATE_PART('year', ?::date) - DATE_PART('year', eligible_from)) * 12
                            + DATE_PART('month', ?::date) - DATE_PART('month', eligible_from) + 1, 0)
                    WHEN frequency = 'ANNUALLY' THEN
                        amount * GREATEST(DATE_PART('year', ?::date) - DATE_PART('year', eligible_from) + 1, 0)
                    WHEN frequency = 'PUNCTUALLY' THEN
                        CASE WHEN eligible_from BETWEEN ?::date AND ?::date THEN amount ELSE 0 END
                    ELSE 0
                END
            ), 0) AS total_amount
            FROM active_cotisations
        ),
        member_activities AS (
            SELECT a.id AS activity_id, aa.id_member, aa.attendance_status
            FROM activity a
            JOIN activity_attendance aa ON a.id = aa.id_activity
            WHERE a.id_collectivity = ? AND a.executive_date >= ?::date AND a.executive_date <= ?::date
        ),
        member_attendance_stats AS (
            SELECT ma.id_member,
                COUNT(ma.activity_id) as total_activities,
                COUNT(CASE WHEN ma.attendance_status = 'ATTENDED' THEN 1 END) as attended_activities
            FROM member_activities ma
            GROUP BY ma.id_member
        )
        SELECT cm.id, cm.first_name, cm.last_name, cm.email, cm.occupation,
            COALESCE(mp.earned_amount, 0) as earned_amount,
            GREATEST(tac.total_amount - COALESCE(mp.earned_amount, 0), 0) as unpaid_amount,
            CASE WHEN mas.total_activities IS NULL OR mas.total_activities = 0 THEN 0.0
                ELSE ROUND((mas.attended_activities::decimal / mas.total_activities::decimal) * 100, 2)
            END as assiduity_percentage
        FROM collectivity_members cm
        CROSS JOIN total_active_cotisation tac
        LEFT JOIN member_payments mp ON cm.id = mp.id_member
        LEFT JOIN member_attendance_stats mas ON cm.id = mas.id_member
        ORDER BY cm.id
    """;

        List<CollectivityLocalStatistics> statistics = new ArrayList<>();
        LocalDate fromDate = from;
        LocalDate toDate = to;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int i = 1;
            stmt.setString(i++, collectivityId);        // 1
            stmt.setString(i++, toDate.toString());     // 2: ::date
            stmt.setString(i++, collectivityId);        // 3
            stmt.setString(i++, fromDate.toString());   // 4: ::date
            stmt.setString(i++, toDate.toString());     // 5: ::date
            stmt.setString(i++, collectivityId);        // 6
            stmt.setString(i++, toDate.toString());     // 7: WEEKLY ::date
            stmt.setString(i++, toDate.toString());     // 8: MONTHLY year ::date
            stmt.setString(i++, toDate.toString());     // 9: MONTHLY month ::date
            stmt.setString(i++, toDate.toString());     // 10: ANNUALLY ::date
            stmt.setString(i++, fromDate.toString());   // 11: PUNCTUALLY from ::date
            stmt.setString(i++, toDate.toString());     // 12: PUNCTUALLY to ::date
            stmt.setString(i++, collectivityId);        // 13
            stmt.setString(i++, fromDate.toString());   // 14: ::date
            stmt.setString(i++, toDate.toString());     // 15: ::date

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                statistics.add(CollectivityLocalStatistics.builder()
                        .memberDescription(MemberDescription.builder()
                                .id(rs.getString("id"))
                                .firstName(rs.getString("first_name"))
                                .lastName(rs.getString("last_name"))
                                .email(rs.getString("email"))
                                .occupation(rs.getString("occupation"))
                                .build())
                        .earnedAmount(rs.getDouble("earned_amount"))
                        .unpaidAmount(rs.getDouble("unpaid_amount"))
                        .assiduityPercentage(rs.getDouble("assiduity_percentage"))
                        .build());
            }
            return statistics;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get local statistics: " + collectivityId, e);
        }
    }
    /**
     * Get overall statistics for all collectivities:
     * - Percentage of members current with their dues
     * - Number of new members in period
     * - Overall assiduity percentage per collectivity
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
            collectivity_attendance AS (
                SELECT 
                    a.id_collectivity,
                    aa.id_member,
                    aa.attendance_status
                FROM activity a
                JOIN activity_attendance aa ON a.id = aa.id_activity
                WHERE a.executive_date >= ?::date
                AND a.executive_date <= ?::date
            ),
            collectivity_attendance_stats AS (
                SELECT 
                    ca.id_collectivity,
                    COUNT(DISTINCT ca.id_member) as total_members_with_activities,
                    COUNT(DISTINCT CASE WHEN ca.attendance_status = 'ATTENDED' THEN ca.id_member END) as members_attended,
                    COUNT(*) as total_attendance_records,
                    COUNT(CASE WHEN ca.attendance_status = 'ATTENDED' THEN 1 END) as attended_records
                FROM collectivity_attendance ca
                GROUP BY ca.id_collectivity
            ),
            collectivity_stats AS (
                SELECT 
                    cl.id,
                    cl.number,
                    cl.name,
                    COUNT(DISTINCT mc.id_member) as total_members,
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
                cs.id,
                cs.number,
                cs.name,
                cs.total_members,
                cs.current_members,
                cs.new_members,
                CASE 
                    WHEN cs.total_members > 0 THEN 
                        ROUND((cs.current_members::decimal / cs.total_members::decimal) * 100, 2)
                    ELSE 0
                END as current_due_percentage,
                COALESCE(cas.total_members_with_activities, 0) as total_with_activities,
                COALESCE(cas.members_attended, 0) as attended_members,
                CASE 
                    WHEN cas.total_attendance_records IS NULL OR cas.total_attendance_records = 0 THEN 0.0
                    ELSE ROUND((cas.attended_records::decimal / cas.total_attendance_records::decimal) * 100, 2)
                END as assiduity_percentage
            FROM collectivity_stats cs
            LEFT JOIN collectivity_attendance_stats cas ON cs.id = cas.id_collectivity
            ORDER BY cs.id
        """;

        List<CollectivityOverallStatistics> statistics = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int paramIndex = 1;
            stmt.setDate(paramIndex++, Date.valueOf(LocalDate.ofInstant(to, ZoneId.systemDefault())));
            stmt.setTimestamp(paramIndex++, Timestamp.from(from));
            stmt.setTimestamp(paramIndex++, Timestamp.from(to));
            stmt.setDate(paramIndex++, Date.valueOf(LocalDate.ofInstant(from, ZoneId.systemDefault())));
            stmt.setDate(paramIndex++, Date.valueOf(LocalDate.ofInstant(to, ZoneId.systemDefault())));
            stmt.setTimestamp(paramIndex++, Timestamp.from(from));
            stmt.setTimestamp(paramIndex++, Timestamp.from(to));

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                CollectivityInformation info = new CollectivityInformation();
                info.setName(rs.getString("name"));
                info.setNumber(rs.getString("number"));

                CollectivityOverallStatistics stat = CollectivityOverallStatistics.builder()
                        .collectivityInformation(info)
                        .newMembersNumber(rs.getInt("new_members"))
                        .overallMemberCurrentDuePercentage(rs.getDouble("current_due_percentage"))
                        .overallMemberAssiduityPercentage(rs.getDouble("assiduity_percentage"))
                        .build();

                statistics.add(stat);
            }

            return statistics;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get overall statistics " + e.getMessage());
        }
    }
}