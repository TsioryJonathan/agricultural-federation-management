package com.hei.agriculturalfederationmanagement.entity.dto;

import com.hei.agriculturalfederationmanagement.entity.enums.MobileMoneyService;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class MobileBankingAccountResponse extends FinancialAccountResponse {
    private String holderName;
    private MobileMoneyService mobileBankingService;
    private String mobileNumber;
}