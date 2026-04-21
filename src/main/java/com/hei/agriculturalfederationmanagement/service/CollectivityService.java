package com.hei.agriculturalfederationmanagement.service;

import com.hei.agriculturalfederationmanagement.entity.Collectivity;
import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.MemberCollectivity;
import com.hei.agriculturalfederationmanagement.entity.Structure;
import com.hei.agriculturalfederationmanagement.entity.dto.CollectivityResponse;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateCollectivity;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateStructure;
import com.hei.agriculturalfederationmanagement.entity.enums.CollectivityOccupation;
import com.hei.agriculturalfederationmanagement.repository.CollectivityRepository;
import com.hei.agriculturalfederationmanagement.validator.CollectivityValidator;
import lombok.AllArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CollectivityService {
    private final CollectivityRepository repository;
    private final CollectivityValidator validator;

    public List<CollectivityResponse> createCollectivies(List<CreateCollectivity> createCollectivities) throws BadRequestException {
        List<Collectivity> savedCollectivities = new ArrayList<>();

        for (CreateCollectivity request : createCollectivities) {
            validator.validateCollectivityCreation(request);


            Collectivity collectivity = Collectivity.builder()
                    .number(generateCollectivityNumber())
                    .name(generateCollectivityName(request.getLocation()))
                    .speciality("Agriculture")
                    .creationDatetime(Instant.now())
                    .federationApproval(request.isFederationApproval())
                    .authorizationDate(Instant.now())
                    .location(request.getLocation())
                    .build();

            collectivity = repository.save(collectivity);

            // might need to put this in repository instead
            addMembersToCollectivity(collectivity, request);

            savedCollectivities.add(collectivity);
        }

        // might not need this anymore if in repository i provide the list of members
        List<Integer> savedIds = savedCollectivities.stream()
                .map(Collectivity::getId)
                .toList();

        List<Collectivity> fullCollectivities = repository.findAllByIds(savedIds);

        return fullCollectivities.stream()
                .map(this::buildResponse)
                .toList();
    }

    private CollectivityResponse buildResponse(Collectivity collectivity) {
        List<Member> members = collectivity.getMembers();

        Structure structure = collectivity.getStructure();
        if (structure != null) {
            structure.setPresident(findMemberInList(members, structure.getPresident().getId()));
            structure.setVicePresident(findMemberInList(members, structure.getVicePresident().getId()));
            structure.setTreasurer(findMemberInList(members, structure.getTreasurer().getId()));
            structure.setSecretary(findMemberInList(members, structure.getSecretary().getId()));
        }

        return CollectivityResponse.builder()
                .id(String.valueOf(collectivity.getId()))
                .location(collectivity.getLocation())
                .structure(structure)
                .members(members)
                .build();
    }

    private Member findMemberInList(List<Member> members, Integer memberId) {
        if (memberId == null) return null;
        return members.stream()
                .filter(m -> m.getId().equals(memberId))
                .findFirst()
                .orElse(null);
    }

    private String generateCollectivityNumber() {
        return "COL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String generateCollectivityName(String locationName) {
        return "Collectivité de " + locationName;
    }

    private void addMembersToCollectivity(Collectivity collectivity, CreateCollectivity request) {
        List<Integer> memberIds = request.getMemberIds();
        CreateStructure structure = request.getStructure();

        List<Member> members = memberRepository.findByIds(memberIds);

        Set<Integer> structureRoleIds = Set.of(
                structure.getPresidentId(),
                structure.getVicePresidentId(),
                structure.getTreasurerId(),
                structure.getSecretaryId()
        );

        for (Member member : members) {
            CollectivityOccupation occupation;

            if (member.getId().equals(structure.getPresidentId())) {
                occupation = CollectivityOccupation.PRESIDENT;
            } else if (member.getId().equals(structure.getVicePresidentId())) {
                occupation = CollectivityOccupation.VICE_PRESIDENT;
            } else if (member.getId().equals(structure.getTreasurerId())) {
                occupation = CollectivityOccupation.TREASURER;
            } else if (member.getId().equals(structure.getSecretaryId())) {
                occupation = CollectivityOccupation.SECRETARY;
            } else {
                occupation = member.isAValidSponsor()?
                        CollectivityOccupation.SENIOR : CollectivityOccupation.JUNIOR;
            }

            MemberCollectivity membership = MemberCollectivity.builder()
                    .member(member)
                    .collectivity(collectivity)
                    .occupation(occupation)
                    .startDate(Instant.now())
                    .build();

            memberCollectivityRepository.save(membership);
        }
    }
}
