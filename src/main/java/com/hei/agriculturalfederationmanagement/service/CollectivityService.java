package com.hei.agriculturalfederationmanagement.service;

import com.hei.agriculturalfederationmanagement.entity.Collectivity;
import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.Structure;
import com.hei.agriculturalfederationmanagement.entity.dto.*;
import com.hei.agriculturalfederationmanagement.exception.ConflictException;
import com.hei.agriculturalfederationmanagement.exception.NotFoundException;
import com.hei.agriculturalfederationmanagement.repository.CollectivityRepository;
import com.hei.agriculturalfederationmanagement.validator.CollectivityValidator;
import lombok.AllArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class CollectivityService {
    private final CollectivityRepository repository;
    private final CollectivityValidator validator;

    public List<CollectivityResponse> createCollectivities(List<CreateCollectivity> createCollectivities) throws BadRequestException {
        List<Collectivity> collectivitiesToSave = new ArrayList<>();
        List<List<Integer>> memberIdsList = new ArrayList<>();
        List<Integer> presidentIds = new ArrayList<>();
        List<Integer> vicePresidentIds = new ArrayList<>();
        List<Integer> treasurerIds = new ArrayList<>();
        List<Integer> secretaryIds = new ArrayList<>();

        for (CreateCollectivity request : createCollectivities) {
            validator.validateCollectivityCreation(request);

            Collectivity collectivity = Collectivity.builder()
                    .speciality("Agriculture")
                    .federationApproval(request.isFederationApproval())
                    .authorizationDate(Instant.now())
                    .location(request.getLocation())
                    .build();

            collectivitiesToSave.add(collectivity);
            memberIdsList.add(request.getMemberIds());
            presidentIds.add(request.getStructure().getPresidentId());
            vicePresidentIds.add(request.getStructure().getVicePresidentId());
            treasurerIds.add(request.getStructure().getTreasurerId());
            secretaryIds.add(request.getStructure().getSecretaryId());
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


    public CollectivityResponse assignIdentity(Integer id, AssignCollectivityIdentityRequest request) throws BadRequestException {
        if (request.getNumber() == null || request.getNumber().trim().isEmpty()) {
            throw new BadRequestException("Number is required");
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new BadRequestException("Name is required");
        }

        Collectivity collectivity = repository.findById(id);
        if (collectivity == null) {
            throw new NotFoundException("Collectivity not found with id: " + id);
        }

        if (collectivity.getNumber() != null || collectivity.getName() != null) {
            throw new BadRequestException("Collectivity identity already assigned and cannot be modified");
        }

        if (repository.existsByNumber(request.getNumber())) {
            throw new ConflictException("Collectivity number already exists: " + request.getNumber());
        }
        if (repository.existsByName(request.getName())) {
            throw new ConflictException("Collectivity name already exists: " + request.getName());
        }

        repository.assignIdentity(id, request.getNumber(), request.getName());

        Collectivity updated = repository.findById(id);
        return buildResponse(updated);
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
                .referees(member.getReferees())
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
}