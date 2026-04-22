package com.hei.agriculturalfederationmanagement.entity.dto;

import com.hei.agriculturalfederationmanagement.entity.enums.Gender;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class MemberResponse {

    private Integer id;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private Gender gender;
    private String address;
    private String profession;
    private String phoneNumber;
    private String email;

    private List<Integer> referees;
}
