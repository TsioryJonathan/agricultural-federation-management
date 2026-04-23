package com.hei.agriculturalfederationmanagement.entity.dto;

import com.hei.agriculturalfederationmanagement.entity.enums.BankName;
import com.hei.agriculturalfederationmanagement.entity.enums.MobileMoneyService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CollectivityFinancialAccountResponse {
    private String id;
    private String type;
    private Double amount;
    private String holderName;
    private BankName bankName;
    private String bankCode;
    private String bankBranchCode;
    private String bankAccountNumber;
    private String bankAccountKey;
    private MobileMoneyService mobileBankingService;
    private String mobileNumber;
}