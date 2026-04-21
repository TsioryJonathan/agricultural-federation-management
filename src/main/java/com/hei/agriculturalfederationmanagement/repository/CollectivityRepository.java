package com.hei.agriculturalfederationmanagement.repository;

import com.hei.agriculturalfederationmanagement.entity.Collectivity;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateCollectivity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.util.List;

@Repository
@AllArgsConstructor
public class CollectivityRepository {
    private final Connection connection;

    public List<Collectivity> createCollectivities(List<CreateCollectivity> createCollectivities) {
        String sql = "";
        return null;
    }
}
