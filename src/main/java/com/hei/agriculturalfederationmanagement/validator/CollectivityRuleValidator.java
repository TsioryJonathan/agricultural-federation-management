package com.hei.agriculturalfederationmanagement.validator;

import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateMember;
import com.hei.agriculturalfederationmanagement.exception.InsufficientSponsorCount;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CollectivityRuleValidator {

    public void validate(CreateMember dto, List<Member> sponsors) {

        int inTargetCollectivity = 0;
        int inOtherCollectivities = 0;

        for (Member sponsor : sponsors) {

            if (!dto.getReferees().contains(sponsor.getId())) {
                continue;
            }

            List<Integer> collectivityIds =
                    sponsor.getIdsOfActualBelongingCollectivities();

            if (collectivityIds.contains(dto.getCollectivityIdentifier())) {
                inTargetCollectivity++;
            } else {
                inOtherCollectivities++;
            }
        }

        if (inTargetCollectivity < inOtherCollectivities) {
            throw new InsufficientSponsorCount(
                    dto.getFirstName() +
                            " does not satisfy collectivity sponsor rule"
            );
        }
    }
}