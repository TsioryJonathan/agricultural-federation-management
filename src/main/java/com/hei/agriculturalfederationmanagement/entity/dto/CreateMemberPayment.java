package com.hei.agriculturalfederationmanagement.entity.dto;

import com.hei.agriculturalfederationmanagement.entity.enums.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateMemberPayment {
    private Double amount;
    private Integer membershipFeeIdentifier;
    private Integer accountCreditedIdentifier;
    private PaymentMode paymentMode;
}
