package com.hei.agriculturalfederationmanagement.service;

import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateMember;
import com.hei.agriculturalfederationmanagement.entity.dto.MemberResponse;
import com.hei.agriculturalfederationmanagement.repository.MemberRepository;
import com.hei.agriculturalfederationmanagement.validator.CollectivityRuleValidator;
import com.hei.agriculturalfederationmanagement.validator.PaymentValidator;
import com.hei.agriculturalfederationmanagement.validator.SponsorCountValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.IntStream;

@Service
@AllArgsConstructor
public class MemberService {

    private final MemberRepository repository;
    private final PaymentValidator paymentValidator;
    private final CollectivityRuleValidator collectivityRuleValidator;
    private final SponsorCountValidator sponsorCountValidator;

    public List<MemberResponse> createMembers(List<CreateMember> memberList) {

        // ================= VALIDATION =================
        memberList.forEach(paymentValidator::validate);
        memberList.forEach(sponsorCountValidator::validate);

        List<Integer> sponsorIds = memberList.stream()
                .flatMap(m -> m.getReferees().stream())
                .distinct()
                .toList();

        List<Member> sponsors = repository.findByIds(sponsorIds);

        memberList.forEach(m ->
                collectivityRuleValidator.validate(m, sponsors)
        );

        // ================= MAPPING DTO → ENTITY =================
        List<Member> members = memberList.stream()
                .map(this::toEntity)
                .toList();

        // ================= SAVE =================
        List<Member> saved = repository.saveAll(members, memberList);

        // ================= RESPONSE =================
        return buildResponse(saved, memberList);
    }

    // ---------------- DTO → ENTITY ----------------
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

    // ---------------- RESPONSE BUILDER ----------------
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
}