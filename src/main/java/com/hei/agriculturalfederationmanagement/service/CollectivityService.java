package com.hei.agriculturalfederationmanagement.service;

import com.hei.agriculturalfederationmanagement.entity.Collectivity;
import com.hei.agriculturalfederationmanagement.entity.Member;
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
import java.util.UUID;
import java.util.stream.IntStream;

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

    private List<MemberResponse> buildResponse(List<Member> members) {

        return IntStream.range(0, members.size())
                .mapToObj(i -> {
                    Member m = members.get(i);

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
                            .build();
                })
                .toList();
    }

    private CollectivityResponse buildResponse(Collectivity collectivity) {
        return CollectivityResponse.builder()
                .id(String.valueOf(collectivity.getId()))
                .number(collectivity.getNumber())
                .name(collectivity.getName())
                .location(collectivity.getLocation())
                .structure(collectivity.getStructure())
                .members(buildResponse(collectivity.getMembers()))
                .build();
    }

}