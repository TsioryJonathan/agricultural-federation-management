package com.hei.agriculturalfederationmanagement.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectivityLocalStatistics {
    private MemberDescription memberDescription;
    private int earnedAmount;
    private int unpaidAmount;
    private Double assiduityPercentage;
}
