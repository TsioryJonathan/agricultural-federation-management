package com.hei.agriculturalfederationmanagement.entity.dto;

import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.Structure;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollectivityResponse {
    private String id;
    private String location;
    private Structure structure;
    private List<Member> members;
}