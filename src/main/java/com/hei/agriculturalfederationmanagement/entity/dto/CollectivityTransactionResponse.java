package com.hei.agriculturalfederationmanagement.entity.dto;

import com.hei.agriculturalfederationmanagement.entity.enums.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectivityTransactionResponse {
    private String id;
    private Instant creationDate;
    private BigDecimal amount;
    private PaymentMode paymentMode;
    private FinancialAccountResponse accountCredited;
    private MemberResponse memberDebited;
}