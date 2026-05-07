package com.hei.agriculturalfederationmanagement.service;

import com.hei.agriculturalfederationmanagement.entity.*;
import com.hei.agriculturalfederationmanagement.entity.dto.*;
import com.hei.agriculturalfederationmanagement.entity.enums.ActivityStatus;
import com.hei.agriculturalfederationmanagement.exception.BadRequestException;
import com.hei.agriculturalfederationmanagement.exception.NotFoundException;
import com.hei.agriculturalfederationmanagement.mapper.Mapper;
import com.hei.agriculturalfederationmanagement.repository.CollectivityRepository;
import com.hei.agriculturalfederationmanagement.repository.CotisationPlanRepository;
import com.hei.agriculturalfederationmanagement.validator.CollectivityValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
@AllArgsConstructor
public class CollectivityService {
    private final CollectivityRepository repository;
    private final CotisationPlanRepository cotisationPlanRepository;
    private final Mapper mapper;
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
                .map(collectivity -> {
                    Map<String, String> occupations = repository.findMemberOccupations(collectivity.getId());
                    return mapper.toResponse(collectivity, occupations);
                })
                .toList();
    }

    public CollectivityResponse assignIdentity(String id, CollectivityInformation request) {
        Collectivity collectivity = repository.findById(id);
        if (collectivity == null) {
            throw new NotFoundException("Collectivity not found with id: " + id);
        }

        String newName = request.getName();
        Integer newNumber = request.getNumber();

        String currentName = collectivity.getName();
        String currentNumber = collectivity.getNumber();

        if (newName != null && !newName.trim().isEmpty()
                && currentName != null && !currentName.trim().isEmpty()) {
            throw new BadRequestException("Collectivity name is already assigned and cannot be modified");
        }

        if (newNumber != null
                && currentNumber != null && !currentNumber.trim().isEmpty()) {
            throw new BadRequestException("Collectivity number is already assigned and cannot be modified");
        }

        if (newName != null && !newName.trim().isEmpty() && repository.existsByName(newName)) {
            throw new BadRequestException("Collectivity name already exists: " + newName);
        }
        if (newNumber != null && repository.existsByNumber(String.valueOf(newNumber))) {
            throw new BadRequestException("Collectivity number already exists: " + newNumber);
        }

        String finalName = (newName != null && !newName.trim().isEmpty()) ? newName : currentName;
        Integer finalNumber = (newNumber != null) ? newNumber : Integer.valueOf(currentNumber);

        repository.assignIdentity(id, String.valueOf(finalNumber), finalName);
        Collectivity updated = repository.findById(id);

        Map<String, String> occupations = repository.findMemberOccupations(id);
        return mapper.toResponse(updated, occupations);
    }
    public CollectivityResponse getCollectivityById(String id) {
        Collectivity collectivity = repository.findById(id);
        if (collectivity == null) {
            throw new NotFoundException("Collectivity not found with id: " + id);
        }

        Map<String, String> occupations = repository.findMemberOccupations(id);
        return mapper.toResponse(collectivity, occupations);
    }

    // Service method
    public List<CollectivityTransactionResponse> getCollectivityTransactions(
            String id, String from, String to) {

        Collectivity collectivity = repository.findById(id);

        if (collectivity == null) {
            throw new NotFoundException("Collectivity not found with id: " + id);
        }

        LocalDate fromDate;
        LocalDate toDate;

        try {
            fromDate = LocalDate.parse(from);
            toDate = LocalDate.parse(to);
        } catch (DateTimeParseException e) {
            throw new BadRequestException("Invalid date format. Expected format: yyyy-MM-dd");
        }

        if (fromDate.isAfter(toDate)) {
            throw new BadRequestException("'from' date must be before or equal to 'to' date");
        }

        List<Transaction> transactions =
                repository.findTransactionsByCollectivityIdAndDateRange(id, fromDate, toDate);

        Map<String, String> occupations =
                repository.findMemberOccupations(id);

        return transactions.stream()
                .map(tx -> mapper.toTransactionResponse(tx, occupations))
                .toList();
    }

    public List<Object> getFinancialAccounts(String collectivityId, Instant at) {
        Collectivity collectivity = repository.findById(collectivityId);
        if (collectivity == null) {
            throw new NotFoundException("Collectivity not found with id: " + collectivityId);
        }

        Map<String, Account> accounts = repository.loadAccountsWithTransactions(collectivityId, at);

        List<Object> accountDetails = new ArrayList<>();
        for (Account account : accounts.values()) {
            Object detail = mapper.toAccountDetail(account);
            if (detail != null) {
                accountDetails.add(detail);
            }
        }

        return accountDetails;
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
                    .status(ActivityStatus.valueOf("ACTIVE"))
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