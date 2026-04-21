package com.hei.agriculturalfederationmanagement.service;

import com.hei.agriculturalfederationmanagement.entity.Collectivity;
import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.Structure;
import com.hei.agriculturalfederationmanagement.entity.dto.CollectivityResponse;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateCollectivity;
import com.hei.agriculturalfederationmanagement.repository.CollectivityRepository;
import com.hei.agriculturalfederationmanagement.validator.CollectivityValidator;
import lombok.AllArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class CollectivityService {
    private final CollectivityRepository repository;
    private final CollectivityValidator validator;

    public List<Collectivity> createCollectivies(List<CreateCollectivity> createCollectivities) throws BadRequestException {
        List<Collectivity> savedCollectivities = new ArrayList<>();

        for (CreateCollectivity request : createCollectivities) {
            validator.validateCollectivityCreation(request);

            Integer locationId = locationRepository.save(request.getLocation().getName());

            Collectivity collectivity = Collectivity.builder()
                    .number(generateCollectivityNumber())
                    .name(generateCollectivityName(request.getLocation().getName()))
                    .speciality("Agriculture")
                    .creationDatetime(LocalDateTime.now())
                    .federationApproval(request.isFederationApproval())
                    .authorizationDate(LocalDateTime.now())
                    .idFederation(1)
                    .idLocation(locationId)
                    .build();

            collectivity = repository.save(collectivity);

            addMembersToCollectivity(collectivity.getId(), request);

            savedCollectivities.add(collectivity);
        }

        List<Integer> savedIds = savedCollectivities.stream()
                .map(Collectivity::getId)
                .toList();

        List<Collectivity> fullCollectivities = repository.findAllByIds(savedIds);

        // 7. Build responses with full member objects
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
                .location(collectivity.getLocation().getName())
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
}
