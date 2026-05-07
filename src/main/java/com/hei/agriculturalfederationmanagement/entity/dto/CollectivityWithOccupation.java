package com.hei.agriculturalfederationmanagement.entity.dto;

import com.hei.agriculturalfederationmanagement.entity.Collectivity;

import java.util.Map;

public record CollectivityWithOccupation(
        Collectivity collectivity,
        Map<String, String> memberOccupations
) {}