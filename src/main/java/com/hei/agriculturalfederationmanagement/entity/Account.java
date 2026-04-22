package com.hei.agriculturalfederationmanagement.entity;

import com.hei.agriculturalfederationmanagement.entity.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    private Integer id;
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

}