
package com.hei.agriculturalfederationmanagement.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.lang.reflect.Member;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Federation {
    private Integer id;
    private List<Collectivity> collectivities;
    private Member president;
    private Member vicePresident;
    private Member treasurer;
    private Member secretary;
}
