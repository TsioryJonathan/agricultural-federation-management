package com.hei.agriculturalfederationmanagement.validator;

import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateMember;
import com.hei.agriculturalfederationmanagement.exception.InsufficientSponsorCount;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CollectivityRuleValidator {
    public void validate(CreateMember member, List<Member> sponsors) {
        int nbInActual = 0;
        int nbInOther = 0;
        List<Member> filteredSponsors = sponsors.stream()
                .filter(s -> member.getRefereesId().contains(s.getId()))
                .toList();
        for (Member sp : filteredSponsors) {
            List<Integer> collectivityIds = sp.getIdsOfActualBelongingCollectivities();
            if (collectivityIds.contains(member.getCollectivityId())) {
                nbInActual++;
            } else {
                nbInOther++;
            }
        }
        if (nbInActual < nbInOther) {
            throw new InsufficientSponsorCount(
                    member.getFirstName() + " does not satisfy collectivity sponsor rule"
            );
        }
    }
}
