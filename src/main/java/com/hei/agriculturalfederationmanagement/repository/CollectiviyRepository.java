package com.hei.agriculturalfederationmanagement.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;

@Repository
@AllArgsConstructor
public class CollectiviyRepository {
    private final Connection connection;
}
