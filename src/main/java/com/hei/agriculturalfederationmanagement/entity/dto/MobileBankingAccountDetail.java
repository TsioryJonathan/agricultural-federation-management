package com.hei.agriculturalfederationmanagement.entity.dto;

import com.hei.agriculturalfederationmanagement.entity.enums.MobileBankingService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileBankingAccountDetail {
    private String id;
    private Double amount;
    private String holderName;
    private MobileBankingService mobileBankingService;
    private String mobileNumber;
}