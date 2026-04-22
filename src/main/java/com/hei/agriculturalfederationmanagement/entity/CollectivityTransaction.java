package com.hei.agriculturalfederationmanagement.entity;

import com.hei.agriculturalfederationmanagement.entity.enums.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CollectivityTransaction {
    private Integer id;
    private LocalDate creationDate;
    private BigDecimal amount;
    private PaymentMode paymentMode;
    private FinancialAccount accountCredited;
    private Member memberDebited;
    private Collectivity collectivity;
}