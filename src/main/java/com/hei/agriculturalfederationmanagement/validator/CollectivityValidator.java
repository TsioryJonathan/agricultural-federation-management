package com.hei.agriculturalfederationmanagement.validator;

import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateCollectivity;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateStructure;
import com.hei.agriculturalfederationmanagement.exception.NotFoundException;
import com.hei.agriculturalfederationmanagement.repository.MemberRepository;
import lombok.AllArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
@AllArgsConstructor
public class CollectivityValidator {
    private final MemberRepository memberRepository;

    public void validateCollectivityCreation(CreateCollectivity createCollectivity) throws BadRequestException {

        if(!createCollectivity.isFederationApproval()){
            throw new BadRequestException("Collectivity must have federation approval");
        }

        if(createCollectivity.getLocation() == null){
            throw new BadRequestException("Collectivity must have location");
        }


        List<Integer> memberIds = createCollectivity.getMemberIds();
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

        long membersWithSeniority = members.stream()
                .filter(Member::isAValidSponsor)
                .count();

        if (membersWithSeniority < 5) {
            throw new BadRequestException(
                    String.format("Collectivity must have at least 5 members with 6+ months seniority (currently has %d)",
                            membersWithSeniority)
            );
        }

        validateStructure(createCollectivity.getStructure(), memberIds);
    }


    private void validateAllMembersExist(List<Integer> memberIds) {
        List<Integer> missingIds = new ArrayList<>();

        for (Integer id : memberIds) {
            if (!memberRepository.existsById(id)) {
                missingIds.add(id);
            }
        }

        if (!missingIds.isEmpty()) {
            throw new NotFoundException("Members not found with IDs: " + missingIds);
        }
    }



    private void validateStructure(CreateStructure structure, List<Integer> memberIds) throws BadRequestException {
        if (structure == null) {
            throw new BadRequestException("Collectivity structure is required");
        }

        if (structure.getPresidentId() == null) {
            throw new BadRequestException("President ID is required");
        }
        if (structure.getVicePresidentId() == null) {
            throw new BadRequestException("Vice President ID is required");
        }
        if (structure.getTreasurerId() == null) {
            throw new BadRequestException("Treasurer ID is required");
        }
        if (structure.getSecretaryId() == null) {
            throw new BadRequestException("Secretary ID is required");
        }

        validateStructureMemberExists(structure.getPresidentId(), "President");
        validateStructureMemberExists(structure.getVicePresidentId(), "Vice President");
        validateStructureMemberExists(structure.getTreasurerId(), "Treasurer");
        validateStructureMemberExists(structure.getSecretaryId(), "Secretary");

        validateStructureMemberInList(structure.getPresidentId(), memberIds, "President");
        validateStructureMemberInList(structure.getVicePresidentId(), memberIds, "Vice President");
        validateStructureMemberInList(structure.getTreasurerId(), memberIds, "Treasurer");
        validateStructureMemberInList(structure.getSecretaryId(), memberIds, "Secretary");

        validateNoDuplicateRoles(structure);
    }

    private void validateStructureMemberExists(Integer memberId, String role) {
        if (memberRepository.existsById(memberId)) {
            throw new NotFoundException(role + " not found with ID: " + memberId);
        }
    }

    private void validateStructureMemberInList(Integer memberId, List<Integer> memberIds, String role) throws BadRequestException {
        if (!memberIds.contains(memberId)) {
            throw new BadRequestException(role + " must be one of the collectivity members");
        }
    }

    private void validateNoDuplicateRoles(CreateStructure structure) throws BadRequestException {
        List<Integer> roleIds = List.of(
                structure.getPresidentId(),
                structure.getVicePresidentId(),
                structure.getTreasurerId(),
                structure.getSecretaryId()
        );

        long distinctCount = roleIds.stream().distinct().count();
        if (distinctCount != 4) {
            throw new BadRequestException("The same member cannot hold multiple specific posts (President, Vice President, Treasurer, Secretary)");
        }
    }

}
