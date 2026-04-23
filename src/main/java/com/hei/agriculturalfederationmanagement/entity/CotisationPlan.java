package com.hei.agriculturalfederationmanagement.entity;

import com.hei.agriculturalfederationmanagement.entity.enums.Frequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CotisationPlan {
    private String id;
    private Integer idCollectivity;
    private String label;
    private Frequency frequency;
    private Double amount;
    private LocalDate eligibleFrom;
    private Boolean isActive;
}