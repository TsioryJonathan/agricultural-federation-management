package com.hei.agriculturalfederationmanagement.entity.dto;

import com.hei.agriculturalfederationmanagement.entity.enums.MobileBankingService;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class MobileBankingAccountResponse extends FinancialAccountResponse {
    private String holderName;
    private MobileBankingService mobileBankingService;
    private String mobileNumber;

    @Builder
    public MobileBankingAccountResponse(String id, Double amount, String holderName,
                                        MobileBankingService mobileBankingService, String mobileNumber) {
        super(id, amount);
        this.holderName = holderName;
        this.mobileBankingService = mobileBankingService;
        this.mobileNumber = mobileNumber;
    }
}