package com.hei.agriculturalfederationmanagement.entity;

import com.hei.agriculturalfederationmanagement.entity.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    private String id;
    private Collectivity collectivity;
    private CashAccount cashAccount;
    private BankAccount bankAccount;
    private MobileMoneyAccount mobileMoneyAccount;

    @Builder.Default
    private List<Transaction> transactions = new ArrayList<>();

    public Double getBalance() {
        if (transactions == null || transactions.isEmpty()) {
            return 0.0;
        }

        double totalIn = transactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.IN)
                .mapToDouble(Transaction::getAmount)
                .sum();

        double totalOut = transactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.OUT)
                .mapToDouble(Transaction::getAmount)
                .sum();

        return totalIn - totalOut;
    }

    public Double getBalance(Instant at){
        if (transactions == null || transactions.isEmpty()) {
            return 0.0;
        }

        List<Transaction> filteredTransactions = transactions.stream() .filter(t ->
                (at == null || t.getTransactionDate().isBefore(at) || t.getTransactionDate().equals(at))
        ).toList();

        double totalIn = filteredTransactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.IN)
                .mapToDouble(Transaction::getAmount)
                .sum();

        double totalOut = filteredTransactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.OUT)
                .mapToDouble(Transaction::getAmount)
                .sum();

        return totalIn - totalOut;
    }

    public Double getBalance(Instant from,Instant to){
        if (transactions == null || transactions.isEmpty()) {
            return 0.0;
        }

        List<Transaction> filteredTransactions = transactions.stream() .filter(t ->
                (from == null || !t.getTransactionDate().isBefore(from)) &&
                        (to == null || !t.getTransactionDate().isAfter(to))
        ).toList();

        double totalIn = filteredTransactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.IN)
                .mapToDouble(Transaction::getAmount)
                .sum();

        double totalOut = filteredTransactions.stream()
                .filter(t -> t.getTransactionType() == TransactionType.OUT)
                .mapToDouble(Transaction::getAmount)
                .sum();

        return totalIn - totalOut;
    }

}