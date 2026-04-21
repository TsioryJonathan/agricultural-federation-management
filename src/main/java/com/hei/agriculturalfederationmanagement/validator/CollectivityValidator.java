package com.hei.agriculturalfederationmanagement.validator;

import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateCollectivity;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class CollectivityValidator {


    public void validateCollectivityCreation(CreateCollectivity createCollectivity) throws BadRequestException {

        if(!createCollectivity.isFederationApproval()){
            throw new BadRequestException("Collectivity must have federation approval");
        }

        if(createCollectivity.getMemberIds() == null || createCollectivity.getMemberIds().size() < 10){
            throw new BadRequestException(
                    String.format("Collectivity must have at least 10 members (currently has %d)",
                            createCollectivity.getMemberIds() != null ? createCollectivity.getMemberIds().size() : 0)
            );
        }
    }


    // maybe this should be in member entity but im not sure yet
    private boolean hasMinimumSeniority(Member member) {
        if (member.getEnrolmentDate() == null) {
            return false;
        }
        long monthsSinceEnrolment = ChronoUnit.MONTHS.between(member.getEnrolmentDate(), LocalDateTime.now());
        return monthsSinceEnrolment >= 6;
    }
}
