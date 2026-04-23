package com.hei.agriculturalfederationmanagement.controller;

import com.hei.agriculturalfederationmanagement.entity.dto.CreateMember;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateMemberPayment;
import com.hei.agriculturalfederationmanagement.entity.dto.MemberPaymentResponse;
import com.hei.agriculturalfederationmanagement.exception.BadRequestException;
import com.hei.agriculturalfederationmanagement.exception.InsufficientSponsorCount;
import com.hei.agriculturalfederationmanagement.exception.NotFoundException;
import com.hei.agriculturalfederationmanagement.exception.PaymentException;
import com.hei.agriculturalfederationmanagement.service.MemberService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/members")
@AllArgsConstructor
public class MemberController {

    private final MemberService service;

    @PostMapping
    public ResponseEntity<?> createMember(@RequestBody List<CreateMember> members) {
        try {
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(service.createMembers(members));

        } catch (PaymentException | InsufficientSponsorCount ex) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ex.getMessage());

        } catch (NotFoundException ex) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ex.getMessage());

        } catch (RuntimeException ex) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal server error: " + ex.getMessage());
        }
    }

    @PostMapping("/{id}/payments")
    public ResponseEntity<?> createMemberPayments(
            @PathVariable Integer id,
            @RequestBody List<CreateMemberPayment> requests) {
        try {
            List<MemberPaymentResponse> responses = service.createPayments(id, requests);
            return ResponseEntity.status(HttpStatus.CREATED).body(responses);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }
}