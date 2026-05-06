package com.hei.agriculturalfederationmanagement.service;

import com.hei.agriculturalfederationmanagement.entity.Activity;
import com.hei.agriculturalfederationmanagement.entity.ActivityAttendance;
import com.hei.agriculturalfederationmanagement.entity.MonthlyRecurrenceRule;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateActivityMemberAttendance;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateCollectivityActivity;
import com.hei.agriculturalfederationmanagement.entity.enums.ActivityType;
import com.hei.agriculturalfederationmanagement.repository.ActivityRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ActivityService {
    private final ActivityRepository activityRepository;

    public List<Activity> createActivities(String collectivityId, List<CreateCollectivityActivity> requests) {
        // Validate: cannot have both recurrenceRule and executiveDate
        for (CreateCollectivityActivity request : requests) {
            if (request.getRecurrenceRule() != null && request.getExecutiveDate() != null) {
                throw new IllegalArgumentException("Cannot provide both recurrenceRule and executiveDate");
            }
        }

        List<Activity> activities = requests.stream()
            .map(req -> {
                MonthlyRecurrenceRule entityRecurrenceRule = null;
                if (req.getRecurrenceRule() != null) {
                    entityRecurrenceRule = MonthlyRecurrenceRule.builder()
                        .weekOrdinal(req.getRecurrenceRule().getWeekOrdinal())
                        .dayOfWeek(req.getRecurrenceRule().getDayOfWeek())
                        .build();
                }
                return Activity.builder()
                    .id(UUID.randomUUID().toString())
                    .label(req.getLabel())
                    .activityType(ActivityType.valueOf(req.getActivityType()))
                    .memberOccupationConcerned(req.getMemberOccupationConcerned())
                    .recurrenceRule(entityRecurrenceRule)
                    .executiveDate(req.getExecutiveDate())
                    .collectivityId(collectivityId)
                    .build();
            })
            .collect(Collectors.toList());

        activityRepository.saveAll(activities);
        return activities;
    }

    public List<Activity> getActivities(String collectivityId) {
        return activityRepository.findByCollectivityId(collectivityId);
    }

    public List<ActivityAttendance> confirmAttendance(String activityId,
                                                  List<CreateActivityMemberAttendance> requests) {
        // Validate: only UNDEFINED status can be updated
        for (CreateActivityMemberAttendance request : requests) {
            if (activityRepository.hasConfirmedAttendance(activityId, request.getMemberIdentifier())) {
                throw new IllegalStateException("Attendance already confirmed for member: " + request.getMemberIdentifier());
            }
        }

        List<ActivityAttendance> attendanceList = requests.stream()
            .map(req -> ActivityAttendance.builder()
                .id(UUID.randomUUID().toString())
                .activityId(activityId)
                .memberId(req.getMemberIdentifier())
                .attendanceStatus(req.getAttendanceStatus())
                .build())
            .collect(Collectors.toList());

        activityRepository.saveAttendance(attendanceList);
        return attendanceList;
    }

    public List<ActivityAttendance> getAttendance(String activityId) {
        return activityRepository.findAttendanceByActivityId(activityId);
    }
}
