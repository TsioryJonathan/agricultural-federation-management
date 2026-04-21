package com.hei.agriculturalfederationmanagement.service;

import com.hei.agriculturalfederationmanagement.entity.Federation;
import com.hei.agriculturalfederationmanagement.exception.NotFoundException;
import com.hei.agriculturalfederationmanagement.repository.FederationRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@AllArgsConstructor
public class FederationService {

    private final FederationRepository repository;

    public Federation getFederation(){
        return repository.findFederation().orElseThrow(()-> new NotFoundException("Federation not found"));
    }
}
