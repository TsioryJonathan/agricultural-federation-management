package com.hei.agriculturalfederationmanagement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hei.agriculturalfederationmanagement.entity.enums.ActivityType;
import com.hei.agriculturalfederationmanagement.entity.enums.CollectivityOccupation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Activity {
    private String id;
    private String label;
    private ActivityType activityType;
    private List<CollectivityOccupation> memberOccupationConcerned;
    private MonthlyRecurrenceRule recurrenceRule;
    private LocalDate executiveDate;
    
    @JsonIgnore
    private String collectivityId;
    
    @JsonIgnore
    private String federationId;
}
