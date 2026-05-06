package com.hei.agriculturalfederationmanagement.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonthlyRecurrenceRule {
    private Integer weekOrdinal;  // 1-5 (1st to 5th week)
    private String dayOfWeek;     // MO, TU, WE, TH, FR, SA, SU
}
