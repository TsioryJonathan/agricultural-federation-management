package com.hei.agriculturalfederationmanagement.entity.dto;

import com.hei.agriculturalfederationmanagement.entity.enums.BankName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BankAccountDetail {
    private String id;
    private String type;
    private Double amount;
    private String holderName;
    private BankName bankName;
    private String bankCode;
    private String bankBranchCode;
    private String bankAccountNumber;
    private String bankAccountKey;

    public BankAccountDetail(String id, Double amount, String holderName, BankName bankName, String bankCode, String bankBranchCode, String bankAccountNumber, String bankAccountKey) {
        this.id = id;
        this.type = "BANK";
        this.amount = amount;
        this.holderName = holderName;
        this.bankName = bankName;
        this.bankCode = bankCode;
        this.bankBranchCode = bankBranchCode;
        this.bankAccountNumber = bankAccountNumber;
        this.bankAccountKey = bankAccountKey;
    }
}