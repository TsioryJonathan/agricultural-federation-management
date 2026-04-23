package com.hei.agriculturalfederationmanagement.entity.dto;

import com.hei.agriculturalfederationmanagement.entity.enums.MobileMoneyService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MobileBankingAccountDetail {
    private String id;
    private String type;
    private Double amount;
    private String holderName;
    private MobileMoneyService mobileBankingService;
    private String mobileNumber;

    public MobileBankingAccountDetail(String id, Double amount, String holderName, MobileMoneyService mobileBankingService, String mobileNumber) {
        this.id = id;
        this.type = "MOBILE_BANKING";
        this.amount = amount;
        this.holderName = holderName;
        this.mobileBankingService = mobileBankingService;
        this.mobileNumber = mobileNumber;
    }
}