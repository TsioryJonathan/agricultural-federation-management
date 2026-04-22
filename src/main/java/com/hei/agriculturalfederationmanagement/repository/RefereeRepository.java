package com.hei.agriculturalfederationmanagement.repository;

import com.hei.agriculturalfederationmanagement.entity.MemberRefereeLink;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@Repository
@AllArgsConstructor
public class RefereeRepository {
    private final Connection connection;

    public void saveRefereeMemberLink(List<MemberRefereeLink> linkToAdd) {
        String insertRefSql = """
        INSERT INTO member_referee(
            id_candidate, id_referee, id_collectivity, relationship, created_at
        )
        VALUES (?, ?, ?, ?, ?)
        """;

        try {
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to set auto-commit", e);
        }

        try(PreparedStatement pstmt = connection.prepareStatement(insertRefSql)){
            for(MemberRefereeLink link : linkToAdd){
                pstmt.setInt(1, link.getIdMember());
                pstmt.setInt(2, link.getIdReferee());
                pstmt.setInt(3, link.getIdCollectivity());
                pstmt.setString(4, link.getLink());
                pstmt.setTimestamp(5, Timestamp.from(Instant.now()));

                pstmt.addBatch();
            }
            pstmt.executeBatch();
            connection.commit();
        } catch (RuntimeException | SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new RuntimeException("Failed to rollback", ex);
            }
            throw new RuntimeException(e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                throw new RuntimeException("Failed to reset auto-commit", e);
            }
        }
    }
}