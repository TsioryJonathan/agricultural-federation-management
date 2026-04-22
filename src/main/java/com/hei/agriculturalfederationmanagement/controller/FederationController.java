package com.hei.agriculturalfederationmanagement.controller;

import com.hei.agriculturalfederationmanagement.entity.Federation;
import com.hei.agriculturalfederationmanagement.entity.dto.CollectivityInformation;
import com.hei.agriculturalfederationmanagement.entity.dto.CollectivityResponse;
import com.hei.agriculturalfederationmanagement.exception.ConflictException;
import com.hei.agriculturalfederationmanagement.exception.NotFoundException;
import com.hei.agriculturalfederationmanagement.service.CollectivityService;
import com.hei.agriculturalfederationmanagement.service.FederationService;
import lombok.AllArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/federation")
@AllArgsConstructor
public class FederationController {
    private final FederationService federationService;
    private final CollectivityService collectivityService;

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

    @PatchMapping("/{collectivityId}/assign-identity")
    public ResponseEntity<?> assignIdentity(
            @PathVariable Integer collectivityId,
            @RequestBody(required = false) CollectivityInformation request) {
        try {
            if(request == null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mandatory request body not provided");
            }
            CollectivityResponse response = collectivityService.assignIdentity(collectivityId, request);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (ConflictException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }
}