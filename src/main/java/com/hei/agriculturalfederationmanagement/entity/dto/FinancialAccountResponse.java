package com.hei.agriculturalfederationmanagement.entity.dto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = CashAccountResponse.class, name = "CASH"),
        @JsonSubTypes.Type(value = MobileBankingAccountResponse.class, name = "MOBILE_BANKING"),
        @JsonSubTypes.Type(value = BankAccountResponse.class, name = "BANK")
})
public abstract class FinancialAccountResponse {
    private String id;
    private Double amount;
}