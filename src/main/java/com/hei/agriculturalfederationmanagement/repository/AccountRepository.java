package com.hei.agriculturalfederationmanagement.repository;

import com.hei.agriculturalfederationmanagement.entity.Account;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class AccountRepository {
    private final Connection connection;

    public Optional<Account> findById(Integer id) {
        String sql = "select id, id_collectivity, id_federation from account where id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(Account.builder()
                        .id(rs.getInt("id"))
                        .build());
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find account", e);
        }
    }

    public boolean existsByIdAndCollectivityId(Integer accountId, Integer collectivityId) {
        String sql = "select count(id) from account where id = ? and id_collectivity = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, accountId);
            stmt.setInt(2, collectivityId);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check account existence", e);
        }
    }
}