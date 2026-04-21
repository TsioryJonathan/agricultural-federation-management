-- ============================================================
-- TYPES (ENUMS)
-- ============================================================

CREATE TYPE "collectivity_post_name"  AS ENUM ('PRESIDENT', 'DEPUTY_PRESIDENT', 'TREASURER', 'SECRETARY', 'CONFIRMED', 'JUNIOR');
CREATE TYPE "federation_post_name"    AS ENUM ('PRESIDENT', 'DEPUTY_PRESIDENT', 'TREASURER', 'SECRETARY');
CREATE TYPE "gender"                  AS ENUM ('MALE', 'FEMALE');
CREATE TYPE "payment_mode"            AS ENUM ('CASH', 'BANK_TRANSFER', 'MOBILE_MONEY');
CREATE TYPE "cotisation_frequency"    AS ENUM ('MONTHLY', 'ANNUAL', 'PUNCTUAL');
CREATE TYPE "account_type"            AS ENUM ('CASH', 'BANK', 'MOBILE_MONEY');
CREATE TYPE "bank_name_enum"          AS ENUM ('BRED', 'MCB', 'BMOI', 'BOA', 'BGFI', 'AFG', 'ACCES_BANQUE', 'BAOBAB', 'SIPEM');
CREATE TYPE "mobile_money_service"    AS ENUM ('ORANGE_MONEY', 'MVOLA', 'AIRTEL_MONEY');
CREATE TYPE "activity_type"           AS ENUM ('MONTHLY_GA', 'JUNIOR_TRAINING', 'EXCEPTIONAL');
CREATE TYPE "collectivity_status"     AS ENUM ('PENDING', 'APPROVED', 'REJECTED');
CREATE TYPE "movement_direction"      AS ENUM ('IN', 'OUT');

-- ============================================================
-- BASE TABLES
-- ============================================================

CREATE TABLE "public"."city" (
    "id"   serial      NOT NULL,
    "name" varchar     NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."federation" (
    "id"                    serial         NOT NULL,
    -- % of periodic cotisations forwarded to the federation (Section C)
    "cotisation_percentage" numeric(5,2)   NOT NULL DEFAULT 10.00,
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."member" (
    "id"              serial      NOT NULL,
    "first_name"      varchar     NOT NULL,
    "last_name"       varchar     NOT NULL,
    "birth_date"      date        NOT NULL,
    "enrolment_date"  timestamp   NOT NULL,
    "address"         text        NOT NULL,
    "email"           varchar     NOT NULL UNIQUE,
    "phone"           varchar     NOT NULL UNIQUE,
    "job"             varchar     NOT NULL,
    "gender"          gender      NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."collectivity" (
    "id"                 serial                NOT NULL,
    "number"             varchar               NOT NULL UNIQUE,
    "name"               varchar               NOT NULL UNIQUE,
    "speciality"         varchar               NOT NULL,
    "creation_datetime"  timestamp             NOT NULL,
    "status"             collectivity_status   NOT NULL DEFAULT 'PENDING',
    "authorization_date" timestamp,
    "id_federation"      int                   NOT NULL,
    "id_city"            int                   NOT NULL,
    PRIMARY KEY ("id")
);

-- ============================================================
-- SECTION A/B – Membership and posts
-- member_collectivity: history of posts + membership in a collectivity
-- ============================================================

CREATE TABLE "public"."member_collectivity" (
    "id"              serial                    NOT NULL,
    "id_member"       int                       NOT NULL,
    "id_collectivity" int                       NOT NULL,
    "post_name"       collectivity_post_name    NOT NULL,
    "start_date"      timestamp                 NOT NULL,
    "end_date"        timestamp,                -- NULL = active; set = mandate ended or resigned
    PRIMARY KEY ("id")
);

-- ============================================================
-- SECTION B-2 – Multiple sponsorship (updated admission conditions)
-- ============================================================

CREATE TABLE "public"."sponsorship" (
    "id"              serial      NOT NULL,
    "id_candidate"    int         NOT NULL,   -- member being admitted
    "id_sponsor"      int         NOT NULL,   -- confirmed member acting as sponsor
    "id_collectivity" int         NOT NULL,   -- target collectivity of the candidate
    "relationship"    varchar     NOT NULL,   -- family, friend, colleague, etc.
    "created_at"      timestamp   NOT NULL DEFAULT NOW(),
    PRIMARY KEY ("id"),
    UNIQUE ("id_candidate", "id_sponsor")
);

-- ============================================================
-- SECTION A – Federation mandate (2 years)
-- ============================================================

CREATE TABLE "public"."mandate_federation" (
    "id"            serial                  NOT NULL,
    "id_member"     int                     NOT NULL,
    "id_federation" int                     NOT NULL,
    "post_name"     federation_post_name    NOT NULL,
    "start_date"    timestamp               NOT NULL,
    "end_date"      timestamp,
    PRIMARY KEY ("id")
);

-- ============================================================
-- SECTION C – Cotisation plans & payments
-- ============================================================

-- Cotisation plan defined by the collectivity (e.g. annual cotisation 200,000 MGA)
CREATE TABLE "public"."cotisation_plan" (
    "id"              serial                NOT NULL,
    "id_collectivity" int                   NOT NULL,
    "label"           varchar               NOT NULL,
    "frequency"       cotisation_frequency  NOT NULL,
    "amount"          numeric(15,2)         NOT NULL,
    "year"            int,
    "is_active"       boolean               NOT NULL DEFAULT true,
    PRIMARY KEY ("id")
);

-- Payment: records each payment made by a member (Section C)
CREATE TABLE "public"."payment" (
    "id"                  serial          NOT NULL,
    "id_member"           int             NOT NULL,
    "id_collectivity"     int             NOT NULL,
    "id_cotisation_plan"  int,            -- NULL = membership fee or free punctual payment
    "id_account"          int             NOT NULL,   -- account receiving the payment (Section D)
    "amount"              numeric(15,2)   NOT NULL,
    "payment_date"        timestamp       NOT NULL,
    "payment_mode"        payment_mode    NOT NULL,
    "recorded_by"         int             NOT NULL,   -- treasurer who recorded the entry
    PRIMARY KEY ("id")
);

-- ============================================================
-- SECTION D – Accounts (cash, bank, mobile money)
-- ============================================================

-- Parent table: an account belongs to either a collectivity or the federation
CREATE TABLE "public"."account" (
    "id"              serial          NOT NULL,
    "account_type"    account_type    NOT NULL,
    "id_collectivity" int,
    "id_federation"   int,
    PRIMARY KEY ("id"),
    -- Constraint: exactly one owner
    CONSTRAINT "chk_account_owner" CHECK (
        ("id_collectivity" IS NOT NULL AND "id_federation" IS NULL) OR
        ("id_collectivity" IS NULL  AND "id_federation"   IS NOT NULL)
    )
);

-- Unique cash account per owner (only one cash register per collectivity/federation)
CREATE UNIQUE INDEX "uq_cash_per_collectivity"
    ON "public"."account" ("id_collectivity")
    WHERE "account_type" = 'CASH' AND "id_collectivity" IS NOT NULL;

CREATE UNIQUE INDEX "uq_cash_per_federation"
    ON "public"."account" ("id_federation")
    WHERE "account_type" = 'CASH' AND "id_federation" IS NOT NULL;

-- ============================================================
-- SECTION D – Account movements (replaces balance field)
-- Each financial transaction is recorded as a movement IN or OUT
-- ============================================================

CREATE TABLE "public"."account_movement" (
    "id"              serial                NOT NULL,
    "id_account"      int                   NOT NULL,
    "direction"       movement_direction    NOT NULL,   -- IN = credit, OUT = debit
    "amount"          numeric(15,2)         NOT NULL,
    "label"           varchar               NOT NULL,   -- description of the movement
    "movement_date"   timestamp             NOT NULL DEFAULT NOW(),
    "id_payment"      int,                             -- link to payment if applicable (nullable)
    PRIMARY KEY ("id")
);

-- Bank account details (RIB breakdown)
CREATE TABLE "public"."bank_account" (
    "id"             serial           NOT NULL,
    "id_account"     int              NOT NULL UNIQUE,
    "holder_name"    varchar          NOT NULL,
    "bank_name"      bank_name_enum   NOT NULL,
    "bank_code"      char(5)          NOT NULL,   -- BBBBB
    "branch_code"    char(5)          NOT NULL,   -- GGGGG
    "account_number" char(11)         NOT NULL,   -- CCCCCCCCCCC
    "rib_key"        char(2)          NOT NULL,   -- KK
    PRIMARY KEY ("id")
);

-- Mobile money account details
CREATE TABLE "public"."mobile_money_account" (
    "id"           serial                  NOT NULL,
    "id_account"   int                     NOT NULL UNIQUE,
    "holder_name"  varchar                 NOT NULL,
    "service_name" mobile_money_service    NOT NULL,
    "phone_number" varchar                 NOT NULL UNIQUE,
    PRIMARY KEY ("id")
);

-- ============================================================
-- SECTION E – Activities
-- ============================================================

CREATE TABLE "public"."activity" (
    "id"               serial          NOT NULL,
    "title"            varchar         NOT NULL,
    "description"      text,
    "activity_date"    timestamp       NOT NULL,
    "activity_type"    activity_type   NOT NULL,
    "is_mandatory_all" boolean         NOT NULL DEFAULT false,
    "id_collectivity"  int,   -- NULL if federation activity
    "id_federation"    int,   -- NULL if collectivity activity
    PRIMARY KEY ("id"),
    CONSTRAINT "chk_activity_owner" CHECK (
        ("id_collectivity" IS NOT NULL AND "id_federation" IS NULL) OR
        ("id_collectivity" IS NULL  AND "id_federation"   IS NOT NULL)
    )
);

-- For exceptional activities: specific posts required to attend
CREATE TABLE "public"."activity_mandatory_role" (
    "id"          serial                    NOT NULL,
    "id_activity" int                       NOT NULL,
    "post_name"   collectivity_post_name    NOT NULL,
    PRIMARY KEY ("id"),
    UNIQUE ("id_activity", "post_name")
);

-- ============================================================
-- SECTION F – Attendance sheet
-- ============================================================

CREATE TABLE "public"."attendance" (
    "id"                     serial    NOT NULL,
    "id_activity"            int       NOT NULL,
    "id_member"              int       NOT NULL,
    "is_present"             boolean   NOT NULL DEFAULT false,
    "is_excused"             boolean   NOT NULL DEFAULT false,
    "excuse_reason"          text,
    -- Collectivity membership of the member at the time of the activity
    -- (allows distinguishing guests from other collectivities – Section F)
    "id_member_collectivity" int       NOT NULL,
    PRIMARY KEY ("id"),
    UNIQUE ("id_activity", "id_member")
);

-- ============================================================
-- FOREIGN KEY CONSTRAINTS
-- ============================================================

ALTER TABLE "public"."collectivity"
    ADD CONSTRAINT "fk_collectivity_federation"    FOREIGN KEY ("id_federation")   REFERENCES "public"."federation"("id"),
    ADD CONSTRAINT "fk_collectivity_city"          FOREIGN KEY ("id_city")          REFERENCES "public"."city"("id");

ALTER TABLE "public"."member_collectivity"
    ADD CONSTRAINT "fk_mc_member"                  FOREIGN KEY ("id_member")        REFERENCES "public"."member"("id"),
    ADD CONSTRAINT "fk_mc_collectivity"            FOREIGN KEY ("id_collectivity")  REFERENCES "public"."collectivity"("id");

ALTER TABLE "public"."mandate_federation"
    ADD CONSTRAINT "fk_mf_member"                  FOREIGN KEY ("id_member")        REFERENCES "public"."member"("id"),
    ADD CONSTRAINT "fk_mf_federation"              FOREIGN KEY ("id_federation")    REFERENCES "public"."federation"("id");

ALTER TABLE "public"."sponsorship"
    ADD CONSTRAINT "fk_sp_candidate"               FOREIGN KEY ("id_candidate")     REFERENCES "public"."member"("id"),
    ADD CONSTRAINT "fk_sp_sponsor"                 FOREIGN KEY ("id_sponsor")       REFERENCES "public"."member"("id"),
    ADD CONSTRAINT "fk_sp_collectivity"            FOREIGN KEY ("id_collectivity")  REFERENCES "public"."collectivity"("id");

ALTER TABLE "public"."cotisation_plan"
    ADD CONSTRAINT "fk_cp_collectivity"            FOREIGN KEY ("id_collectivity")  REFERENCES "public"."collectivity"("id");

ALTER TABLE "public"."payment"
    ADD CONSTRAINT "fk_pay_member"                 FOREIGN KEY ("id_member")        REFERENCES "public"."member"("id"),
    ADD CONSTRAINT "fk_pay_collectivity"           FOREIGN KEY ("id_collectivity")  REFERENCES "public"."collectivity"("id"),
    ADD CONSTRAINT "fk_pay_cotisation_plan"        FOREIGN KEY ("id_cotisation_plan") REFERENCES "public"."cotisation_plan"("id"),
    ADD CONSTRAINT "fk_pay_account"                FOREIGN KEY ("id_account")       REFERENCES "public"."account"("id"),
    ADD CONSTRAINT "fk_pay_recorded_by"            FOREIGN KEY ("recorded_by")      REFERENCES "public"."member"("id");

ALTER TABLE "public"."account"
    ADD CONSTRAINT "fk_acc_collectivity"           FOREIGN KEY ("id_collectivity")  REFERENCES "public"."collectivity"("id"),
    ADD CONSTRAINT "fk_acc_federation"             FOREIGN KEY ("id_federation")    REFERENCES "public"."federation"("id");

ALTER TABLE "public"."account_movement"
    ADD CONSTRAINT "fk_mv_account"                FOREIGN KEY ("id_account")       REFERENCES "public"."account"("id"),
    ADD CONSTRAINT "fk_mv_payment"                FOREIGN KEY ("id_payment")       REFERENCES "public"."payment"("id");

ALTER TABLE "public"."bank_account"
    ADD CONSTRAINT "fk_ba_account"                FOREIGN KEY ("id_account")       REFERENCES "public"."account"("id");

ALTER TABLE "public"."mobile_money_account"
    ADD CONSTRAINT "fk_mma_account"               FOREIGN KEY ("id_account")       REFERENCES "public"."account"("id");

ALTER TABLE "public"."activity"
    ADD CONSTRAINT "fk_act_collectivity"           FOREIGN KEY ("id_collectivity")  REFERENCES "public"."collectivity"("id"),
    ADD CONSTRAINT "fk_act_federation"             FOREIGN KEY ("id_federation")    REFERENCES "public"."federation"("id");

ALTER TABLE "public"."activity_mandatory_role"
    ADD CONSTRAINT "fk_amr_activity"              FOREIGN KEY ("id_activity")      REFERENCES "public"."activity"("id");

ALTER TABLE "public"."attendance"
    ADD CONSTRAINT "fk_att_activity"              FOREIGN KEY ("id_activity")           REFERENCES "public"."activity"("id"),
    ADD CONSTRAINT "fk_att_member"                FOREIGN KEY ("id_member")             REFERENCES "public"."member"("id"),
    ADD CONSTRAINT "fk_att_member_collectivity"   FOREIGN KEY ("id_member_collectivity") REFERENCES "public"."member_collectivity"("id");