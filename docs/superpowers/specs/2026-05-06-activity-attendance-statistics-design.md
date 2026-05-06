# Design: Activity, Attendance & Statistics (OAS v0.0.7)

## Overview
Implement Bonus 1 (v0.0.6) and Bonus 2 (v0.0.7) features:
- **Activities**: Create and retrieve collectivity activities with recurrence rules
- **Attendance**: Track member attendance (UNDEFINED, ATTENDED, MISSING) with immutable confirmation
- **Statistics**: Add assiduity percentages to local and overall statistics

## Database Changes (schema - v0.0.7.sql)

### New Types
```sql
CREATE TYPE "attendance_status" AS ENUM ('UNDEFINED', 'ATTENDED', 'MISSING');
CREATE TYPE "activity_type" AS ENUM ('MEETING', 'TRAINING', 'OTHER');
```

### New Tables

**activity** (replaces existing one from v0.0.1):
```sql
CREATE TABLE "public"."activity" (
    "id"               varchar        NOT NULL,
    "label"            varchar         NOT NULL,
    "activity_type"    activity_type   NOT NULL,
    "member_occupation_concerned" collectivity_occupation[],
    "recurrence_rule"   jsonb,
    "executive_date"    date,
    "id_collectivity"  varchar,
    "id_federation"    varchar,
    PRIMARY KEY ("id"),
    CONSTRAINT "chk_activity_owner" CHECK (
        ("id_collectivity" IS NOT NULL AND "id_federation" IS NULL) OR
        ("id_collectivity" IS NULL AND "id_federation" IS NOT NULL)
    )
);
```

**activity_attendance**:
```sql
CREATE TABLE "public"."activity_attendance" (
    "id"                    varchar        NOT NULL,
    "id_activity"           varchar        NOT NULL,
    "id_member"             varchar        NOT NULL,
    "id_member_collectivity" varchar        NOT NULL,
    "attendance_status"     attendance_status NOT NULL DEFAULT 'UNDEFINED',
    PRIMARY KEY ("id"),
    UNIQUE ("id_activity", "id_member"),
    CONSTRAINT "fk_aa_activity" FOREIGN KEY ("id_activity") REFERENCES "public"."activity"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_aa_member" FOREIGN KEY ("id_member") REFERENCES "public"."member"("id"),
    CONSTRAINT "fk_aa_member_collectivity" FOREIGN KEY ("id_member_collectivity") REFERENCES "public"."member_collectivity"("id")
);
```

### Modified Tables
- `collectivity_activity` view/materialized view if needed for DTO mapping
- Statistics queries updated to include assiduity calculation

## Repository Layer (ActivityRepository)

### Methods
```java
// Activities
List<CollectivityActivity> findByCollectivityId(String collectivityId);
Optional<CollectivityActivity> findById(String activityId);
void saveAll(List<CollectivityActivity> activities);

// Attendance
List<ActivityMemberAttendance> findAttendanceByActivityId(String activityId);
void saveAttendance(List<ActivityMemberAttendance> attendanceList);
boolean hasConfirmedAttendance(String activityId, String memberId);
```

### SQL Mapping
- `activity.attendance` column stores `attendance_status` enum
- `member_occupation_concerned` stored as PostgreSQL array
- `recurrence_rule` stored as JSONB (weekOrdinal, dayOfWeek)

## Service Layer (ActivityService)

### Activity Management
```java
// Create activities for a collectivity
List<CollectivityActivity> createActivities(String collectivityId, List<CreateCollectivityActivity> requests);

// Retrieve activities
List<CollectivityActivity> getActivities(String collectivityId);

// Attendance confirmation (UNDEFINED only can be updated)
List<ActivityMemberAttendance> confirmAttendance(String activityId, List<CreateActivityMemberAttendance> requests);

// Get attendance list
List<ActivityMemberAttendance> getAttendance(String activityId);
```

### Validation Rules
- Cannot provide both `recurrenceRule` and `executiveDate`
- `attendanceStatus` can only be set from UNDEFINED to ATTENDED/MISSING once
- Members outside collectivity can only have ATTENDED status
- Members with concerned occupation get UNDEFINED status automatically

## Controller Layer (CollectivityController additions)

### Endpoints
```
POST   /collectivities/{id}/activities
GET    /collectivities/{id}/activities
POST   /collectivities/{id}/activities/{activityId}/attendance
GET    /collectivities/{id}/activities/{activityId}/attendance
```

### Request/Response
- Use DTOs already created: `CreateCollectivityActivity`, `CollectivityActivity`, `CreateActivityMemberAttendance`, `ActivityMemberAttendance`
- Return 201 for creation, 200 for retrieval, 400 for invalid data

## Statistics Updates (CollectivityService)

### Local Statistics (GET /collectivities/{id}/statistics)
Added field: `assiduityPercentage` (Double)
- Calculate based on: (attended activities / total mandatory activities) * 100
- Only count activities where member was required (occupation concerned or mandatory_all)

### Overall Statistics (GET /collectivities/statistics)
Added field: `overallMemberAssiduityPercentage` (Double)
- Average assiduity across all members in all collectivities
- Based on same logic as local statistics

## Assiduity Calculation Logic

```java
// For each member in collectivity
Double calculateAssiduity(String memberId, String collectivityId, LocalDate from, LocalDate to) {
    List<Activity> activities = getActivitiesForPeriod(collectivityId, from, to);
    long totalRequired = activities.stream()
        .filter(a -> isMemberRequired(a, memberId))
        .count();
    
    long attended = attendanceRepository.countByMemberAndStatus(memberId, ATTENDED);
    
    return totalRequired > 0 ? (attended * 100.0) / totalRequired : 0.0;
}
```

## Testing Strategy
1. **Unit tests**: Service validation logic (attendance immutability, activity creation rules)
2. **Integration tests**: Repository SQL mapping (JSONB recurrence, enum arrays)
3. **API tests**: Full endpoint testing with Postman/curl
4. **Statistics tests**: Verify assiduity percentage calculations

## Migration Strategy
1. Create `schema - v0.0.7.sql` with all changes
2. Run migration on dev database
3. Update `schema.sql` to latest version
4. No data migration needed (new feature)

## Files to Create/Modify

### New Files
- `ActivityRepository.java`
- `ActivityService.java`
- `Activity.java` (entity)
- `ActivityAttendance.java` (entity)
- `MonthlyRecurrenceRule.java` (entity)
- `schema - v0.0.7.sql`

### Modify
- `CollectivityController.java` (add activity endpoints)
- `CollectivityService.java` (update statistics methods)
- `CollectivityLocalStatistics.java` (add assiduityPercentage)
- `CollectivityOverallStatistics.java` (add overallMemberAssiduityPercentage)
- `pom.xml` (if new dependencies needed)
