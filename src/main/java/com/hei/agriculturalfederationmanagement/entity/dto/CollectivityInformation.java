package com.hei.agriculturalfederationmanagement.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CollectivityInformation {
    private String number;
    private String name;
}