package com.hei.agriculturalfederationmanagement.entity;

import com.hei.agriculturalfederationmanagement.entity.enums.Status;
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
    private Status status;
    private Instant authorizationDate;
    private Location location;
    private List<Member> members;
    private Member president;
    private Member vicePresident;
    private Member treasurer;
    private Member secretary;
}
