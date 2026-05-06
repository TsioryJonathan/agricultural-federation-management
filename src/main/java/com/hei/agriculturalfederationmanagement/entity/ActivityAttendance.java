package com.hei.agriculturalfederationmanagement.entity;

import com.hei.agriculturalfederationmanagement.entity.dto.MemberDescription;
import com.hei.agriculturalfederationmanagement.entity.enums.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActivityAttendance {
    private String id;
    private String activityId;
    private String memberId;
    private String memberCollectivityId;
    private AttendanceStatus attendanceStatus;
    
    // For response DTO
    private MemberDescription memberDescription;
}
