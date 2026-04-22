package com.hei.agriculturalfederationmanagement.entity;

import com.hei.agriculturalfederationmanagement.entity.enums.MobileMoneyService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MobileMoneyAccount {
    private Integer id;
    private Account account;
    private String holderName;
    private MobileMoneyService serviceName;
    private String phoneNumber;
}