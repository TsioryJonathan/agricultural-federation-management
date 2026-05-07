package com.hei.agriculturalfederationmanagement.repository;

import com.hei.agriculturalfederationmanagement.entity.Activity;
import com.hei.agriculturalfederationmanagement.entity.ActivityAttendance;
import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.enums.ActivityType;
import com.hei.agriculturalfederationmanagement.entity.enums.AttendanceStatus;
import com.hei.agriculturalfederationmanagement.entity.enums.Gender;
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

    public Activity save(Activity activity, String collectivityId) {
        String insertSql = """
            INSERT INTO activity (id, id_collectivity, label, activity_type, executive_date, 
                                week_ordinal, day_of_week, creation_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;

        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
            String id = "act-" + System.currentTimeMillis();
            stmt.setString(1, id);
            stmt.setString(2, collectivityId);
            stmt.setString(3, activity.getLabel());
            stmt.setString(4, activity.getActivityType().name());

            if (activity.getExecutiveDate() != null) {
                stmt.setDate(5, Date.valueOf(activity.getExecutiveDate()));
            } else {
                stmt.setNull(5, Types.DATE);
            }

            if (activity.getWeekOrdinal() != null) {
                stmt.setInt(6, activity.getWeekOrdinal());
            } else {
                stmt.setNull(6, Types.INTEGER);
            }

            stmt.setString(7, activity.getDayOfWeek());
            stmt.setDate(8, Date.valueOf(LocalDate.now()));

            stmt.executeUpdate();
            activity.setId(id);

            // Insert member occupations concerned
            if (activity.getMemberOccupationConcerned() != null && !activity.getMemberOccupationConcerned().isEmpty()) {
                String occupationSql = """
                    INSERT INTO activity_member_occupation (id_activity, occupation)
                    VALUES (?, ?)
                """;
                try (PreparedStatement occStmt = connection.prepareStatement(occupationSql)) {
                    for (String occupation : activity.getMemberOccupationConcerned()) {
                        occStmt.setString(1, id);
                        occStmt.setString(2, occupation);
                        occStmt.addBatch();
                    }
                    occStmt.executeBatch();
                }
            }

            return activity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save activity " + e.getMessage());
        }
    }

    public List<Activity> findByCollectivityId(String collectivityId) {
        String sql = """
            SELECT id, id_collectivity, label, activity_type, executive_date, 
                   week_ordinal, day_of_week, creation_date
            FROM activity
            WHERE id_collectivity = ?
            ORDER BY creation_date DESC
        """;

        List<Activity> activities = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Activity activity = mapActivity(rs);
                // Load occupations concerned
                activity.setMemberOccupationConcerned(findOccupationsByActivityId(activity.getId()));
                activities.add(activity);
            }
            return activities;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find activities for collectivity: " + collectivityId + e.getMessage());
        }
    }

    public Activity findById(String activityId) {
        String sql = """
            SELECT id, id_collectivity, label, activity_type, executive_date, 
                   week_ordinal, day_of_week, creation_date
            FROM activity
            WHERE id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, activityId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Activity activity = mapActivity(rs);
                activity.setMemberOccupationConcerned(findOccupationsByActivityId(activity.getId()));
                return activity;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find activity: " + activityId + e.getMessage());
        }
    }

    private Activity mapActivity(ResultSet rs) throws SQLException {
        Date execDate = rs.getDate("executive_date");
        return Activity.builder()
                .id(rs.getString("id"))
                .label(rs.getString("label"))
                .activityType(ActivityType.valueOf(rs.getString("activity_type")))
                .executiveDate(execDate != null ? execDate.toLocalDate() : null)
                .weekOrdinal(rs.getObject("week_ordinal") != null ? rs.getInt("week_ordinal") : null)
                .dayOfWeek(rs.getString("day_of_week"))
                .creationDate(rs.getDate("creation_date") != null ? rs.getDate("creation_date").toLocalDate() : null)
                .build();
    }

    private List<String> findOccupationsByActivityId(String activityId) {
        String sql = "SELECT occupation FROM activity_member_occupation WHERE id_activity = ?";
        List<String> occupations = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, activityId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                occupations.add(rs.getString("occupation"));
            }
            return occupations;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find occupations for activity: " + activityId + e.getMessage());
        }
    }


    public List<ActivityAttendance> getAttendance(String activityId, String collectivityId) {
        String sql = """
            SELECT 
                aa.id,
                aa.id_activity,
                aa.id_member,
                aa.attendance_status,
                m.first_name,
                m.last_name,
                m.email,
                m.gender,
                m.phone_number,
                mc.occupation
            FROM activity_attendance aa
            JOIN member m ON aa.id_member = m.id
            LEFT JOIN member_collectivity mc ON m.id = mc.id_member 
                AND mc.id_collectivity = ? 
                AND mc.end_date IS NULL
            WHERE aa.id_activity = ?
            ORDER BY m.id
        """;

        List<ActivityAttendance> attendances = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            stmt.setString(2, activityId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Member member = Member.builder()
                        .id(rs.getString("id_member"))
                        .firstName(rs.getString("first_name"))
                        .lastName(rs.getString("last_name"))
                        .email(rs.getString("email"))
                        .gender(Gender.valueOf(rs.getString("gender")))
                        .phoneNumber(rs.getString("phone_number"))
                        .build();

                ActivityAttendance attendance = ActivityAttendance.builder()
                        .id(rs.getString("id"))
                        .activityId(rs.getString("id_activity"))
                        .member(member)
                        .attendanceStatus(AttendanceStatus.valueOf(rs.getString("attendance_status")))
                        .build();

                attendances.add(attendance);
            }
            return attendances;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get attendance for activity: " + activityId + e.getMessage());
        }
    }

    /**
     * Initialize UNDEFINED attendance for all members concerned by the activity
     */
    public void initializeAttendance(String activityId, String collectivityId, List<String> concernedOccupations) {
        String sql = """
            INSERT INTO activity_attendance (id, id_activity, id_member, attendance_status)
            SELECT ?, ?, mc.id_member, 'UNDEFINED'
            FROM member_collectivity mc
            WHERE mc.id_collectivity = ?
            AND mc.end_date IS NULL
            AND mc.occupation = ANY(?)
            AND NOT EXISTS (
                SELECT 1 FROM activity_attendance aa 
                WHERE aa.id_activity = ? AND aa.id_member = mc.id_member
            )
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            // For each member, create an UNDEFINED attendance record
            String memberSql = """
                SELECT mc.id_member
                FROM member_collectivity mc
                WHERE mc.id_collectivity = ?
                AND mc.end_date IS NULL
                AND mc.occupation = ANY(?)
                AND NOT EXISTS (
                    SELECT 1 FROM activity_attendance aa 
                    WHERE aa.id_activity = ? AND aa.id_member = mc.id_member
                )
            """;

            try (PreparedStatement memberStmt = connection.prepareStatement(memberSql)) {
                memberStmt.setString(1, collectivityId);
                memberStmt.setArray(2, connection.createArrayOf("VARCHAR", concernedOccupations.toArray()));
                memberStmt.setString(3, activityId);

                ResultSet rs = memberStmt.executeQuery();

                String insertSql = """
                    INSERT INTO activity_attendance (id, id_activity, id_member, attendance_status)
                    VALUES (?, ?, ?, 'UNDEFINED')
                """;

                try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                    while (rs.next()) {
                        String memberId = rs.getString("id_member");
                        String attId = "att-init-" + activityId + "-" + memberId;
                        insertStmt.setString(1, attId);
                        insertStmt.setString(2, activityId);
                        insertStmt.setString(3, memberId);
                        insertStmt.addBatch();
                    }
                    insertStmt.executeBatch();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize attendance " + e.getMessage());
        }
    }


    public List<ActivityAttendance> saveAttendance(String activityId, List<ActivityAttendance> attendances) {
        List<ActivityAttendance> savedAttendances = new ArrayList<>();

        String checkSql = """
            SELECT id, attendance_status FROM activity_attendance 
            WHERE id_activity = ? AND id_member = ?
        """;

        String updateSql = """
            UPDATE activity_attendance 
            SET attendance_status = ? 
            WHERE id_activity = ? AND id_member = ? AND attendance_status = 'UNDEFINED'
            RETURNING id
        """;

        String insertSql = """
            INSERT INTO activity_attendance (id, id_activity, id_member, attendance_status)
            VALUES (?, ?, ?, ?)
        """;

        try {
            connection.setAutoCommit(false);

            for (ActivityAttendance attendance : attendances) {
                // Check current status
                try (PreparedStatement checkStmt = connection.prepareStatement(checkSql)) {
                    checkStmt.setString(1, activityId);
                    checkStmt.setString(2, attendance.getMember().getId());
                    ResultSet rs = checkStmt.executeQuery();

                    if (rs.next()) {
                        String currentStatus = rs.getString("attendance_status");
                        String existingId = rs.getString("id");

                        if ("UNDEFINED".equals(currentStatus)) {
                            // Allow update from UNDEFINED to ATTENDED or MISSING
                            try (PreparedStatement updateStmt = connection.prepareStatement(updateSql)) {
                                updateStmt.setString(1, attendance.getAttendanceStatus().name());
                                updateStmt.setString(2, activityId);
                                updateStmt.setString(3, attendance.getMember().getId());
                                ResultSet updateRs = updateStmt.executeQuery();
                                if (updateRs.next()) {
                                    attendance.setId(updateRs.getString("id"));
                                    savedAttendances.add(attendance);
                                }
                            }
                        } else {
                            // Already confirmed (ATTENDED or MISSING) - cannot modify
                            throw new RuntimeException(
                                    "Attendance already confirmed for member: " + attendance.getMember().getId() +
                                            " with status: " + currentStatus + ". Cannot modify once set."
                            );
                        }
                    } else {
                        // No existing record, insert new one
                        String id = "att-" + System.currentTimeMillis() + "-" + attendance.getMember().getId();
                        try (PreparedStatement insertStmt = connection.prepareStatement(insertSql)) {
                            insertStmt.setString(1, id);
                            insertStmt.setString(2, activityId);
                            insertStmt.setString(3, attendance.getMember().getId());
                            insertStmt.setString(4, attendance.getAttendanceStatus().name());
                            insertStmt.executeUpdate();

                            attendance.setId(id);
                            savedAttendances.add(attendance);
                        }
                    }
                }
            }

            connection.commit();
            return savedAttendances;
        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new RuntimeException("Failed to rollback attendance save", rollbackEx);
            }
            throw new RuntimeException("Failed to save attendance: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new RuntimeException("Failed to rollback attendance save", rollbackEx);
            }
            throw e;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Failed to reset auto-commit");
            }
        }
    }
}