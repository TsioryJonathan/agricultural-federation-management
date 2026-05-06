package com.hei.agriculturalfederationmanagement.mapper;

import com.hei.agriculturalfederationmanagement.entity.Collectivity;
import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.Structure;
import com.hei.agriculturalfederationmanagement.entity.dto.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CollectivityMapper {

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

    private CollectivityStructureResponse toStructureResponse(Structure structure) {
        if (structure == null) return null;

        return CollectivityStructureResponse.builder()
                .president(toMemberResponse(structure.getPresident()))
                .vicePresident(toMemberResponse(structure.getVicePresident()))
                .treasurer(toMemberResponse(structure.getTreasurer()))
                .secretary(toMemberResponse(structure.getSecretary()))
                .build();
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
                .phoneNumber(member.getPhoneNumber())
                .email(member.getEmail())
                .referees(member.getReferees() != null ?
                        member.getReferees().stream()
                        .map(this::toMemberResponse)
                        .collect(Collectors.toList()) :
                        new ArrayList<>())
                .build();
    }

    private List<MemberResponse> toMemberResponseList(List<Member> members) {
        if (members == null) return new ArrayList<>();
        return members.stream()
                .map(this::toMemberResponse)
                .collect(Collectors.toList());
    }
}