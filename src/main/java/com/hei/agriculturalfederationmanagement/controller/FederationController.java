package com.hei.agriculturalfederationmanagement.controller;

import com.hei.agriculturalfederationmanagement.entity.Federation;

import com.hei.agriculturalfederationmanagement.exception.NotFoundException;
import com.hei.agriculturalfederationmanagement.service.FederationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/federation")
@AllArgsConstructor
public class FederationController {
    private final FederationService federationService;

    @GetMapping
    public ResponseEntity<?> getFederation() {
        try {
            Federation federation = federationService.getFederation();
            return ResponseEntity.status(HttpStatus.OK).body(federation);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }
}