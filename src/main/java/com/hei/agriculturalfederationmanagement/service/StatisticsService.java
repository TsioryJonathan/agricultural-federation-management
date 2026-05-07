package com.hei.agriculturalfederationmanagement.service;

import com.hei.agriculturalfederationmanagement.entity.dto.CollectivityLocalStatistics;
import com.hei.agriculturalfederationmanagement.entity.dto.CollectivityOverallStatistics;
import com.hei.agriculturalfederationmanagement.exception.BadRequestException;
import com.hei.agriculturalfederationmanagement.exception.NotFoundException;
import com.hei.agriculturalfederationmanagement.repository.CollectivityRepository;
import com.hei.agriculturalfederationmanagement.repository.StatisticsRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class StatisticsService {
    private final StatisticsRepository statisticsRepository;
    private final CollectivityRepository collectivityRepository;

    /**
     * Get local statistics for a specific collectivity
     * Includes: earned amount, unpaid amount, and assiduity percentage per member
     */
    public List<CollectivityLocalStatistics> getLocalStatistics(String collectivityId, LocalDate from, LocalDate to) {
        if (collectivityId == null || collectivityId.trim().isEmpty()) {
            throw new BadRequestException("Collectivity ID is required");
        }

        if (from == null || to == null) {
            throw new BadRequestException("Both 'from' and 'to' dates are required");
        }

        if (from.isAfter(to)) {
            throw new BadRequestException("'from' date must be before or equal to 'to' date");
        }

        // Verify collectivity exists
        if (collectivityRepository.findById(collectivityId) == null) {
            throw new NotFoundException("Collectivity not found with id: " + collectivityId);
        }

        return statisticsRepository.getLocalStatistics(collectivityId, from, to);
    }

    /**
     * Get overall statistics for all collectivities
     * Includes: current due percentage, new members, and overall assiduity percentage
     */
    public List<CollectivityOverallStatistics> getOverallStatistics(Instant from, Instant to) {
        if (from == null || to == null) {
            throw new BadRequestException("Both 'from' and 'to' dates are required");
        }

        if (from.isAfter(to)) {
            throw new BadRequestException("'from' date must be before or equal to 'to' date");
        }

        return statisticsRepository.getOverallStatistics(from, to);
    }
}