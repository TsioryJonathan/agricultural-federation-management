package com.hei.agriculturalfederationmanagement.service;

import com.hei.agriculturalfederationmanagement.entity.Activity;
import com.hei.agriculturalfederationmanagement.entity.ActivityAttendance;
import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.dto.*;
import com.hei.agriculturalfederationmanagement.entity.enums.MemberOccupation;
import com.hei.agriculturalfederationmanagement.exception.AttendanceAlreadyConfirmedException;
import com.hei.agriculturalfederationmanagement.exception.BadRequestException;
import com.hei.agriculturalfederationmanagement.exception.NotFoundException;
import com.hei.agriculturalfederationmanagement.mapper.Mapper;
import com.hei.agriculturalfederationmanagement.repository.ActivityRepository;
import com.hei.agriculturalfederationmanagement.repository.CollectivityRepository;
import com.hei.agriculturalfederationmanagement.repository.MemberRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ActivityService {
    private final ActivityRepository activityRepository;
    private final CollectivityRepository collectivityRepository;
    private final MemberRepository memberRepository;
    private final Mapper mapper;

    public List<CollectivityActivity> createActivities(String collectivityId, List<CreateCollectivityActivity> createActivities) {
        // Verify collectivity exists
        if (collectivityRepository.findById(collectivityId) == null) {
            throw new NotFoundException("Collectivity not found with id: " + collectivityId);
        }

        List<CollectivityActivity> responses = new ArrayList<>();

        for (CreateCollectivityActivity createDto : createActivities) {
            // Validate: cannot have both recurrence rule and executive date
            if (createDto.getRecurrenceRule() != null && createDto.getExecutiveDate() != null) {
                throw new BadRequestException("Cannot provide both recurrence rule and executive date");
            }

            // Validate: must have either recurrence rule or executive date
            if (createDto.getRecurrenceRule() == null && createDto.getExecutiveDate() == null) {
                throw new BadRequestException("Must provide either recurrence rule or executive date");
            }

            Activity activity = Activity.builder()
                    .label(createDto.getLabel())
                    .activityType(createDto.getActivityType())
                    .executiveDate(createDto.getExecutiveDate())
                    .memberOccupationConcerned(
                            createDto.getMemberOccupationConcerned() != null ?
                                    createDto.getMemberOccupationConcerned().stream()
                                    .map(Enum::name)
                                    .collect(Collectors.toList()) :
                                    new ArrayList<>()
                    )
                    .build();

            if (createDto.getRecurrenceRule() != null) {
                activity.setWeekOrdinal(createDto.getRecurrenceRule().getWeekOrdinal());
                activity.setDayOfWeek(createDto.getRecurrenceRule().getDayOfWeek());
            }

            Activity savedActivity = activityRepository.save(activity, collectivityId);

            // Initialize UNDEFINED attendance for concerned members
            if (savedActivity.getMemberOccupationConcerned() != null &&
                    !savedActivity.getMemberOccupationConcerned().isEmpty()) {
                activityRepository.initializeAttendance(
                        savedActivity.getId(),
                        collectivityId,
                        savedActivity.getMemberOccupationConcerned()
                );
            }

            responses.add(mapToActivityResponse(savedActivity));
        }

        return responses;
    }

    public List<CollectivityActivity> getActivities(String collectivityId) {
        if (collectivityRepository.findById(collectivityId) == null) {
            throw new NotFoundException("Collectivity not found with id: " + collectivityId);
        }

        List<Activity> activities = activityRepository.findByCollectivityId(collectivityId);
        return activities.stream()
                .map(this::mapToActivityResponse)
                .collect(Collectors.toList());
    }

    public List<ActivityMemberAttendance> createAttendance(String collectivityId, String activityId,
                                                           List<CreateActivityMemberAttendance> attendances) {
        // Verify collectivity exists
        if (collectivityRepository.findById(collectivityId) == null) {
            throw new NotFoundException("Collectivity not found with id: " + collectivityId);
        }

        // Verify activity exists
        Activity activity = activityRepository.findById(activityId);
        if (activity == null) {
            throw new NotFoundException("Activity not found with id: " + activityId);
        }

        // Convert to entity list
        List<ActivityAttendance> attendanceEntities = new ArrayList<>();
        for (CreateActivityMemberAttendance dto : attendances) {
            // Verify member exists
            memberRepository.findById(dto.getMemberIdentifier())
                    .orElseThrow(() -> new NotFoundException("Member not found with id: " + dto.getMemberIdentifier()));

            ActivityAttendance attendance = ActivityAttendance.builder()
                    .activityId(activityId)
                    .member(Member.builder().id(dto.getMemberIdentifier()).build())
                    .attendanceStatus(dto.getAttendanceStatus())
                    .build();
            attendanceEntities.add(attendance);
        }

        List<ActivityAttendance> savedAttendances = new ArrayList<>();

        // Save attendance (will throw error if already confirmed)
        try{
            savedAttendances = activityRepository.saveAttendance(activityId, attendanceEntities);
        }catch (RuntimeException e){
            if(e.getMessage().contains("Attendance already confirmed for member")){
                throw new AttendanceAlreadyConfirmedException(e.getMessage());
            }
        }

        return savedAttendances.stream()
                .map(this::mapToAttendanceResponse)
                .collect(Collectors.toList());
    }

    public List<ActivityMemberAttendance> getAttendance(String collectivityId, String activityId) {
        // Verify collectivity exists
        if (collectivityRepository.findById(collectivityId) == null) {
            throw new NotFoundException("Collectivity not found with id: " + collectivityId);
        }

        // Verify activity exists
        Activity activity = activityRepository.findById(activityId);
        if (activity == null) {
            throw new NotFoundException("Activity not found with id: " + activityId);
        }

        List<ActivityAttendance> attendances = activityRepository.getAttendance(activityId, collectivityId);

        return attendances.stream()
                .map(this::mapToAttendanceResponse)
                .collect(Collectors.toList());
    }

    private CollectivityActivity mapToActivityResponse(Activity activity) {
        MonthlyRecurrenceRule recurrenceRule = null;
        if (activity.getWeekOrdinal() != null && activity.getDayOfWeek() != null) {
            recurrenceRule = MonthlyRecurrenceRule.builder()
                    .weekOrdinal(activity.getWeekOrdinal())
                    .dayOfWeek(activity.getDayOfWeek())
                    .build();
        }

        return CollectivityActivity.builder()
                .id(activity.getId())
                .label(activity.getLabel())
                .activityType(activity.getActivityType())
                .memberOccupationConcerned(
                        activity.getMemberOccupationConcerned() != null ?
                                activity.getMemberOccupationConcerned().stream()
                                .map(MemberOccupation::valueOf)
                                .collect(Collectors.toList()) :
                                new ArrayList<>()
                )
                .recurrenceRule(recurrenceRule)
                .executiveDate(activity.getExecutiveDate())
                .build();
    }

    private ActivityMemberAttendance mapToAttendanceResponse(ActivityAttendance attendance) {
        Member member = attendance.getMember();
        MemberDescription memberDescription = MemberDescription.builder()
                .id(member.getId())
                .firstName(member.getFirstName())
                .lastName(member.getLastName())
                .email(member.getEmail())
                .build();

        return ActivityMemberAttendance.builder()
                .id(attendance.getId())
                .memberDescription(memberDescription)
                .attendanceStatus(attendance.getAttendanceStatus())
                .build();
    }
}