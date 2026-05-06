package com.hei.agriculturalfederationmanagement.mapper;

import com.hei.agriculturalfederationmanagement.entity.*;
import com.hei.agriculturalfederationmanagement.entity.dto.*;
import com.hei.agriculturalfederationmanagement.entity.enums.MemberOccupation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class Mapper {

    public CollectivityResponse toResponse(Collectivity collectivity) {
        if (collectivity == null) return null;

        return CollectivityResponse.builder()
                .id(collectivity.getId())
                .number(collectivity.getNumber())
                .name(collectivity.getName())
                .location(collectivity.getLocation())
                .structure(toStructureResponse(collectivity.getStructure()))
                .members(toMemberResponseList(collectivity.getMembers()))
                .build();
    }
    public MemberResponse toMemberResponse(Member member, Map<String, String> occupations) {
        String occupation = occupations != null ? occupations.get(member.getId()) : null;

        return MemberResponse.builder()
                .id(member.getId())
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .birthDate(member.getBirthDate())
                .gender(member.getGender())
                .address(member.getAddress())
                .profession(member.getProfession())
                .phoneNumber(member.getPhoneNumber())
                .email(member.getEmail())
                .occupation(occupation != null ? MemberOccupation.valueOf(occupation) : null)
                .referees(member.getReferees() != null
                        ? member.getReferees().stream()
                          .map(this::toMemberSummary)
                          .toList()
                        : List.of())
                .build();
    }

    public CollectivityResponse toResponse(Collectivity collectivity, Map<String, String> occupations) {
        List<MemberResponse> memberResponses = collectivity.getMembers().stream()
                .map(member -> toMemberResponse(member, occupations))
                .toList();

        return CollectivityResponse.builder()
                .id(collectivity.getId())
                .number(collectivity.getNumber())
                .name(collectivity.getName())
                .location(collectivity.getLocation())
                .structure(CollectivityStructureResponse.builder()
                        .president(toMemberResponse(collectivity.getStructure().getPresident(), occupations))
                        .vicePresident(toMemberResponse(collectivity.getStructure().getVicePresident(), occupations))
                        .treasurer(toMemberResponse(collectivity.getStructure().getTreasurer(), occupations))
                        .secretary(toMemberResponse(collectivity.getStructure().getSecretary(), occupations))
                        .build())
                .members(memberResponses)
                .build();
    }

    public CollectivityStructureResponse toStructureResponse(Structure structure) {
        if (structure == null) return null;

        return CollectivityStructureResponse.builder()
                .president(toMemberResponse(structure.getPresident()))
                .vicePresident(toMemberResponse(structure.getVicePresident()))
                .treasurer(toMemberResponse(structure.getTreasurer()))
                .secretary(toMemberResponse(structure.getSecretary()))
                .build();
    }

    public MemberResponse toMemberResponse(Member member) {
        if (member == null) {
            return null;
        }
        return MemberResponse.builder()
                .id(member.getId())
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .birthDate(member.getBirthDate())
                .gender(member.getGender())
                .address(member.getAddress())
                .profession(member.getProfession())
                .phoneNumber(member.getPhoneNumber())
                .email(member.getEmail())
                .referees(member.getReferees() != null
                        ? member.getReferees().stream()
                          .map(this::toMemberSummary)
                          .toList()
                        : List.of())
                .build();
    }
    public List<MemberResponse> toMemberResponseList(List<Member> members) {
        if (members == null) return new ArrayList<>();
        return members.stream()
                .map(this::toMemberResponse)
                .collect(Collectors.toList());
    }

    public MemberDescription toMemberDescription(Member member) {
        if (member == null) return null;

        return MemberDescription.builder()
                .id(member.getId())
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .email(member.getEmail())
                .occupation(null) // Occupation is set separately when needed
                .build();
    }

    public FinancialAccountResponse toFinancialAccountResponse(Account account) {
        if (account == null) return null;

        Double balance = account.getBalance();

        if (account.getCashAccount() != null) {
            CashAccountResponse response = new CashAccountResponse();
            response.setId(account.getId());
            response.setAmount(balance);
            return response;

        } else if (account.getBankAccount() != null) {
            BankAccount ba = account.getBankAccount();
            return BankAccountResponse.builder()
                    .id(account.getId())
                    .amount(balance)
                    .holderName(ba.getHolderName())
                    .bankName(ba.getBankName())
                    .bankCode(ba.getBankCode() != null ? Integer.parseInt(ba.getBankCode()) : null)
                    .bankBranchCode(ba.getBranchCode() != null ? Integer.parseInt(ba.getBranchCode()) : null)
                    .bankAccountNumber(ba.getAccountNumber() != null ? Integer.parseInt(ba.getAccountNumber()) : null)
                    .bankAccountKey(ba.getRibKey() != null ? Integer.parseInt(ba.getRibKey()) : null)
                    .build();

        } else if (account.getMobileMoneyAccount() != null) {
            MobileMoneyAccount ma = account.getMobileMoneyAccount();
            return MobileBankingAccountResponse.builder()
                    .id(account.getId())
                    .amount(balance)
                    .holderName(ma.getHolderName())
                    .mobileBankingService(ma.getServiceName())
                    .mobileNumber(ma.getPhoneNumber() != null ? ma.getPhoneNumber() : null)
                    .build();
        }

        return null;
    }

    public Object toAccountDetail(Account account) {
        if (account == null) return null;

        Double balance = account.getBalance();

        if (account.getCashAccount() != null) {
            return CashAccountDetail.builder()
                    .id(account.getId())
                    .amount(balance)
                    .build();

        } else if (account.getBankAccount() != null) {
            BankAccount ba = account.getBankAccount();
            return BankAccountDetail.builder()
                    .id(account.getId())
                    .amount(balance)
                    .holderName(ba.getHolderName())
                    .bankName(ba.getBankName())
                    .bankCode(ba.getBankCode() != null ? Integer.parseInt(ba.getBankCode()) : null)
                    .bankBranchCode(ba.getBranchCode() != null ? Integer.parseInt(ba.getBranchCode()) : null)
                    .bankAccountNumber(ba.getAccountNumber() != null ? Integer.parseInt(ba.getAccountNumber()) : null)
                    .bankAccountKey(ba.getRibKey() != null ? Integer.parseInt(ba.getRibKey()) : null)
                    .build();

        } else if (account.getMobileMoneyAccount() != null) {
            MobileMoneyAccount ma = account.getMobileMoneyAccount();
            return MobileBankingAccountDetail.builder()
                    .id(account.getId())
                    .amount(balance)
                    .holderName(ma.getHolderName())
                    .mobileBankingService(ma.getServiceName())
                    .mobileNumber(ma.getPhoneNumber() != null ? ma.getPhoneNumber() : null)
                    .build();
        }

        return null;
    }

    public CollectivityTransactionResponse toTransactionResponse(Transaction transaction,Map<String,String> occupations) {
        if (transaction == null) return null;

        return CollectivityTransactionResponse.builder()
                .id(transaction.getId())
                .creationDate(transaction.getTransactionDate())
                .amount(transaction.getAmount())
                .paymentMode(transaction.getPaymentMode())
                .accountCredited(toFinancialAccountResponse(transaction.getAccount()))
                .memberDebited(toMemberResponse(transaction.getMember(),occupations))
                .build();
    }

    public MemberPaymentResponse toMemberPaymentResponse(Transaction transaction) {
        if (transaction == null) return null;

        return MemberPaymentResponse.builder()
                .id(transaction.getId())
                .amount(transaction.getAmount() != null ? transaction.getAmount().intValue() : null)
                .paymentMode(transaction.getPaymentMode())
                .accountCredited(toFinancialAccountResponse(transaction.getAccount()))
                .creationDate(transaction.getTransactionDate())
                .build();
    }
    public MemberSummary toMemberSummary(Member member) {
        if (member == null) {
            return null;
        }
        return MemberSummary.builder()
                .id(member.getId())
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .email(member.getEmail())
                .phoneNumber(member.getPhoneNumber())
                .gender(member.getGender() != null ? member.getGender().name() : null)
                .referees(member.getReferees() == null ? List.of() : member.getReferees().stream().map(Member::getId).toList())
                .build();
    }
}