package com.hei.agriculturalfederationmanagement.controller;

import com.hei.agriculturalfederationmanagement.entity.Collectivity;
import com.hei.agriculturalfederationmanagement.entity.Activity;
import com.hei.agriculturalfederationmanagement.entity.ActivityAttendance;
import com.hei.agriculturalfederationmanagement.entity.dto.*;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateActivityMemberAttendance;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateCollectivityActivity;
import com.hei.agriculturalfederationmanagement.exception.BadRequestException;
import com.hei.agriculturalfederationmanagement.exception.NotFoundException;
import com.hei.agriculturalfederationmanagement.service.CollectivityService;
import com.hei.agriculturalfederationmanagement.service.ActivityService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/collectivities")
public class CollectivityController {
    private final CollectivityService service;
    private final ActivityService activityService;

    @PostMapping
    public ResponseEntity<?> createCollectivities(@RequestBody(required = false) List<CreateCollectivity> createCollectivities){
        try{
            if(createCollectivities == null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mandatory body not provided");
            }
            List<CollectivityResponse> collectivities = service.createCollectivities(createCollectivities);
            return ResponseEntity.status(HttpStatus.CREATED).body(collectivities);
        }catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCollectivity(@PathVariable String id){
        try{
            CollectivityResponse collectivity = service.getCollectivityById(id);
            return ResponseEntity.status(HttpStatus.OK).body(collectivity);
        }catch (NotFoundException e){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }


    @GetMapping("/{id}/membershipFees")
    public ResponseEntity<?> getMembershipFees(@PathVariable String id) {
        try {
            List<MembershipFeeResponse> membershipFees = service.getMembershipFees(id);
            return ResponseEntity.ok(membershipFees);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/membershipFees")
    public ResponseEntity<?> createMembershipFees(@PathVariable String id, @RequestBody(required = false) List<CreateMembershipFee> createMembershipFees) {
        try {
            if (createMembershipFees == null || createMembershipFees.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Request body is required");
            }
            List<MembershipFeeResponse> membershipFees = service.createMembershipFees(id, createMembershipFees);
            return ResponseEntity.status(HttpStatus.CREATED).body(membershipFees);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/informations")
    public ResponseEntity<?> assignInformations(@PathVariable String id, @RequestBody(required = false) CollectivityInformation collectivityInformation){
        try{
            if(collectivityInformation == null){
                return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mandatory body not provided");
            }
            CollectivityResponse response = service.assignIdentity(id, collectivityInformation);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/transactions")
    public ResponseEntity<?> getCollectivityTransactions(
            @PathVariable String id,
            @RequestParam(required = false) Instant from,
            @RequestParam(required = false) Instant to) {
        try {
            if(from == null || to == null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Either mandatory 'to' or 'from' param not provided");
            }
            List<CollectivityTransactionResponse> transactions =
                    service.getCollectivityTransactions(id, from, to);
            return ResponseEntity.status(HttpStatus.OK).body(transactions);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @GetMapping("/{id}/financialAccounts")
    public ResponseEntity<?> getFinancialAccounts(
            @PathVariable String id,
            @RequestParam(required = false) LocalDate at) {
        try {
            CollectivityFinancialAccountResponse accounts = service.getFinancialAccounts(id, at);
            return ResponseEntity.ok(accounts);
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PostMapping("/{id}/activities")
    public List<CollectivityActivity> createActivities(@PathVariable String id,
                                                   @RequestBody List<CreateCollectivityActivity> requests) {
        List<Activity> activities = activityService.createActivities(id, requests);
        return activities.stream()
            .map(this::toCollectivityActivity)
            .toList();
    }

    @GetMapping("/{id}/activities")
    public List<CollectivityActivity> getActivities(@PathVariable String id) {
        return activityService.getActivities(id).stream()
            .map(this::toCollectivityActivity)
            .toList();
    }

    @PostMapping("/{id}/activities/{activityId}/attendance")
    public List<ActivityMemberAttendance> confirmAttendance(@PathVariable String activityId,
                                                         @RequestBody List<CreateActivityMemberAttendance> requests) {
        List<ActivityAttendance> attendance = activityService.confirmAttendance(activityId, requests);
        return attendance.stream()
            .map(this::toActivityMemberAttendance)
            .toList();
    }

    @GetMapping("/{id}/activities/{activityId}/attendance")
    public List<ActivityMemberAttendance> getAttendance(@PathVariable String activityId) {
        return activityService.getAttendance(activityId).stream()
            .map(this::toActivityMemberAttendance)
            .toList();
    }

    private CollectivityActivity toCollectivityActivity(Activity activity) {
        com.hei.agriculturalfederationmanagement.entity.dto.MonthlyRecurrenceRule dtoRecurrenceRule = null;
        if (activity.getRecurrenceRule() != null) {
            dtoRecurrenceRule = com.hei.agriculturalfederationmanagement.entity.dto.MonthlyRecurrenceRule.builder()
                .weekOrdinal(activity.getRecurrenceRule().getWeekOrdinal())
                .dayOfWeek(activity.getRecurrenceRule().getDayOfWeek())
                .build();
        }
        return CollectivityActivity.builder()
            .id(activity.getId())
            .label(activity.getLabel())
            .activityType(activity.getActivityType() != null ? activity.getActivityType().name() : null)
            .memberOccupationConcerned(activity.getMemberOccupationConcerned())
            .recurrenceRule(dtoRecurrenceRule)
            .executiveDate(activity.getExecutiveDate())
            .build();
    }

    private ActivityMemberAttendance toActivityMemberAttendance(ActivityAttendance attendance) {
        return ActivityMemberAttendance.builder()
            .id(attendance.getId())
            .memberDescription(attendance.getMemberDescription())
            .attendanceStatus(attendance.getAttendanceStatus())
            .build();
    }

}
