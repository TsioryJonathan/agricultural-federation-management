package com.hei.agriculturalfederationmanagement.repository;

import com.hei.agriculturalfederationmanagement.entity.MembershipFee;
import com.hei.agriculturalfederationmanagement.entity.enums.ActivityStatus;
import com.hei.agriculturalfederationmanagement.entity.enums.Frequency;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@AllArgsConstructor
public class MembershipFeeRepository {
    private final Connection connection;

    public List<MembershipFee> findByCollectivityId(Integer collectivityId) {
        String sql = """
            select id, label, frequency, amount, eligible_from, is_active
            from cotisation_plan
            where id_collectivity = ?
            order by eligible_from desc
        """;

        List<MembershipFee> membershipFees = new ArrayList<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, collectivityId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                membershipFees.add(mapResultSetToMembershipFee(rs));
            }
            return membershipFees;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find membership fees", e);
        }
    }

    private MembershipFee mapResultSetToMembershipFee(ResultSet rs) throws SQLException {
        return MembershipFee.builder()
                .id(rs.getInt("id"))
                .label(rs.getString("label"))
                .frequency(Frequency.valueOf(rs.getString("frequency")))
                .amount(rs.getDouble("amount"))
                .eligibleFrom(rs.getObject("eligible_from", LocalDate.class))
                .status(rs.getBoolean("is_active") ? ActivityStatus.ACTIVE : ActivityStatus.INACTIVE)
                .build();
    }

    public MembershipFee save(MembershipFee membershipFee, Integer collectivityId) {
        String sql = """
            insert into cotisation_plan (id_collectivity, label, frequency, amount, eligible_from, is_active)
            values (?, ?, ?::cotisation_frequency, ?, ?, true)
            returning id
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, collectivityId);
            stmt.setString(2, membershipFee.getLabel());
            stmt.setObject(3, membershipFee.getFrequency().name(), Types.OTHER);
            stmt.setDouble(4, membershipFee.getAmount());
            stmt.setObject(5, membershipFee.getEligibleFrom());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                membershipFee.setId(rs.getInt("id"));
                membershipFee.setStatus(ActivityStatus.ACTIVE);
            }
            return membershipFee;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save membership fee", e);
        }
    }

    public boolean existsByCollectivityIdAndFrequency(Integer collectivityId, Frequency frequency) {
        String sql = "select count(id) from cotisation_plan where id_collectivity = ? and frequency = ?::cotisation_frequency and is_active = true";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, collectivityId);
            stmt.setObject(2, frequency.name(), Types.OTHER);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check frequency existence", e);
        }
    }
}