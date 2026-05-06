package com.hei.agriculturalfederationmanagement.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyRecurrenceRule {
    private Integer weekOrdinal; // 1-5
    private String dayOfWeek; // MO, TU, WE, TH, FR, SA, SU
}