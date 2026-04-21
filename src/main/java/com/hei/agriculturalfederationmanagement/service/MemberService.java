package com.hei.agriculturalfederationmanagement.service;

import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.MemberCollectivity;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateMember;
import com.hei.agriculturalfederationmanagement.exception.InsufficientSponsorCount;
import com.hei.agriculturalfederationmanagement.exception.PaymentException;
import com.hei.agriculturalfederationmanagement.exception.SponsorTenureException;
import com.hei.agriculturalfederationmanagement.validator.CollectivityRuleValidator;
import com.hei.agriculturalfederationmanagement.validator.PaymentValidator;
import com.hei.agriculturalfederationmanagement.validator.SponsorCountValidator;
import com.hei.agriculturalfederationmanagement.validator.SponsorTenureValidator;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
@Service
@AllArgsConstructor
public class MemberService {

    private final ServiceRepository repository;
    private final PaymentValidator paymentValidator;
    private final CollectivityRuleValidator collectivityRuleValidator;
    private final SponsorCountValidator sponsorCountValidator;

    public List<Member> createMembers(List<CreateMember> memberList) {
        for(CreateMember member : memberList){
            paymentValidator.validate(member);
            sponsorCountValidator.validate(member);
        }

        // 3. Get all sponsors
        List<Integer> sponsorIds = memberList.stream()
                .flatMap(m -> m.getRefereesId().stream())
                .distinct()
                .toList();

        List<Member> sponsorList = repository.findAllByIds(sponsorIds);

        memberList.forEach(m ->
                collectivityRuleValidator.validate(m, sponsorList)
        );

        // 6. TODO: create + save members
        List<Member> createdMembers = memberList.stream()
                .map(m -> Member.builder()
                        .firstName(m.getFirstName())
                        .lastName(m.getLastName())
                        .email(m.getEmail())
                        .phone(m.getPhone())
                        .address(m.getAddress())
                        .build()
                )
                .toList();

        // repository.saveAll(createdMembers);

        return createdMembers;
    }
}