package com.hei.agriculturalfederationmanagement.entity.dto;

import com.hei.agriculturalfederationmanagement.entity.enums.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberPaymentResponse {
    private String id;
    private Double amount;
    private PaymentMode paymentMode;
    private FinancialAccountResponse accountCredited;
    private Instant creationDate;
}