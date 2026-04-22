package com.hei.agriculturalfederationmanagement.entity;

import com.hei.agriculturalfederationmanagement.entity.enums.BankName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankAccount {
    private Integer id;
    private Account account;
    private String holderName;
    private BankName bankName;
    private String bankCode;
    private String branchCode;
    private String accountNumber;
    private String ribKey;
}