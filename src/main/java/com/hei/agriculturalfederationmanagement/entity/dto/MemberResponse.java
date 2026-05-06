package com.hei.agriculturalfederationmanagement.entity.dto;

import com.hei.agriculturalfederationmanagement.entity.enums.Gender;
import com.hei.agriculturalfederationmanagement.entity.enums.CollectivityOccupation;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class MemberResponse {

    private String id;
    private String firstName;
    private String lastName;
    private LocalDate birthDate;
    private Gender gender;
    private String address;
    private String profession;
    private int phoneNumber;
    private String email;

    private CollectivityOccupation occupation;

    private List<String> referees;
}
