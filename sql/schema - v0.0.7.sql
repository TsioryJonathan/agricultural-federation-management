-- ===========================================================
-- SCHEMA v0.0.7 - Agricultural Federation Management
-- ===========================================================

-- New types for v0.0.6 and v0.0.7
CREATE TYPE "attendance_status" AS ENUM ('UNDEFINED', 'ATTENDED', 'MISSING');
CREATE TYPE "activity_type" AS ENUM ('MEETING', 'TRAINING', 'OTHER');

-- Activity table
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

-- Activity attendance table
CREATE TABLE "public"."activity_attendance" (
    "id"                    varchar        NOT NULL,
    "id_activity"           varchar        NOT NULL,
    "id_member"             varchar        NOT NULL,
    "id_member_collectivity" varchar        NOT NULL,
    "attendance_status"      attendance_status NOT NULL DEFAULT 'UNDEFINED',
    PRIMARY KEY ("id"),
    UNIQUE ("id_activity", "id_member"),
    CONSTRAINT "fk_aa_activity" FOREIGN KEY ("id_activity") 
        REFERENCES "public"."activity"("id") ON DELETE CASCADE,
    CONSTRAINT "fk_aa_member" FOREIGN KEY ("id_member") 
        REFERENCES "public"."member"("id"),
    CONSTRAINT "fk_aa_member_collectivity" FOREIGN KEY ("id_member_collectivity") 
        REFERENCES "public"."member_collectivity"("id")
);
