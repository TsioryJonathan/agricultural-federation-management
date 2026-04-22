package com.hei.agriculturalfederationmanagement.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    private Integer id;
    private Integer idCollectivity;
    private Integer idFederation;
    private CashAccount cashAccount;
    private BankAccount bankAccount;
    private MobileMoneyAccount mobileMoneyAccount;
}