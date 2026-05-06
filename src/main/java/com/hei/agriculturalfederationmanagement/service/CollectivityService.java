
import com.hei.agriculturalfederationmanagement.entity.*;
import com.hei.agriculturalfederationmanagement.entity.dto.*;
import com.hei.agriculturalfederationmanagement.entity.enums.*;
import com.hei.agriculturalfederationmanagement.exception.BadRequestException;
import com.hei.agriculturalfederationmanagement.exception.NotFoundException;
import com.hei.agriculturalfederationmanagement.mapper.CollectivityMapper;
import com.hei.agriculturalfederationmanagement.repository.CollectivityRepository;
import com.hei.agriculturalfederationmanagement.repository.MemberRepository;
import com.hei.agriculturalfederationmanagement.validator.CollectivityValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CollectivityService {
    private final CollectivityRepository repository;
    private final MemberRepository memberRepository;
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
                    .authorizationDate(Instant.now())
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
                .map(this::toTransactionResponse)
                .toList();
    }

    private CollectivityTransactionResponse toTransactionResponse(Transaction transaction) {
        return CollectivityTransactionResponse.builder()
                .id(transaction.getId())
                .creationDate(transaction.getTransactionDate())
                .amount(transaction.getAmount())
                .paymentMode(transaction.getPaymentMode())
                .accountCredited(toFinancialAccountResponse(transaction.getAccount()))
                .memberDebited(toMemberResponse(transaction))
                .build();
    }

    // ... Additional helper methods would continue here

    public CollectivityFinancialAccountResponse getFinancialAccounts(String collectivityId, Instant at) {
        Collectivity collectivity = repository.findById(collectivityId);
        if (collectivity == null) {
            throw new NotFoundException("Collectivity not found with id: " + collectivityId);
        }

        Map<String, Account> accounts = repository.loadAccountsWithTransactions(collectivityId, at);

        CollectivityFinancialAccountResponse response = CollectivityFinancialAccountResponse.builder()
                .id(collectivityId)
                .amount(accounts.values().stream().mapToDouble(Account::getBalance).sum())
                .accounts(new ArrayList<>())
                .build();

        for (Account account : accounts.values()) {
            // Build appropriate account detail based on type
            Double balance = account.getBalance();
            if (account.getCashAccount() != null) {
                response.getAccounts().add(CashAccountDetail.builder()
                        .id(account.getId())
                        .amount(balance)
                        .type("CASH")
                        .build());
            } else if (account.getMobileMoneyAccount() != null) {
                // Add mobile money account detail
            } else if (account.getBankAccount() != null) {
                // Add bank account detail
            }
        }

        return response;
    }
}