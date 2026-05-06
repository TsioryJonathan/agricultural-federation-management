package com.hei.agriculturalfederationmanagement.service;

import com.hei.agriculturalfederationmanagement.entity.*;
import com.hei.agriculturalfederationmanagement.entity.dto.*;
import com.hei.agriculturalfederationmanagement.entity.enums.ActivityStatus;
import com.hei.agriculturalfederationmanagement.entity.enums.PaymentMode;
import com.hei.agriculturalfederationmanagement.entity.enums.TransactionType;
import com.hei.agriculturalfederationmanagement.exception.BadRequestException;
import com.hei.agriculturalfederationmanagement.exception.NotFoundException;
import com.hei.agriculturalfederationmanagement.mapper.CollectivityMapper;
import com.hei.agriculturalfederationmanagement.repository.*;
import com.hei.agriculturalfederationmanagement.validator.MemberValidator;
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
public class MemberService {
    private final MemberRepository memberRepository;
    private final CollectivityRepository collectivityRepository;
    private final CotisationPlanRepository cotisationPlanRepository;
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final CollectivityMapper mapper;
    private final MemberValidator validator;

    public List<MemberResponse> createMembers(List<CreateMember> createMembers) {
        List<MemberResponse> responses = new ArrayList<>();

        for (CreateMember dto : createMembers) {
            // Validate the member creation request
            validator.validate(dto);

            // Check if collectivity exists
            Collectivity collectivity = collectivityRepository.findById(dto.getCollectivityIdentifier());
            if (collectivity == null) {
                throw new NotFoundException("Collectivity not found with id: " + dto.getCollectivityIdentifier());
            }

            // Create member entity
            Member member = Member.builder()
                    .firstName(dto.getFirstName())
                    .lastName(dto.getLastName())
                    .birthDate(dto.getBirthDate())
                    .gender(dto.getGender())
                    .address(dto.getAddress())
                    .profession(dto.getProfession())
                    .phoneNumber(dto.getPhoneNumber())
                    .email(dto.getEmail())
                    .enrolmentDate(LocalDate.now())
                    .isSuperuser(false)
                    .referees(new ArrayList<>())
                    .build();

            // Save member
            Member savedMember = memberRepository.save(member);

            // Add referees
            if (dto.getReferees() != null) {
                for (String refereeId : dto.getReferees()) {
                    memberRepository.addReferee(savedMember.getId(), refereeId, "Parrainage");
                }
            }

            // Add member to collectivity with occupation
            String occupation = dto.getOccupation() != null ? dto.getOccupation().name() : "JUNIOR";
            memberRepository.addToCollectivity(savedMember.getId(), dto.getCollectivityIdentifier(), occupation);

            // Handle registration fee payment (50,000 MGA)
            if (dto.isRegistrationFeePaid()) {
                createMemberPaymentTransaction(savedMember.getId(), dto.getCollectivityIdentifier(),
                        50000.0, PaymentMode.MOBILE_BANKING, "Registration fee");
            }

            // Handle membership dues payment
            if (dto.isMembershipDuesPaid()) {
                List<CotisationPlan> activePlans = cotisationPlanRepository
                        .findByCollectivityId(dto.getCollectivityIdentifier())
                        .stream()
                        .filter(plan -> plan.getStatus() == ActivityStatus.ACTIVE)
                        .toList();

                for (CotisationPlan plan : activePlans) {
                    createMemberPaymentTransaction(savedMember.getId(), dto.getCollectivityIdentifier(),
                            plan.getAmount(), PaymentMode.MOBILE_BANKING,
                            "Membership dues: " + plan.getLabel());
                }
            }

            // Fetch the member with referees for response
            Member memberWithReferees = memberRepository.findById(savedMember.getId())
                    .orElseThrow(() -> new NotFoundException("Member not found after creation"));

            responses.add(mapper.toMemberResponse(memberWithReferees));
        }

        return responses;
    }

    public List<MemberPaymentResponse> createPayments(String memberId, List<CreateMemberPayment> requests) {
        // Verify member exists
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found with id: " + memberId));

        List<MemberPaymentResponse> responses = new ArrayList<>();

        for (CreateMemberPayment request : requests) {
            // Validate payment request
            if (request.getAmount() == null || request.getAmount() <= 0) {
                throw new BadRequestException("Payment amount must be greater than 0");
            }
            if (request.getPaymentMode() == null) {
                throw new BadRequestException("Payment mode is required");
            }

            // Verify membership fee exists if provided
            if (request.getMembershipFeeIdentifier() != null) {
                cotisationPlanRepository.findById(request.getMembershipFeeIdentifier())
                        .orElseThrow(() -> new NotFoundException(
                                "Membership fee not found with id: " + request.getMembershipFeeIdentifier()));
            }

            // Verify account exists if provided
            if (request.getAccountCreditedIdentifier() != null) {
                accountRepository.findById(request.getAccountCreditedIdentifier())
                        .orElseThrow(() -> new NotFoundException(
                                "Account not found with id: " + request.getAccountCreditedIdentifier()));
            }

            // Get member's collectivity
            String collectivityId = memberRepository.findCollectivityIdByMemberId(memberId);
            if (collectivityId == null) {
                throw new NotFoundException("Member is not assigned to any collectivity");
            }

            // Create transaction for payment
            Transaction transaction = createMemberPaymentTransaction(
                    memberId, collectivityId, Double.valueOf(request.getAmount()),
                    request.getPaymentMode(), "Member payment");

            // Build response
            MemberPaymentResponse response = mapper.toMemberPaymentResponse(transaction);
            responses.add(response);
        }

        return responses;
    }

    private Transaction createMemberPaymentTransaction(String memberId, String collectivityId,
                                                       Double amount, PaymentMode paymentMode, String description) {
        // Find a valid account for the collectivity
        Account account = findCollectivityCashAccount(collectivityId);

        Transaction transaction = Transaction.builder()
                .collectivity(Collectivity.builder().id(collectivityId).build())
                .member(Member.builder().id(memberId).build())
                .transactionType(TransactionType.IN)
                .amount(amount)
                .transactionDate(LocalDate.now())
                .paymentMode(paymentMode)
                .description(description)
                .account(account)
                .build();

        return transactionRepository.save(transaction);
    }

    private Account findCollectivityCashAccount(String collectivityId) {
        // Find the cash account for the collectivity
        Map<String, Account> accounts = collectivityRepository.loadAccountsWithTransactions(collectivityId, null);

        return accounts.values().stream()
                .filter(account -> account.getCashAccount() != null)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No cash account found for collectivity: " + collectivityId));
    }
}