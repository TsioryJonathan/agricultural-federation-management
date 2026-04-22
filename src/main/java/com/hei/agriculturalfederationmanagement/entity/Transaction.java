package com.hei.agriculturalfederationmanagement.entity;

import com.hei.agriculturalfederationmanagement.entity.enums.PaymentMode;
import com.hei.agriculturalfederationmanagement.entity.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private Integer id;
    private Integer idMember;
    private Integer idCollectivity;
    private Integer idCotisationPlan;
    private Integer idAccount;
    private TransactionType transactionType;
    private Double amount;
    private Instant transactionDate;
    private PaymentMode paymentMode;
    private String description;
    private Account account;
    private Member member;
    private CotisationPlan cotisationPlan;
}