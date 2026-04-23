package com.hei.agriculturalfederationmanagement.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CashAccountDetail {
    private String id;
    private String type;
    private Double amount;

    public CashAccountDetail(String id, Double amount) {
        this.id = id;
        this.type = "CASH";
        this.amount = amount;
    }
}