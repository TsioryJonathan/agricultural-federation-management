package com.hei.agriculturalfederationmanagement.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private List<Transaction> transactions;

    public Double getBalance() {
        if (transactions == null || transactions.isEmpty()) {
            return 0.0;
        }
        return transactions.stream()
                .mapToDouble(t -> t.getTransactionType().equals("IN") ? t.getAmount() : -t.getAmount())
                .sum();
    }
}