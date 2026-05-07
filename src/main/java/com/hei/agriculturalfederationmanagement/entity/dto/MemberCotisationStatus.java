package com.hei.agriculturalfederationmanagement.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberCotisationStatus {
    private MemberDescription memberDescription;
    private Double totalDue;
    private Double totalPaid;
    private Double remainingToPay;
    private Boolean isLate;
}