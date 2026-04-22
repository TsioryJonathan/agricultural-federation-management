package com.hei.agriculturalfederationmanagement.service;

import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.MemberRefereeLink;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateMember;
import com.hei.agriculturalfederationmanagement.entity.dto.MemberResponse;
import com.hei.agriculturalfederationmanagement.repository.MemberCollectivityRepository;
import com.hei.agriculturalfederationmanagement.repository.MemberRepository;
import com.hei.agriculturalfederationmanagement.repository.RefereeRepository;
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

    private final MemberRepository memberRepository;
    private final MemberCollectivityRepository memberCollectivityRepository;
    private final RefereeRepository refereeRepository;
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

        List<Member> sponsors = memberRepository.findByIds(sponsorIds);

        memberList.forEach(m ->
                collectivityRuleValidator.validate(m, sponsors)
        );

        List<Member> members = memberList.stream()
                .map(this::toEntity)
                .toList();

        // ================= SAVE MEMBERS =================
        List<Member> savedMembers = memberRepository.saveAll(members, memberList);

        // ================= SAVE MEMBER_COLLECTIVITY LINKS =================
        for (int i = 0; i < savedMembers.size(); i++) {
            Member saved = savedMembers.get(i);
            CreateMember dto = memberList.get(i);
            memberCollectivityRepository.saveMemberCollectivityLink(
                    saved.getId(),
                    dto.getCollectivityIdentifier(),
                    dto.getOccupation()
            );
        }

        // ================= SAVE REFEREES =================
        List<MemberRefereeLink> refereeLinks = new java.util.ArrayList<>();
        for (int i = 0; i < savedMembers.size(); i++) {
            Member saved = savedMembers.get(i);
            CreateMember dto = memberList.get(i);
            if (dto.getReferees() != null) {
                for (Integer refId : dto.getReferees()) {
                    refereeLinks.add(MemberRefereeLink.builder()
                            .idMember(saved.getId())
                            .idReferee(refId)
                            .idCollectivity(dto.getCollectivityIdentifier())
                            .link("FRIEND")
                            .build());
                }
            }
        }
        if (!refereeLinks.isEmpty()) {
            refereeRepository.saveRefereeMemberLink(refereeLinks);
        }

        return buildResponse(savedMembers, memberList);
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