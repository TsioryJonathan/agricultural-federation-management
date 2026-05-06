package com.hei.agriculturalfederationmanagement.repository;

import com.hei.agriculturalfederationmanagement.entity.Activity;
import com.hei.agriculturalfederationmanagement.entity.ActivityAttendance;
import com.hei.agriculturalfederationmanagement.entity.dto.MemberDescription;
import com.hei.agriculturalfederationmanagement.entity.MonthlyRecurrenceRule;
import com.hei.agriculturalfederationmanagement.entity.enums.ActivityType;
import com.hei.agriculturalfederationmanagement.entity.enums.AttendanceStatus;
import com.hei.agriculturalfederationmanagement.entity.enums.CollectivityOccupation;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Repository
@AllArgsConstructor
public class ActivityRepository {
    private final Connection connection;

    public List<Activity> findByCollectivityId(String collectivityId) {
        String sql = """
            SELECT id, label, activity_type, member_occupation_concerned, 
                   recurrence_rule, executive_date
            FROM activity
            WHERE id_collectivity = ?
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            ResultSet rs = stmt.executeQuery();
            
            List<Activity> activities = new ArrayList<>();
            while (rs.next()) {
                activities.add(mapToActivity(rs));
            }
            return activities;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find activities: " + e.getMessage(), e);
        }
    }

    public void saveAll(List<Activity> activities) {
        String sql = """
            INSERT INTO activity (id, label, activity_type, member_occupation_concerned, 
                               recurrence_rule, executive_date, id_collectivity)
            VALUES (?, ?, ?::activity_type, ?, ?, ?, ?)
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (Activity activity : activities) {
                stmt.setString(1, activity.getId());
                stmt.setString(2, activity.getLabel());
                stmt.setObject(3, activity.getActivityType() != null ? activity.getActivityType().name() : null, Types.OTHER);
                stmt.setObject(4, activity.getMemberOccupationConcerned() != null ?
                    connection.createArrayOf("collectivity_occupation", 
                        activity.getMemberOccupationConcerned().toArray()) : null);
                stmt.setObject(5, activity.getRecurrenceRule() != null ?
                    serializeRecurrenceRule(activity.getRecurrenceRule()) : null);
                stmt.setObject(6, activity.getExecutiveDate());
                stmt.setString(7, activity.getCollectivityId());
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save activities: " + e.getMessage(), e);
        }
    }

    public List<ActivityAttendance> findAttendanceByActivityId(String activityId) {
        String sql = """
            SELECT aa.id, aa.attendance_status, aa.id_member,
                   m.first_name, m.last_name, m.email,
                   mc.occupation
            FROM activity_attendance aa
            JOIN member m ON aa.id_member = m.id
            LEFT JOIN member_collectivity mc ON aa.id_member_collectivity = mc.id
            WHERE aa.id_activity = ?
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, activityId);
            ResultSet rs = stmt.executeQuery();
            
            List<ActivityAttendance> attendanceList = new ArrayList<>();
            while (rs.next()) {
                ActivityAttendance attendance = new ActivityAttendance();
                attendance.setId(rs.getString("id"));
                attendance.setAttendanceStatus(AttendanceStatus.valueOf(rs.getString("attendance_status")));
                attendance.setMemberDescription(MemberDescription.builder()
                    .id(rs.getString("id_member"))
                    .firstName(rs.getString("first_name"))
                    .lastName(rs.getString("last_name"))
                    .email(rs.getString("email"))
                    .occupation(rs.getString("occupation"))
                    .build());
                attendanceList.add(attendance);
            }
            return attendanceList;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find attendance: " + e.getMessage(), e);
        }
    }

    public void saveAttendance(List<ActivityAttendance> attendanceList) {
        String sql = """
            INSERT INTO activity_attendance (id, id_activity, id_member, 
                                           id_member_collectivity, attendance_status)
            VALUES (?, ?, ?, ?, ?::attendance_status)
            """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (ActivityAttendance attendance : attendanceList) {
                stmt.setString(1, attendance.getId());
                stmt.setString(2, attendance.getActivityId());
                stmt.setString(3, attendance.getMemberId());
                stmt.setString(4, attendance.getMemberCollectivityId());
                stmt.setObject(5, attendance.getAttendanceStatus() != null ?
                    attendance.getAttendanceStatus().name() : null, Types.OTHER);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save attendance: " + e.getMessage(), e);
        }
    }

    public boolean hasConfirmedAttendance(String activityId, String memberId) {
        String sql = """
            SELECT COUNT(*) FROM activity_attendance
            WHERE id_activity = ? AND id_member = ? 
            AND attendance_status != 'UNDEFINED'
            """;
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, activityId);
            stmt.setString(2, memberId);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check attendance: " + e.getMessage(), e);
        }
    }

    private Activity mapToActivity(ResultSet rs) throws SQLException {
        Activity activity = Activity.builder()
            .id(rs.getString("id"))
            .label(rs.getString("label"))
            .activityType(rs.getString("activity_type") != null ?
                ActivityType.valueOf(rs.getString("activity_type")) : null)
            .executiveDate(rs.getObject("executive_date", LocalDate.class))
            .build();
        
        Array occArray = rs.getArray("member_occupation_concerned");
        if (occArray != null) {
            String[] occupations = (String[]) occArray.getArray();
            List<CollectivityOccupation> occList = new ArrayList<>();
            for (String occ : occupations) {
                occList.add(CollectivityOccupation.valueOf(occ));
            }
            activity.setMemberOccupationConcerned(occList);
        }
        
        return activity;
    }

    private String serializeRecurrenceRule(MonthlyRecurrenceRule rule) {
        if (rule == null) return null;
        return String.format("{\"weekOrdinal\": %d, \"dayOfWeek\": \"%s\"}", 
            rule.getWeekOrdinal(), rule.getDayOfWeek());
    }
}
