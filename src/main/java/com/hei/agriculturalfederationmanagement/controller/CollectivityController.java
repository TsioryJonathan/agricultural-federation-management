package com.hei.agriculturalfederationmanagement.controller;

import com.hei.agriculturalfederationmanagement.entity.dto.CollectivityResponse;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateCollectivity;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateMembershipFee;
import com.hei.agriculturalfederationmanagement.entity.dto.MembershipFeeResponse;
import com.hei.agriculturalfederationmanagement.exception.BadRequestException;
import com.hei.agriculturalfederationmanagement.exception.NotFoundException;
import com.hei.agriculturalfederationmanagement.service.CollectivityService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/collectivities")
public class CollectivityController {
    private final CollectivityService service;

    @PostMapping
    public ResponseEntity<?> createCollectivities(@RequestBody(required = false) List<CreateCollectivity> createCollectivities){
        try{
            if(createCollectivities == null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mandatory body not provided");
            }
            List<CollectivityResponse> collectivities = service.createCollectivities(createCollectivities);
            return ResponseEntity.status(HttpStatus.CREATED).body(collectivities);
        }catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/membershipFees")
    public ResponseEntity<?> getMembershipFees(@PathVariable Integer id) {
        try {
            List<MembershipFeeResponse> membershipFees = service.getMembershipFees(id);
            return ResponseEntity.ok(membershipFees);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/membershipFees")
    public ResponseEntity<?> createMembershipFees(@PathVariable Integer id, @RequestBody(required = false) List<CreateMembershipFee> createMembershipFees) {
        try {
            if (createMembershipFees == null || createMembershipFees.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request body is required");
            }
            List<MembershipFeeResponse> membershipFees = service.createMembershipFees(id, createMembershipFees);
            return ResponseEntity.ok(membershipFees);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }
}
