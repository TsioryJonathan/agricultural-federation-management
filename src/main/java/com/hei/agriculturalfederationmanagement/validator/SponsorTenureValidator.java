package com.hei.agriculturalfederationmanagement.validator;

import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.exception.SponsorTenureException;
import org.springframework.stereotype.Component;

@Component
public class SponsorTenureValidator {
    public void validate(Member sponsor){
        if(!sponsor.isAValidSponsor()){
            throw new SponsorTenureException(sponsor.getId() + "'s tenure in Federation is below 90 days");
        }
    }
}
