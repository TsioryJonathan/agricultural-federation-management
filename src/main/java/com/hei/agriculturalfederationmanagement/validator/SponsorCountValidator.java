package com.hei.agriculturalfederationmanagement.validator;

import com.hei.agriculturalfederationmanagement.entity.dto.CreateMember;
import com.hei.agriculturalfederationmanagement.exception.InsufficientSponsorCount;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SponsorCountValidator {
    public void validate(CreateMember member) {
        if (member.getRefereesId().size() < 2) {
            throw new InsufficientSponsorCount(
                    member.getFirstName() + " : at least two sponsors required"
            );
        }
    }
    public void validate(List<CreateMember> members) {
        for (CreateMember member : members) {
            validate(member);
        }
    }
}
