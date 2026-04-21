package com.hei.agriculturalfederationmanagement.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Collectivity {
    private Integer id;
    private String number;
    private String speciality;
    private Instant creationDatetime;
    private boolean federationApproval;
    private Instant authorizationDate;
    private Location location;
    private Structure structure;
    private List<Member> members;
}
