package com.hei.agriculturalfederationmanagement.entity.dto;

import com.hei.agriculturalfederationmanagement.entity.Location;
import com.hei.agriculturalfederationmanagement.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateCollectivity {
    private Location location;
    private List<Member> members;
    private boolean federationApproval;
    private Member president;
    private Member vicePresident;
    private Member treasurer;
    private Member secretary;
}
