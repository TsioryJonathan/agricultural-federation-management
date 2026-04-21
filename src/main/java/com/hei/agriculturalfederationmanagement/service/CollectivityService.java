package com.hei.agriculturalfederationmanagement.service;

import com.hei.agriculturalfederationmanagement.entity.Collectivity;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateCollectivity;
import com.hei.agriculturalfederationmanagement.repository.CollectivityRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class CollectivityService {
    private final CollectivityRepository repository;

    public List<Collectivity> createCollectivies(List<CreateCollectivity> createCollectivities) {
        return repository.createCollectivities(createCollectivities);
    }
}
