package com.hei.agriculturalfederationmanagement.service;

import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.Transaction;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateMemberPayment;
import com.hei.agriculturalfederationmanagement.entity.dto.FinancialAccountResponse;
import com.hei.agriculturalfederationmanagement.entity.dto.MemberPaymentResponse;
import com.hei.agriculturalfederationmanagement.entity.enums.TransactionType;
import com.hei.agriculturalfederationmanagement.exception.BadRequestException;
import com.hei.agriculturalfederationmanagement.exception.NotFoundException;
import com.hei.agriculturalfederationmanagement.repository.*;
import com.hei.agriculturalfederationmanagement.validator.MemberPaymentValidator;
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
    private final MemberPaymentValidator validator;
    private final AccountRepository accountRepository;
    private final CollectivityRepository collectivityRepository;

    public List<MemberPaymentResponse> createPayments(Integer memberId, List<CreateMemberPayment> requests) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found with id: " + memberId));

        Integer collectivityId = memberRepository.findActiveCollectivityIdByMemberId(memberId);
        if (collectivityId == null) {
            throw new BadRequestException("Member is not assigned to any active collectivity");
        }

        List<MemberPaymentResponse> responses = new ArrayList<>();

        for (CreateMemberPayment request : requests) {
            validator.validatePaymentRequest(request, collectivityId);

            Transaction transaction = Transaction.builder()
                    .transactionType(TransactionType.IN)
                    .amount(request.getAmount())
                    .transactionDate(Instant.now())
                    .member(memberRepository.findById(memberId).orElseThrow(()-> new NotFoundException("Member not found")))
                    .paymentMode(request.getPaymentMode())
                    .description(buildDescription(request, member))
                    .collectivity(collectivityRepository.findByMembershipFeeId(request.getMembershipFeeIdentifier()))
                    .account(accountRepository.findById(request.getAccountCreditedIdentifier()).orElseThrow(()-> new NotFoundException("Account not found")))
                    .build();

            transaction = transactionRepository.save(transaction);

            MemberPaymentResponse response = buildPaymentResponse(transaction);
            responses.add(response);
        }

        return responses;
    }



    private String buildDescription(CreateMemberPayment request, Member member) {
        StringBuilder description = new StringBuilder();
        description.append("Paiement - ").append(member.getFirstName()).append(" ").append(member.getLastName());

        if (request.getMembershipFeeIdentifier() != null) {
            var fee = cotisationPlanRepository.findById((request.getMembershipFeeIdentifier()));
            fee.ifPresent(f -> description.append(" - ").append(f.getLabel()));
        } else {
            description.append(" - Frais d'adhésion");
        }

        return description.toString();
    }

    private MemberPaymentResponse buildPaymentResponse(Transaction transaction) {
        var account = collectivityRepository.findAccountById(transaction.getAccount().getId());

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