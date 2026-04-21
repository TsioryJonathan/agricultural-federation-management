package com.hei.agriculturalfederationmanagement.entity;


import com.hei.agriculturalfederationmanagement.entity.enums.Occupation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberCollectivity {
    private int id;
    private Member member;
    private Collectivity collectivity;
    private Occupation occupation;
    private Instant startDate;
    private Instant endDate;
}
