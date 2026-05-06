package com.hei.agriculturalfederationmanagement.repository;
import com.hei.agriculturalfederationmanagement.entity.Transaction;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;

@Repository
@AllArgsConstructor
public class TransactionRepository {
    private final Connection connection;

    public Transaction save(Transaction transaction) {
        String insertSql = """
            INSERT INTO transaction (id, id_collectivity, id_member, id_cotisation_plan, 
                                    transaction_type, amount, transaction_date, payment_mode, 
                                    description, id_account)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
            String id = "trx-" + System.currentTimeMillis();
            stmt.setString(1, id);
            stmt.setString(2, transaction.getCollectivity() != null ?
                    transaction.getCollectivity().getId() : null);
            stmt.setString(3, transaction.getMember() != null ?
                    transaction.getMember().getId() : null);
            stmt.setString(4, transaction.getCotisationPlan() != null ?
                    transaction.getCotisationPlan().getId() : null);
            stmt.setString(5, transaction.getTransactionType().name());
            stmt.setDouble(6, transaction.getAmount());
            stmt.setDate(7, Date.valueOf(transaction.getTransactionDate() != null ?
                    transaction.getTransactionDate() : LocalDate.now()));
            stmt.setString(8, transaction.getPaymentMode() != null ?
                    transaction.getPaymentMode().name() : null);
            stmt.setString(9, transaction.getDescription());
            stmt.setString(10, transaction.getAccount() != null ?
                    transaction.getAccount().getId() : null);
            stmt.executeUpdate();
            transaction.setId(id);
            return transaction;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save transaction " + e.getMessage());
        }
    }
}