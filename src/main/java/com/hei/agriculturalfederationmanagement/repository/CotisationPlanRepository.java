package com.hei.agriculturalfederationmanagement.repository;

import com.hei.agriculturalfederationmanagement.entity.CotisationPlan;
import com.hei.agriculturalfederationmanagement.entity.enums.ActivityStatus;
import com.hei.agriculturalfederationmanagement.entity.enums.Frequency;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class CotisationPlanRepository {
    private final Connection connection;

    public CotisationPlan save(CotisationPlan cotisationPlan, String collectivityId) {
        String insertSql = """
            INSERT INTO cotisation_plan (id, label, id_collectivity, status, frequency, eligible_from, amount)
            VALUES (?, ?, ?, 'ACTIVE', ?, ?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
            String id = "cot-" + System.currentTimeMillis();
            stmt.setString(1, id);
            stmt.setString(2, cotisationPlan.getLabel());
            stmt.setString(3, collectivityId);
            stmt.setString(4, cotisationPlan.getFrequency().name());
            stmt.setDate(5, cotisationPlan.getEligibleFrom() != null ?
                    Date.valueOf(cotisationPlan.getEligibleFrom()) : null);
            stmt.setDouble(6, cotisationPlan.getAmount());
            stmt.executeUpdate();
            cotisationPlan.setId(id);
            return cotisationPlan;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save cotisation plan", e);
        }
    }

    public List<CotisationPlan> findByCollectivityId(String collectivityId) {
        String sql = """
            SELECT id, label, id_collectivity, status, frequency, eligible_from, amount
            FROM cotisation_plan WHERE id_collectivity = ?
        """;

        List<CotisationPlan> plans = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                plans.add(mapCotisationPlan(rs));
            }
            return plans;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find cotisation plans", e);
        }
    }

    public Optional<CotisationPlan> findById(String id) {
        String sql = """
            SELECT id, label, id_collectivity, status, frequency, eligible_from, amount
            FROM cotisation_plan WHERE id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapCotisationPlan(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find cotisation plan", e);
        }
    }

    private CotisationPlan mapCotisationPlan(ResultSet rs) throws SQLException {
        Date eligibleFrom = rs.getDate("eligible_from");
        return CotisationPlan.builder()
                .id(rs.getString("id"))
                .label(rs.getString("label"))
                .status(ActivityStatus.valueOf(rs.getString("status")))
                .frequency(Frequency.valueOf(rs.getString("frequency")))
                .eligibleFrom(eligibleFrom != null ? eligibleFrom.toLocalDate() : null)
                .amount(rs.getDouble("amount"))
                .build();
    }
}