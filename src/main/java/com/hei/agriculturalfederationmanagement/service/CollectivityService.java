package com.hei.agriculturalfederationmanagement.service;

import com.hei.agriculturalfederationmanagement.entity.*;
import com.hei.agriculturalfederationmanagement.entity.dto.*;
import com.hei.agriculturalfederationmanagement.entity.enums.AttendanceStatus;
import com.hei.agriculturalfederationmanagement.exception.BadRequestException;
import com.hei.agriculturalfederationmanagement.exception.NotFoundException;
import com.hei.agriculturalfederationmanagement.repository.ActivityRepository;
import com.hei.agriculturalfederationmanagement.repository.CollectivityRepository;
import com.hei.agriculturalfederationmanagement.repository.MembershipFeeRepository;
import com.hei.agriculturalfederationmanagement.validator.CollectivityValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class CollectivityService {
    private final CollectivityRepository repository;
    private final MembershipFeeRepository membershipFeeRepository;
    private final CollectivityValidator validator;
    private final ActivityRepository activityRepository;

    public List<CollectivityResponse> createCollectivities(List<CreateCollectivity> createCollectivities) throws BadRequestException {
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
                collectivitiesToSave,
                memberIdsList,
                presidentIds,
                vicePresidentIds,
                treasurerIds,
                secretaryIds
        );

        return savedCollectivities.stream()
                .map(this::buildResponse)
                .toList();
    }


    public CollectivityResponse assignIdentity(String id, CollectivityInformation request) throws BadRequestException {
        if (request.getNumber() == null) {
            throw new BadRequestException("Collectivity number is required");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Collectivity name is required");
        }

        Collectivity collectivity = repository.findById(id);
        if (collectivity == null) {
            throw new NotFoundException("Collectivity not found with id: " + id);
        }

        if (collectivity.getName() != null && !collectivity.getName().isBlank()
                && collectivity.getNumber() != null) {

            throw new BadRequestException("Collectivity identity already assigned and cannot be modified");
        }

        if (repository.existsByNumber(String.valueOf(request.getNumber()))) {
            throw new BadRequestException("Collectivity number already exists: " + request.getNumber());
        }
        if (repository.existsByName(request.getName())) {
            throw new BadRequestException("Collectivity name already exists: " + request.getName());
        }

        repository.assignIdentity(id, String.valueOf(request.getNumber()), request.getName());

        Collectivity updated = repository.findById(id);
        return buildResponse(updated);
    }


    public List<CollectivityTransactionResponse> getCollectivityTransactions(
            String id,
            Instant from,
            Instant to) throws BadRequestException {

        Collectivity collectivity = repository.findById(id);
        if (collectivity == null) {
            throw new NotFoundException("Collectivity not found with id: " + id);
        }

        if (from == null || to == null) {
            throw new BadRequestException("Both 'from' and 'to' dates are required");
        }

        if (from.isAfter(to)) {
            throw new BadRequestException("'from' date must be before or equal to 'to' date");
        }

        List<Transaction> transactions = repository.findTransactionsByCollectivityIdAndDateRange(id, from, to);



        return transactions.stream()
                .map(t -> toTransactionResponse(t))
                .toList();
    }

    private CollectivityTransactionResponse toTransactionResponse(Transaction transaction) {
        return CollectivityTransactionResponse.builder()
                .id(String.valueOf(transaction.getId()))
                .creationDate(transaction.getTransactionDate())
                .amount(transaction.getAmount())
                .paymentMode(transaction.getPaymentMode())
                .accountCredited(toFinancialAccountResponse(transaction.getAccount(),transaction.getTransactionDate()))
                .memberDebited(toMemberResponse(transaction.getMember()))
                .build();
    }

    private FinancialAccountResponse toFinancialAccountResponse(Account account,Instant at) {

        Double balance = account.getBalance(at);

        if (account.getBankAccount() != null) {
            BankAccount ba = account.getBankAccount();
            BankAccountResponse response = BankAccountResponse.builder()
                    .holderName(ba.getHolderName())
                    .bankName(ba.getBankName())
                    .bankCode(ba.getBankCode())
                    .bankBranchCode(ba.getBranchCode())
                    .bankAccountNumber(ba.getAccountNumber())
                    .bankAccountKey(ba.getRibKey())
                    .build();
            response.setId(String.valueOf(account.getId()));
            response.setAmount(balance);
            return response;

        } else if (account.getMobileMoneyAccount() != null) {
            MobileMoneyAccount ma = account.getMobileMoneyAccount();
            MobileBankingAccountResponse response = MobileBankingAccountResponse.builder()
                    .holderName(ma.getHolderName())
                    .mobileBankingService(ma.getServiceName())
                    .mobileNumber(ma.getPhoneNumber())
                    .build();
            response.setId(String.valueOf(account.getId()));
            response.setAmount(balance);
            return response;

        } else if (account.getCashAccount() != null) {
            CashAccountResponse response = CashAccountResponse.builder().build();
            response.setId(String.valueOf(account.getId()));
            response.setAmount(balance);
            return response;
        }

        return null;
    }

    private MemberResponse toMemberResponse(Member member) {
        if (member == null) return null;

        return MemberResponse.builder()
                .id(member.getId())
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .birthDate(member.getBirthDate())
                .gender(member.getGender())
                .address(member.getAddress())
                .profession(member.getProfession())
                .phoneNumber(Integer.parseInt(member.getPhoneNumber()))
                .email(member.getEmail())
                .referees(member.getRefereesId())
                .build();
    }

    private List<MemberResponse> toMemberResponseList(List<Member> members) {
        if (members == null) return new ArrayList<>();

        return members.stream()
                .map(this::toMemberResponse)
                .collect(Collectors.toList());
    }

    private StructureResponse toStructureResponse(Structure structure) {
        if (structure == null) return null;

        return StructureResponse.builder()
                .president(toMemberResponse(structure.getPresident()))
                .vicePresident(toMemberResponse(structure.getVicePresident()))
                .treasurer(toMemberResponse(structure.getTreasurer()))
                .secretary(toMemberResponse(structure.getSecretary()))
                .build();
    }

    private CollectivityResponse buildResponse(Collectivity collectivity) {
        return CollectivityResponse.builder()
                .id(String.valueOf(collectivity.getId()))
                .number(collectivity.getNumber())
                .name(collectivity.getName())
                .location(collectivity.getLocation())
                .structure(toStructureResponse(collectivity.getStructure()))
                .members(toMemberResponseList(collectivity.getMembers()))
                .build();
    }

    public List<MembershipFeeResponse> getMembershipFees(String collectivityId) {
        Collectivity collectivity = repository.findById(collectivityId);
        if (collectivity == null) {
            throw new NotFoundException("Collectivity not found with id: " + collectivityId);
        }

        return membershipFeeRepository.findByCollectivityId(collectivityId).stream()
                .map(mf -> MembershipFeeResponse.builder()
                        .id(mf.getId())
                        .eligibleFrom(mf.getEligibleFrom())
                        .frequency(mf.getFrequency())
                        .amount(mf.getAmount())
                        .label(mf.getLabel())
                        .status(mf.getStatus())
                        .build())
                .toList();
    }

    public List<MembershipFeeResponse> createMembershipFees(String collectivityId, List<CreateMembershipFee> createMembershipFees) {
        Collectivity collectivity = repository.findById(collectivityId);
        if (collectivity == null) {
            throw new NotFoundException("Collectivity not found with id: " + collectivityId);
        }

        List<MembershipFee> savedMembershipFees = new ArrayList<>();
        for (CreateMembershipFee createFee : createMembershipFees) {
            if (createFee.getFrequency() == null) {
                throw new BadRequestException("Frequency is required");
            }
            if (createFee.getAmount() == null || createFee.getAmount() < 0) {
                throw new BadRequestException("Amount must be greater than or equal to 0");
            }

            MembershipFee membershipFee = MembershipFee.builder()
                    .eligibleFrom(createFee.getEligibleFrom())
                    .frequency(createFee.getFrequency())
                    .amount(createFee.getAmount())
                    .label(createFee.getLabel())
                    .build();

            MembershipFee saved = membershipFeeRepository.save(membershipFee, collectivityId);
            savedMembershipFees.add(saved);
        }

        return savedMembershipFees.stream()
                .map(mf -> MembershipFeeResponse.builder()
                        .id(mf.getId())
                        .eligibleFrom(mf.getEligibleFrom())
                        .frequency(mf.getFrequency())
                        .amount(mf.getAmount())
                        .label(mf.getLabel())
                        .status(mf.getStatus())
                        .build())
                .toList();
    }

    public CollectivityResponse getCollectivityById(String id) {
        return buildResponse(repository.findByIdOptional(id).orElseThrow(()->new NotFoundException("Collectivity id=" + id + " not found")));
    }

    public CollectivityFinancialAccountResponse getFinancialAccounts(String collectivityId, LocalDate at) {
        Collectivity collectivity = repository.findById(collectivityId);
        if (collectivity == null) {
            throw new NotFoundException("Collectivity not found with id: " + collectivityId);
        }

        Map<String, Account> accounts = at != null
            ? repository.loadAccountsWithTransactionsAt(collectivityId, at)
            : repository.loadAccountsWithAllTransactions(collectivityId);

        CollectivityFinancialAccountResponse response = CollectivityFinancialAccountResponse.builder()
                .id(collectivityId)
                .amount(accounts.values().stream().mapToDouble(Account::getBalance).sum())
                .accounts(new ArrayList<>())
                .build();

        for (Account account : accounts.values()) {
            Object accountDetail = toAccountDetail(account);
            if (accountDetail != null) {
                response.getAccounts().add(accountDetail);
            }
        }

        return response;
    }

    private Object toAccountDetail(Account account) {
        Double balance = account.getBalance();

        if (account.getCashAccount() != null) {
            return CashAccountDetail.builder()
                    .id(String.valueOf(account.getId()))
                    .amount(balance.intValue())
                    .type("CASH")
                    .build();
        } else if (account.getBankAccount() != null) {
            BankAccount ba = account.getBankAccount();
            return BankAccountDetail.builder()
                    .id(String.valueOf(account.getId()))
                    .amount(balance)
                    .type("BANK")
                    .holderName(ba.getHolderName())
                    .bankName(ba.getBankName())
                    .bankCode(ba.getBankCode())
                    .bankBranchCode(ba.getBranchCode())
                    .bankAccountNumber(ba.getAccountNumber())
                    .bankAccountKey(ba.getRibKey())
                    .build();
        } else if (account.getMobileMoneyAccount() != null) {
            MobileMoneyAccount ma = account.getMobileMoneyAccount();
            return MobileBankingAccountDetail.builder()
                    .id(String.valueOf(account.getId()))
                    .amount(balance)
                    .type("MOBILE_BANKING")
                    .holderName(ma.getHolderName())
                    .mobileBankingService(ma.getServiceName())
                    .mobileNumber(ma.getPhoneNumber())
                    .build();
        }

        return null;
    }

    public List<CollectivityLocalStatistics> getLocalStatistics(String collectivityId, LocalDate from, LocalDate to) {
        Collectivity collectivity = repository.findById(collectivityId);
        if (collectivity == null) {
            throw new NotFoundException("Collectivity not found with id: " + collectivityId);
        }

        List<Member> members = collectivity.getMembers();
        List<CollectivityLocalStatistics> statistics = new ArrayList<>();

        for (Member member : members) {
            MemberDescription memberDesc = MemberDescription.builder()
                .id(member.getId())
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .email(member.getEmail())
                .occupation(member.getProfession())
                .build();

            CollectivityLocalStatistics stats = CollectivityLocalStatistics.builder()
                .memberDescription(memberDesc)
                .earnedAmount(0)
                .unpaidAmount(0)
                .build();
            statistics.add(stats);
        }

        for (CollectivityLocalStatistics stats : statistics) {
            Double assiduity = calculateAssiduity(stats.getMemberDescription().getId(), collectivityId, from, to);
            stats.setAssiduityPercentage(assiduity);
        }

        return statistics;
    }

    public List<CollectivityOverallStatistics> getOverallStatistics(LocalDate from, LocalDate to) {
        List<Collectivity> collectivities = repository.findAll();
        List<CollectivityOverallStatistics> statistics = new ArrayList<>();

        for (Collectivity collectivity : collectivities) {
            CollectivityInformation info = CollectivityInformation.builder()
                .id(collectivity.getId())
                .number(collectivity.getNumber())
                .name(collectivity.getName())
                .location(collectivity.getLocation())
                .build();

            Double overallAssiduity = calculateOverallAssiduity(collectivity.getId(), from, to);

            CollectivityOverallStatistics stats = CollectivityOverallStatistics.builder()
                .collectivityInformation(info)
                .newMembersNumber(0)
                .overallMemberCurrentDuePercentage(0.0)
                .overallMemberAssiduityPercentage(overallAssiduity)
                .build();
            statistics.add(stats);
        }

        return statistics;
    }

    private Double calculateAssiduity(String memberId, String collectivityId, LocalDate from, LocalDate to) {
        List<Activity> requiredActivities = activityRepository.findByCollectivityId(collectivityId).stream()
            .filter(a -> isMemberRequired(a, memberId))
            .filter(a -> isInDateRange(a, from, to))
            .toList();

        if (requiredActivities.isEmpty()) return 0.0;

        long totalRequired = requiredActivities.size();
        long attended = requiredActivities.stream()
            .mapToLong(a -> activityRepository.findAttendanceByActivityId(a.getId()).stream()
                .filter(att -> att.getMemberId().equals(memberId))
                .filter(att -> att.getAttendanceStatus() == AttendanceStatus.ATTENDED)
                .count())
            .sum();

        return (attended * 100.0) / totalRequired;
    }

    private Double calculateOverallAssiduity(String collectivityId, LocalDate from, LocalDate to) {
        List<CollectivityLocalStatistics> localStats = getLocalStatistics(collectivityId, from, to);
        if (localStats.isEmpty()) return 0.0;

        return localStats.stream()
            .mapToDouble(CollectivityLocalStatistics::getAssiduityPercentage)
            .average()
            .orElse(0.0);
    }

    private boolean isMemberRequired(Activity activity, String memberId) {
        if (activity.getMemberOccupationConcerned() == null || activity.getMemberOccupationConcerned().isEmpty()) {
            return true;
        }
        return true;
    }

    private boolean isInDateRange(Activity activity, LocalDate from, LocalDate to) {
        if (activity.getExecutiveDate() == null) return false;
        return !activity.getExecutiveDate().isBefore(from) && !activity.getExecutiveDate().isAfter(to);
    }
}