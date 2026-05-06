package com.hei.agriculturalfederationmanagement.service;

import com.hei.agriculturalfederationmanagement.entity.*;
import com.hei.agriculturalfederationmanagement.entity.dto.*;
import com.hei.agriculturalfederationmanagement.exception.BadRequestException;
import com.hei.agriculturalfederationmanagement.exception.NotFoundException;
import com.hei.agriculturalfederationmanagement.mapper.CollectivityMapper;
import com.hei.agriculturalfederationmanagement.repository.CollectivityRepository;
import com.hei.agriculturalfederationmanagement.repository.CotisationPlanRepository;
import com.hei.agriculturalfederationmanagement.validator.CollectivityValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

@Service
@AllArgsConstructor
public class CollectivityService {
    private final CollectivityRepository repository;
    private final CotisationPlanRepository cotisationPlanRepository;
    private final CollectivityMapper mapper;
    private final CollectivityValidator validator;

    public List<CollectivityResponse> createCollectivities(List<CreateCollectivity> createCollectivities) {
        List<Collectivity> collectivitiesToSave = new ArrayList<>();
        List<List<String>> memberIdsList = new ArrayList<>();
        List<String> presidentIds = new ArrayList<>();
        List<String> vicePresidentIds = new ArrayList<>();
        List<String> treasurerIds = new ArrayList<>();
        List<String> secretaryIds = new ArrayList<>();

        for (CreateCollectivity request : createCollectivities) {
            validator.validateCollectivityCreation(request);

            Collectivity collectivity = Collectivity.builder()
                    .speciality("Agriculture")
                    .federationApproval(request.isFederationApproval())
                    .authorizationDate(LocalDate.now())
                    .location(request.getLocation())
                    .build();

            collectivitiesToSave.add(collectivity);
            memberIdsList.add(request.getMembers());
            presidentIds.add(request.getStructure().getPresident());
            vicePresidentIds.add(request.getStructure().getVicePresident());
            treasurerIds.add(request.getStructure().getTreasurer());
            secretaryIds.add(request.getStructure().getSecretary());
        }

        List<Collectivity> savedCollectivities = repository.saveAll(
                collectivitiesToSave, memberIdsList, presidentIds,
                vicePresidentIds, treasurerIds, secretaryIds
        );

        return savedCollectivities.stream()
                .map(mapper::toResponse)
                .toList();
    }

    public CollectivityResponse assignIdentity(String id, CollectivityInformation request) {
        if (request.getNumber() == null || request.getNumber().trim().isEmpty()) {
            throw new BadRequestException("Number is required");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Name is required");
        }

        Collectivity collectivity = repository.findById(id);
        if (collectivity == null) {
            throw new NotFoundException("Collectivity not found with id: " + id);
        }

        if (collectivity.getName() != null && !collectivity.getName().isBlank()
                && collectivity.getNumber() != null && !collectivity.getNumber().isBlank()) {
            throw new BadRequestException("Collectivity identity already assigned and cannot be modified");
        }

        if (repository.existsByNumber(request.getNumber())) {
            throw new BadRequestException("Collectivity number already exists: " + request.getNumber());
        }
        if (repository.existsByName(request.getName())) {
            throw new BadRequestException("Collectivity name already exists: " + request.getName());
        }

        repository.assignIdentity(id, request.getNumber(), request.getName());
        Collectivity updated = repository.findById(id);
        return mapper.toResponse(updated);
    }

    public Collectivity getCollectivityById(String id) {
        Collectivity collectivity = repository.findById(id);
        if (collectivity == null) {
            throw new NotFoundException("Collectivity not found with id: " + id);
        }
        return collectivity;
    }

    public List<CollectivityTransactionResponse> getCollectivityTransactions(
            String id, Instant from, Instant to) {
        Collectivity collectivity = repository.findById(id);
        if (collectivity == null) {
            throw new NotFoundException("Collectivity not found with id: " + id);
        }

        if (from.isAfter(to)) {
            throw new BadRequestException("'from' date must be before or equal to 'to' date");
        }

        List<Transaction> transactions = repository.findTransactionsByCollectivityIdAndDateRange(id, from, to);

        return transactions.stream()
                .map(mapper::toTransactionResponse)
                .toList();
    }

    public CollectivityFinancialAccountResponse getFinancialAccounts(String collectivityId, Instant at) {
        Collectivity collectivity = repository.findById(collectivityId);
        if (collectivity == null) {
            throw new NotFoundException("Collectivity not found with id: " + collectivityId);
        }

        Map<String, Account> accounts = repository.loadAccountsWithTransactions(collectivityId, at);

        Double totalAmount = accounts.values().stream()
                .mapToDouble(Account::getBalance)
                .sum();

        List<Object> accountDetails = new ArrayList<>();
        for (Account account : accounts.values()) {
            Object detail = mapper.toAccountDetail(account);
            if (detail != null) {
                accountDetails.add(detail);
            }
        }

        return CollectivityFinancialAccountResponse.builder()
                .id(collectivityId)
                .amount(totalAmount)
                .accounts(accountDetails)
                .build();
    }

    public List<MembershipFeeResponse> getMembershipFees(String collectivityId) {
        Collectivity collectivity = repository.findById(collectivityId);
        if (collectivity == null) {
            throw new NotFoundException("Collectivity not found with id: " + collectivityId);
        }

        List<CotisationPlan> plans = cotisationPlanRepository.findByCollectivityId(collectivityId);

        return plans.stream()
                .map(plan -> MembershipFeeResponse.builder()
                        .id(plan.getId())
                        .eligibleFrom(plan.getEligibleFrom())
                        .frequency(plan.getFrequency())
                        .amount(plan.getAmount())
                        .label(plan.getLabel())
                        .status(plan.getStatus())
                        .build())
                .toList();
    }

    public List<MembershipFeeResponse> createMembershipFees(String collectivityId,
                                                            List<CreateMembershipFee> createMembershipFees) {
        Collectivity collectivity = repository.findById(collectivityId);
        if (collectivity == null) {
            throw new NotFoundException("Collectivity not found with id: " + collectivityId);
        }

        List<MembershipFeeResponse> responses = new ArrayList<>();

        for (CreateMembershipFee createFee : createMembershipFees) {
            if (createFee.getFrequency() == null) {
                throw new BadRequestException("Frequency is required");
            }
            if (createFee.getAmount() == null || createFee.getAmount() < 0) {
                throw new BadRequestException("Amount must be greater than or equal to 0");
            }

            CotisationPlan plan = CotisationPlan.builder()
                    .eligibleFrom(createFee.getEligibleFrom())
                    .frequency(createFee.getFrequency())
                    .amount(createFee.getAmount())
                    .label(createFee.getLabel())
                    .build();

            CotisationPlan saved = cotisationPlanRepository.save(plan, collectivityId);

            responses.add(MembershipFeeResponse.builder()
                    .id(saved.getId())
                    .eligibleFrom(saved.getEligibleFrom())
                    .frequency(saved.getFrequency())
                    .amount(saved.getAmount())
                    .label(saved.getLabel())
                    .status(saved.getStatus())
                    .build());
        }

        return responses;
    }
}