package com.hei.agriculturalfederationmanagement.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class FederationRepository {
    private final Connection connection;


}
