package com.hei.agriculturalfederationmanagement.entity;

import com.hei.agriculturalfederationmanagement.entity.enums.MobileBankingService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileMoneyAccount {
    private String id;
    private Account account;
    private String holderName;
    private MobileBankingService serviceName;
    private Integer phoneNumber;
}