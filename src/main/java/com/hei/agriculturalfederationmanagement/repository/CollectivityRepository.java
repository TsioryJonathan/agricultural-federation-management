package com.hei.agriculturalfederationmanagement.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;

@Repository
@AllArgsConstructor
public class CollectivityRepository {
    private final Connection connection;
}
