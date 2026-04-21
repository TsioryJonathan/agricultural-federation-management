package com.hei.agriculturalfederationmanagement.entity;

import com.hei.agriculturalfederationmanagement.entity.enums.Gender;
import com.hei.agriculturalfederationmanagement.entity.enums.CollectivityOccupation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {
    private Integer id;
    private String firstName;
    private String lastName;
    private Date birthDate;
    private Instant enrolmentDate;
    private String address;
    private String email;
    private String phone;
    private String profession;
    private Gender gender;
   private List<Member> referees;
    private CollectivityOccupation occupation;
}
