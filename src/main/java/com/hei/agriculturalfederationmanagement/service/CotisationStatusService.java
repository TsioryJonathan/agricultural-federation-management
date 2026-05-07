package com.hei.agriculturalfederationmanagement.service;

import com.hei.agriculturalfederationmanagement.entity.*;
import com.hei.agriculturalfederationmanagement.entity.dto.*;
import com.hei.agriculturalfederationmanagement.entity.enums.ActivityStatus;
import com.hei.agriculturalfederationmanagement.exception.NotFoundException;
import com.hei.agriculturalfederationmanagement.mapper.Mapper;
import com.hei.agriculturalfederationmanagement.repository.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@AllArgsConstructor
public class CotisationStatusService {
    private final CollectivityRepository collectivityRepository;
    private final CotisationPlanRepository cotisationPlanRepository;
    private final Mapper mapper;

    public List<MemberCotisationStatus> getMembersCotisationStatus(String collectivityId) {
        Collectivity collectivity = collectivityRepository.findById(collectivityId);
        if (collectivity == null) {
            throw new NotFoundException("Collectivity not found: " + collectivityId);
        }

        List<CotisationPlan> activePlans = cotisationPlanRepository
                .findByCollectivityId(collectivityId)
                .stream()
                .filter(p -> p.getStatus() == ActivityStatus.ACTIVE)
                .toList();

        List<MemberCotisationStatus> statuses = new ArrayList<>();

        for (Member member : collectivity.getMembers()) {
            double totalDue = 0;
            double totalPaid = 0;
            boolean isLate = false;

            for (CotisationPlan plan : activePlans) {
                double amountDueNow = calculateAmountDueNow(plan);
                totalDue += amountDueNow;
            }

            totalPaid = getTotalPaid(member.getId(), collectivityId);
            double remaining = totalDue - totalPaid;

            if (remaining > 0) {
                // Check if late: member hasn't paid enough AND due date is passed
                isLate = isMemberLate(activePlans, totalPaid);
            }

            statuses.add(MemberCotisationStatus.builder()
                    .memberDescription(mapper.toMemberDescription(member))
                    .totalDue(totalDue)
                    .totalPaid(totalPaid)
                    .remainingToPay(Math.max(0, remaining))
                    .isLate(isLate)
                    .build());
        }

        return statuses;
    }

    private double calculateAmountDueNow(CotisationPlan plan) {
        LocalDate now = LocalDate.now();
        LocalDate eligible = plan.getEligibleFrom();

        if (eligible == null || eligible.isAfter(now)) {
            return 0; // Not yet eligible
        }

        return switch (plan.getFrequency()) {
            case MONTHLY -> {
                // Count months from eligibleFrom to now
                long months = java.time.temporal.ChronoUnit.MONTHS.between(
                        eligible.withDayOfMonth(1), now.withDayOfMonth(1));
                yield plan.getAmount() * (months + 1);
            }
            case ANNUALLY -> {
                // Count years from eligibleFrom to now
                long years = java.time.temporal.ChronoUnit.YEARS.between(eligible, now);
                yield plan.getAmount() * (years + 1);
            }
            case WEEKLY -> plan.getAmount(); // Basic: just 1 payment for now
            case PUNCTUALLY -> plan.getAmount(); // Always 1 payment
        };
    }

    private boolean isMemberLate(List<CotisationPlan> plans, double totalPaid) {
        LocalDate now = LocalDate.now();
        double requiredByNow = 0;

        for (CotisationPlan plan : plans) {
            LocalDate eligible = plan.getEligibleFrom();
            if (eligible == null) continue;

            switch (plan.getFrequency()) {
                case MONTHLY -> {
                    // Due date: same day next month
                    LocalDate dueDate = eligible.plusMonths(1);
                    if (now.isAfter(dueDate)) {
                        requiredByNow += plan.getAmount();
                        // Add subsequent months
                        long monthsAfter = java.time.temporal.ChronoUnit.MONTHS.between(dueDate.withDayOfMonth(1), now.withDayOfMonth(1));
                        requiredByNow += plan.getAmount() * monthsAfter;
                    }
                }
                case ANNUALLY -> {
                    // Due date: same day next year
                    LocalDate dueDate = eligible.plusYears(1);
                    if (now.isAfter(dueDate)) {
                        requiredByNow += plan.getAmount();
                        long yearsAfter = java.time.temporal.ChronoUnit.YEARS.between(dueDate, now);
                        requiredByNow += plan.getAmount() * yearsAfter;
                    }
                }
                case WEEKLY -> {
                    if (now.isAfter(eligible.plusWeeks(1))) {
                        requiredByNow += plan.getAmount();
                    }
                }
                case PUNCTUALLY -> {
                    if (now.isAfter(eligible)) {
                        requiredByNow += plan.getAmount();
                    }
                }
            }
        }

        return totalPaid < requiredByNow;
    }

    private double getTotalPaid(String memberId, String collectivityId) {
        return collectivityRepository.getTotalPaidByMember(memberId, collectivityId);
    }
}