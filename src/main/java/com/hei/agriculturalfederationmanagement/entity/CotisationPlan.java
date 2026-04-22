package com.hei.agriculturalfederationmanagement.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CotisationPlan {
    private Integer id;
    private Integer idCollectivity;
    private String label;
    private Frequency frequency;
    private BigDecimal amount;
    private LocalDate eligibleFrom;
    private Boolean isActive;
}