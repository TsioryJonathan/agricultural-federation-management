package com.hei.agriculturalfederationmanagement.validator;

import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateMember;
import com.hei.agriculturalfederationmanagement.exception.BadRequestException;
import com.hei.agriculturalfederationmanagement.exception.InsufficientSponsorCount;
import com.hei.agriculturalfederationmanagement.exception.NotFoundException;
import com.hei.agriculturalfederationmanagement.repository.MemberRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
public class MemberValidator {
    private final MemberRepository memberRepository;

    public void validate(CreateMember dto) {
        // Validate required personal information
        if (dto.getFirstName() == null || dto.getFirstName().trim().isEmpty()) {
            throw new BadRequestException("First name is required");
        }
        if (dto.getLastName() == null || dto.getLastName().trim().isEmpty()) {
            throw new BadRequestException("Last name is required");
        }
        if (dto.getBirthDate() == null) {
            throw new BadRequestException("Birth date is required");
        }
        if (dto.getGender() == null) {
            throw new BadRequestException("Gender is required");
        }
        if (dto.getAddress() == null || dto.getAddress().trim().isEmpty()) {
            throw new BadRequestException("Address is required");
        }
        if (dto.getProfession() == null || dto.getProfession().trim().isEmpty()) {
            throw new BadRequestException("Profession is required");
        }
        if (dto.getPhoneNumber() == null) {
            throw new BadRequestException("Phone number is required");
        }
        if (dto.getEmail() == null || dto.getEmail().trim().isEmpty()) {
            throw new BadRequestException("Email is required");
        }
        if (dto.getCollectivityIdentifier() == null || dto.getCollectivityIdentifier().trim().isEmpty()) {
            throw new BadRequestException("Collectivity identifier is required");
        }

        // Validate registration fee payment
        if (!dto.isRegistrationFeePaid()) {
            throw new BadRequestException("Registration fee must be paid (50,000 MGA)");
        }

        // Validate membership dues payment
        if (!dto.isMembershipDuesPaid()) {
            throw new BadRequestException("Membership dues must be paid");
        }

        // Validate referees - at least 2 required per spec
        if (dto.getReferees() == null || dto.getReferees().size() < 2) {
            throw new BadRequestException("At least 2 referees are required for admission");
        }

        // Validate each referee exists
        for (String refereeId : dto.getReferees()) {
            if (!memberRepository.existsById(refereeId)) {
                throw new NotFoundException("Referee not found with ID: " + refereeId);
            }
        }

        // Validate referee rule per spec:
        // Number of referees from target collectivity must be >= referees from other collectivities
        List<Member> referees = memberRepository.findByIds(dto.getReferees());

        int inTargetCollectivity = 0;
        int inOtherCollectivities = 0;

        for (Member referee : referees) {
            List<String> refereeCollectivities = memberRepository.findCollectivityIdsByMemberId(referee.getId());

            if (refereeCollectivities.contains(dto.getCollectivityIdentifier())) {
                inTargetCollectivity++;
            } else {
                inOtherCollectivities++;
            }
        }

        // Per spec: number of referees from target collectivity must be >= number of referees from other collectivities
        // For the referee check, we need at least 2 referees total, and the number from target
        // must be >= the number from other collectivities
        if (inTargetCollectivity < inOtherCollectivities) {
            throw new InsufficientSponsorCount(
                    String.format("%s %s does not satisfy collectivity sponsor rule. " +
                                    "Requires at least as many referees from target collectivity (%d) as from other collectivities (%d)",
                            dto.getFirstName(), dto.getLastName(), inTargetCollectivity, inOtherCollectivities)
            );
        }

        // Verify at least 2 valid referees (senior members or from target collectivity)
        if (inTargetCollectivity < 1) {
            throw new InsufficientSponsorCount(
                    String.format("%s %s must have at least 1 referee from the target collectivity",
                            dto.getFirstName(), dto.getLastName())
            );
        }
    }
}