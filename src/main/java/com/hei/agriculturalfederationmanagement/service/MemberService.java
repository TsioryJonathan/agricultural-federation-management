package com.hei.agriculturalfederationmanagement.service;

import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateMember;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class MemberService {
    private final ServiceRepository repository;

    public List<Member> createMembers(List<CreateMember> memberList) {

    }
}
