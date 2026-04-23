package com.hei.agriculturalfederationmanagement.service;

import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.MemberRefereeLink;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateMember;
import com.hei.agriculturalfederationmanagement.repository.MemberCollectivityRepository;
import com.hei.agriculturalfederationmanagement.repository.RefereeRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class MemberLinkService {

    private final MemberCollectivityRepository memberCollectivityRepository;
    private final RefereeRepository refereeRepository;

    public void createMemberCollectivityLinks(List<Member> savedMembers, List<CreateMember> dtos) {
        for (int i = 0; i < savedMembers.size(); i++) {
            Member saved = savedMembers.get(i);
            CreateMember dto = dtos.get(i);
            memberCollectivityRepository.saveMemberCollectivityLink(
                    saved.getId(),
                    dto.getCollectivityIdentifier(),
                    dto.getOccupation()
            );
        }
    }

    public void createRefereeLinks(List<Member> savedMembers, List<CreateMember> dtos) {
        List<MemberRefereeLink> refereeLinks = new ArrayList<>();
        for (int i = 0; i < savedMembers.size(); i++) {
            Member saved = savedMembers.get(i);
            CreateMember dto = dtos.get(i);
            if (dto.getReferees() != null) {
                for (String refId : dto.getReferees()) {
                    refereeLinks.add(MemberRefereeLink.builder()
                            .idMember(saved.getId())
                            .idReferee(refId)
                            .idCollectivity(dto.getCollectivityIdentifier())
                            .link("FRIEND")
                            .build());
                }
            }
        }
        if (!refereeLinks.isEmpty()) {
            refereeRepository.saveRefereeMemberLink(refereeLinks);
        }
    }
}