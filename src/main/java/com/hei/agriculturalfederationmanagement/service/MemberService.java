package com.hei.agriculturalfederationmanagement.service;

import com.hei.agriculturalfederationmanagement.entity.*;
import com.hei.agriculturalfederationmanagement.entity.dto.*;
import com.hei.agriculturalfederationmanagement.entity.enums.TransactionType;
import com.hei.agriculturalfederationmanagement.exception.BadRequestException;
import com.hei.agriculturalfederationmanagement.exception.NotFoundException;
import com.hei.agriculturalfederationmanagement.repository.AccountRepository;
import com.hei.agriculturalfederationmanagement.repository.CollectivityRepository;
import com.hei.agriculturalfederationmanagement.repository.MemberRepository;
import com.hei.agriculturalfederationmanagement.repository.TransactionRepository;
import com.hei.agriculturalfederationmanagement.validator.CollectivityRuleValidator;
import com.hei.agriculturalfederationmanagement.validator.MemberPaymentValidator;
import com.hei.agriculturalfederationmanagement.validator.PaymentValidator;
import com.hei.agriculturalfederationmanagement.validator.SponsorCountValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberLinkService memberLinkService;
    private final PaymentValidator paymentValidator;
    private final CollectivityRuleValidator collectivityRuleValidator;
    private final SponsorCountValidator sponsorCountValidator;
    private final MemberPaymentValidator memberPaymentValidator;
    private final CollectivityRepository collectivityRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public List<MemberResponse> createMembers(List<CreateMember> memberList) {

        /* Validate payement */
        memberList.forEach(paymentValidator::validate);

        /* Validate Sponsor count */
        memberList.forEach(sponsorCountValidator::validate);

        List<Integer> sponsorIds = memberList.stream()
                .flatMap(m -> m.getReferees().stream())
                .distinct()
                .toList();
        List<Member> sponsors = memberRepository.findByIds(sponsorIds);
        /* Validate all sponsors by rules */
        memberList.forEach(m ->
                collectivityRuleValidator.validate(m, sponsors)
        );

        /* Convert DTO to entity*/
        List<Member> members = memberList.stream()
                .map(this::toEntity)
                .toList();

        /* Save member */
        List<Member> savedMembers = memberRepository.saveAll(members, memberList);

        /* Create link */
        memberLinkService.createMemberCollectivityLinks(savedMembers, memberList);
        memberLinkService.createRefereeLinks(savedMembers, memberList);

        return buildResponse(savedMembers, memberList);
    }

    private Member toEntity(CreateMember dto) {
        return Member.builder()
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .birthDate(dto.getBirthDate())
                .gender(dto.getGender())
                .address(dto.getAddress())
                .email(dto.getEmail())
                .phoneNumber(dto.getPhoneNumber())
                .profession(dto.getProfession())
                .build();
    }

    private List<MemberResponse> buildResponse(List<Member> saved, List<CreateMember> dtos) {

        return IntStream.range(0, saved.size())
                .mapToObj(i -> {
                    Member m = saved.get(i);
                    CreateMember dto = dtos.get(i);

                    return MemberResponse.builder()
                            .id(m.getId())
                            .firstName(m.getFirstName())
                            .lastName(m.getLastName())
                            .birthDate(m.getBirthDate())
                            .gender(m.getGender())
                            .address(m.getAddress())
                            .profession(m.getProfession())
                            .phoneNumber(m.getPhoneNumber())
                            .email(m.getEmail())
                            .referees(dto.getReferees())
                            .build();
                })
                .toList();
    }

    // transactions

    public List<MemberPaymentResponse> createPayments(Integer memberId, List<CreateMemberPayment> requests) {

        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException("Member not found with id: " + memberId));

        List<MemberPaymentResponse> responses = new ArrayList<>();


        for (CreateMemberPayment request : requests) {

            Collectivity collectivity =  collectivityRepository.findByMembershipFeeId(request.getMembershipFeeIdentifier());
            Account account = accountRepository.findById(request.getAccountCreditedIdentifier()).orElseThrow(()-> new NotFoundException("Account not found"));

            if (collectivity == null) {
                throw new BadRequestException("Collectivity with that membership fee not found");
            }

            memberPaymentValidator.validatePaymentRequest(request, collectivity.getId());

            Transaction transaction = Transaction.builder()
                    .transactionType(TransactionType.IN)
                    .amount(request.getAmount())
                    .transactionDate(Instant.now())
                    .member(member)
                    .description("-")
                    .paymentMode(request.getPaymentMode())
                    .collectivity(collectivity)
                    .account(account)
                    .build();

            Transaction savedTransaction = transactionRepository.save(transaction);

            MemberPaymentResponse response = buildPaymentResponse(savedTransaction);
            responses.add(response);
        }

        return responses;
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

    private FinancialAccountResponse toFinancialAccountResponse(Account account) {

        Double balance = account.getBalance();

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


}