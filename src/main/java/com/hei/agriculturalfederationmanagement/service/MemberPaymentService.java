package com.hei.agriculturalfederationmanagement.service;

import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.Transaction;
import com.hei.agriculturalfederationmanagement.entity.dto.FinancialAccountResponse;
import com.hei.agriculturalfederationmanagement.entity.dto.MemberPaymentResponse;
import com.hei.agriculturalfederationmanagement.entity.enums.TransactionType;
import com.hei.agriculturalfederationmanagement.exception.BadRequestException;
import com.hei.agriculturalfederationmanagement.exception.NotFoundException;
import com.hei.agriculturalfederationmanagement.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class MemberPaymentService {
    private final MemberRepository memberRepository;
    private final TransactionRepository transactionRepository;
    private final CotisationPlanRepository cotisationPlanRepository;
    private final AccountRepository accountRepository;
    private final CollectivityRepository collectivityRepository;

    public List<MemberPaymentResponse> createPayments(Integer memberId, List<CreateMemberPaymentRequest> requests) {
        // 1. Validate member exists
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found with id: " + memberId));

        // 2. Get member's active collectivity
        Integer collectivityId = memberRepository.findActiveCollectivityIdByMemberId(memberId);
        if (collectivityId == null) {
            throw new BadRequestException("Member is not assigned to any active collectivity");
        }

        List<MemberPaymentResponse> responses = new ArrayList<>();

        for (CreateMemberPaymentRequest request : requests) {
            // 3. Validate request
            validatePaymentRequest(request, collectivityId);

            // 4. Create transaction
            Transaction transaction = Transaction.builder()
                    .idMember(memberId)
                    .idCollectivity(collectivityId)
                    .idCotisationPlan(request.getMembershipFeeIdentifier() != null ?
                            Integer.parseInt(request.getMembershipFeeIdentifier()) : null)
                    .idAccount(Integer.parseInt(request.getAccountCreditedIdentifier()))
                    .transactionType(TransactionType.IN)
                    .amount(request.getAmount())
                    .transactionDate(Instant.now())
                    .paymentMode(request.getPaymentMode())
                    .description(buildDescription(request, member))
                    .build();

            // 5. Save transaction
            transaction = transactionRepository.save(transaction);

            // 6. Build response with account details
            MemberPaymentResponse response = buildPaymentResponse(transaction);
            responses.add(response);
        }

        return responses;
    }

    private void validatePaymentRequest(CreateMemberPaymentRequest request, Integer collectivityId) {
        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new BadRequestException("Amount must be greater than 0");
        }

        if (request.getPaymentMode() == null) {
            throw new BadRequestException("Payment mode is required");
        }

        if (request.getAccountCreditedIdentifier() == null) {
            throw new BadRequestException("Account credited identifier is required");
        }

        // Validate account exists and belongs to the collectivity
        Integer accountId = Integer.parseInt(request.getAccountCreditedIdentifier());
        if (!accountRepository.existsByIdAndCollectivityId(accountId, collectivityId)) {
            throw new BadRequestException("Account not found or does not belong to the member's collectivity");
        }

        // Validate membership fee if provided
        if (request.getMembershipFeeIdentifier() != null) {
            Integer feeId = Integer.parseInt(request.getMembershipFeeIdentifier());
            var fee = cotisationPlanRepository.findById(feeId)
                    .orElseThrow(() -> new NotFoundException("Membership fee not found with id: " + feeId));

            if (!fee.getIdCollectivity().equals(collectivityId)) {
                throw new BadRequestException("Membership fee does not belong to the member's collectivity");
            }

            if (!fee.getIsActive()) {
                throw new BadRequestException("Membership fee is not active");
            }
        }
    }

    private String buildDescription(CreateMemberPaymentRequest request, Member member) {
        StringBuilder description = new StringBuilder();
        description.append("Paiement - ").append(member.getFirstName()).append(" ").append(member.getLastName());

        if (request.getMembershipFeeIdentifier() != null) {
            var fee = cotisationPlanRepository.findById(Integer.parseInt(request.getMembershipFeeIdentifier()));
            fee.ifPresent(f -> description.append(" - ").append(f.getLabel()));
        } else {
            description.append(" - Frais d'adhésion");
        }

        return description.toString();
    }

    private MemberPaymentResponse buildPaymentResponse(Transaction transaction) {
        // Load account with full details
        var account = collectivityRepository.findAccountById(transaction.getIdAccount());

        return MemberPaymentResponse.builder()
                .id(String.valueOf(transaction.getId()))
                .amount(transaction.getAmount())
                .paymentMode(transaction.getPaymentMode())
                .accountCredited(toFinancialAccountResponse(account))
                .creationDate(transaction.getTransactionDate())
                .build();
    }

    private FinancialAccountResponse toFinancialAccountResponse(com.hei.agriculturalfederationmanagement.entity.Account account) {
        if (account == null) return null;

        Double balance = account.getBalance();

        if (account.getBankAccount() != null) {
            var ba = account.getBankAccount();
            var response = com.hei.agriculturalfederationmanagement.entity.dto.BankAccountResponse.builder()
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
            var ma = account.getMobileMoneyAccount();
            var response = com.hei.agriculturalfederationmanagement.entity.dto.MobileBankingAccountResponse.builder()
                    .holderName(ma.getHolderName())
                    .mobileBankingService(ma.getServiceName())
                    .mobileNumber(ma.getPhoneNumber())
                    .build();
            response.setId(String.valueOf(account.getId()));
            response.setAmount(balance);
            return response;
        } else if (account.getCashAccount() != null) {
            var response = com.hei.agriculturalfederationmanagement.entity.dto.CashAccountResponse.builder().build();
            response.setId(String.valueOf(account.getId()));
            response.setAmount(balance);
            return response;
        }

        return null;
    }
}