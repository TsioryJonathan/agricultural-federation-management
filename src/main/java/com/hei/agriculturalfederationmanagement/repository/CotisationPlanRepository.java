package com.hei.agriculturalfederationmanagement.repository;

import com.hei.agriculturalfederationmanagement.entity.CotisationPlan;
import com.hei.agriculturalfederationmanagement.entity.enums.Frequency;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class CotisationPlanRepository {
    private final Connection connection;

    public Optional<CotisationPlan> findById(Integer id) {
        String sql = """
            select id, id_collectivity, label, frequency, amount, eligible_from, is_active
            from cotisation_plan
            where id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToCotisationPlan(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find cotisation plan", e);
        }
    }

    private CotisationPlan mapResultSetToCotisationPlan(ResultSet rs) throws SQLException {
        return CotisationPlan.builder()
                .id(rs.getInt("id"))
                .idCollectivity(rs.getInt("id_collectivity"))
                .label(rs.getString("label"))
                .frequency(Frequency.valueOf(rs.getString("frequency")))
                .amount(rs.getDouble("amount"))
                .eligibleFrom(rs.getDate("eligible_from") != null ?
                        rs.getDate("eligible_from").toLocalDate() : null)
                .isActive(rs.getBoolean("is_active"))
                .build();
    }
}