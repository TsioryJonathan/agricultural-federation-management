package com.hei.agriculturalfederationmanagement.repository;

import com.hei.agriculturalfederationmanagement.entity.enums.CollectivityOccupation;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
@AllArgsConstructor
public class MemberCollectivityRepository {
    private final Connection connection;

    public void saveMemberCollectivityLink(int memberId, int collectivityId, CollectivityOccupation occupation) {
        String sql = """
        INSERT INTO member_collectivity(
            id_member, id_collectivity, occupation, start_date, end_date
        )
        VALUES (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            stmt.setInt(2, collectivityId);
            stmt.setString(3, occupation.name());
            stmt.setTimestamp(4, Timestamp.from(Instant.now()));
            stmt.setTimestamp(5, null);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save member_collectivity link", e);
        }
    }

    public void saveMemberCollectivityLinks(List<Object[]> memberCollectivityData) {
        String sql = """
        INSERT INTO member_collectivity(
            id_member, id_collectivity, occupation, start_date, end_date
        )
        VALUES (?, ?, ?, ?, ?)
        """;

        try {
            connection.setAutoCommit(false);

            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                for (Object[] data : memberCollectivityData) {
                    int memberId = (int) data[0];
                    int collectivityId = (int) data[1];
                    CollectivityOccupation occupation = (CollectivityOccupation) data[2];

                    stmt.setInt(1, memberId);
                    stmt.setInt(2, collectivityId);
                    stmt.setString(3, occupation.name());
                    stmt.setTimestamp(4, Timestamp.from(Instant.now()));
                    stmt.setTimestamp(5, null);
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }

            connection.commit();
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Failed to rollback", ex);
            }
            throw new RuntimeException("Failed to save member_collectivity links", e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to reset auto-commit", e);
            }
        }
    }
}