package com.hei.agriculturalfederationmanagement.repository;

import com.hei.agriculturalfederationmanagement.entity.Transaction;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;

@Repository
@AllArgsConstructor
public class TransactionRepository {
    private final Connection connection;

    public Transaction save(Transaction transaction) {
        String sql = """
            insert into transaction (id_member, id_collectivity, id_cotisation_plan, id_account,
                                    transaction_type, amount, transaction_date, payment_mode, description)
            values (?, ?, ?, ?, ?::transaction_type, ?, ?, ?::payment_mode, ?)
            returning id
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1,transaction.getMember().getId());
            stmt.setString(2,transaction.getCollectivity().getId());
            if (transaction.getCotisationPlan() != null) {
                stmt.setString(3, transaction.getCotisationPlan().getId());
            } else {
                stmt.setNull(3, Types.INTEGER);
            }
            stmt.setString(4, transaction.getAccount().getId());
            stmt.setString(5, transaction.getTransactionType().name());
            stmt.setDouble(6, transaction.getAmount());
            stmt.setTimestamp(7, Timestamp.from(transaction.getTransactionDate()));
            stmt.setString(8, transaction.getPaymentMode() != null ? transaction.getPaymentMode().name() : null);
            stmt.setString(9, transaction.getDescription());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                transaction.setId(rs.getString("id"));
            }
            return transaction;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save transaction " + e.getMessage());
        }
    }
}