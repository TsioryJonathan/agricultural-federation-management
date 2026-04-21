package com.hei.agriculturalfederationmanagement.service;

import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateMember;
import com.hei.agriculturalfederationmanagement.exception.PaymentException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class MemberService {
    private final ServiceRepository repository;

    public List<Member> createMembers(List<CreateMember> memberList) {
        /* Check payement and minimum 2 */
        for(CreateMember member : memberList){
            if(!member.isMembershipDuesPaid() || !member.isRegistrationFeePaid()){
                throw new PaymentException(member.getFirstName() + "Either membership or registration fee not paid")
            }

        }


    }
}
