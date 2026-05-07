package com.hei.agriculturalfederationmanagement.validator;

import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateCollectivity;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateCollectivityStructure;
import com.hei.agriculturalfederationmanagement.exception.BadRequestException;
import com.hei.agriculturalfederationmanagement.exception.NotFoundException;
import com.hei.agriculturalfederationmanagement.repository.MemberRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class CollectivityValidator {
    private final MemberRepository memberRepository;

    public void validateCollectivityCreation(CreateCollectivity createCollectivity) {
        if (!createCollectivity.isFederationApproval()) {
            throw new BadRequestException("Collectivity must have federation approval");
        }

        if (createCollectivity.getLocation() == null || createCollectivity.getLocation().trim().isEmpty()) {
            throw new BadRequestException("Collectivity must have location");
        }

        List<String> memberIds = createCollectivity.getMembers();
        if (memberIds == null || memberIds.isEmpty()) {
            throw new BadRequestException("Collectivity must have members");
        }

        validateAllMembersExist(memberIds);

        List<Member> members = memberRepository.findByIds(memberIds);
        if (members.size() < 10) {
            throw new BadRequestException(
                    String.format("Collectivity must have at least 10 members (currently has %d)", members.size())
            );
        }

        validateStructure(createCollectivity.getStructure(), memberIds);
    }

    private void validateAllMembersExist(List<String> memberIds) {
        List<String> missingIds = new ArrayList<>();
        for (String id : memberIds) {
            if (!memberRepository.existsById(id)) {
                missingIds.add(id);
            }
        }
        if (!missingIds.isEmpty()) {
            throw new NotFoundException("Members not found with IDs: " + missingIds);
        }
    }

    private void validateStructure(CreateCollectivityStructure structure, List<String> memberIds) {
        if (structure == null) {
            throw new BadRequestException("Collectivity structure is required");
        }

        validateStructureMember(structure.getPresident(), "President", memberIds);
        validateStructureMember(structure.getVicePresident(), "Vice President", memberIds);
        validateStructureMember(structure.getTreasurer(), "Treasurer", memberIds);
        validateStructureMember(structure.getSecretary(), "Secretary", memberIds);

        validateNoDuplicateRoles(structure);
    }

    private void validateStructureMember(String memberId, String role, List<String> memberIds) {
        if (memberId == null) {
            throw new BadRequestException(role + " ID is required");
        }
        if (!memberRepository.existsById(memberId)) {
            throw new NotFoundException(role + " not found with ID: " + memberId);
        }
        if (!memberIds.contains(memberId)) {
            throw new BadRequestException(role + " must be one of the collectivity members");
        }
    }

    private void validateNoDuplicateRoles(CreateCollectivityStructure structure) {
        List<String> roleIds = List.of(
                structure.getPresident(),
                structure.getVicePresident(),
                structure.getTreasurer(),
                structure.getSecretary()
        );

        long distinctCount = roleIds.stream().distinct().count();
        if (distinctCount != 4) {
            throw new BadRequestException("The same member cannot hold multiple specific posts");
        }
    }
}