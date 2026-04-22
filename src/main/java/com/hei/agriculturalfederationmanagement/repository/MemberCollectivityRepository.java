package com.hei.agriculturalfederationmanagement.repository;

import com.hei.agriculturalfederationmanagement.entity.enums.CollectivityOccupation;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.sql.Types;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Repository
@AllArgsConstructor
public class MemberCollectivityRepository {
    private final Connection connection;

    public void saveMemberCollectivityLink(int memberId, int collectivityId, CollectivityOccupation occupation) {
        String sql = """
        insert into member_collectivity(
            id_member, id_collectivity, occupation, start_date, end_date
        )
        values (?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
            stmt.setInt(2, collectivityId);
            stmt.setObject(3, occupation.name(), Types.OTHER);
            stmt.setTimestamp(4, Timestamp.from(Instant.now()));
            stmt.setTimestamp(5, null);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save member_collectivity link", e);
        }
    }

}