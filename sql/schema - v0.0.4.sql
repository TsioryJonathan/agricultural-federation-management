-- ============================================================
-- SCHEMA v0.0.4 - Agricultural Federation Management
-- ============================================================

-- enums

CREATE TYPE "collectivity_occupation"  AS ENUM ('PRESIDENT', 'VICE_PRESIDENT', 'TREASURER', 'SECRETARY', 'SENIOR', 'JUNIOR');
CREATE TYPE "federation_occupation"  AS ENUM ('PRESIDENT', 'VICE_PRESIDENT', 'TREASURER', 'SECRETARY');
CREATE TYPE "gender"                  AS ENUM ('MALE', 'FEMALE');
CREATE TYPE "cotisation_frequency"    AS ENUM ('WEEKLY', 'MONTHLY', 'ANNUALLY', 'PUNCTUALLY');
CREATE TYPE "payment_mode"            AS ENUM ('CASH', 'BANK_TRANSFER', 'MOBILE_BANKING');
CREATE TYPE "bank_name"          AS ENUM ('BRED', 'MCB', 'BMOI', 'BOA', 'BGFI', 'AFG', 'ACCES_BANQUE', 'BAOBAB', 'SIPEM');
CREATE TYPE "mobile_money_service"    AS ENUM ('AIRTEL_MONEY', 'MVOLA', 'ORANGE_MONEY');
CREATE TYPE "transaction_type" AS ENUM ('IN', 'OUT');
CREATE TYPE "frequency" AS ENUM ('WEEKLY', 'MONTHLY', 'ANNUALLY', 'PUNCTUALLY');
CREATE TYPE "activity_status" AS ENUM ('ACTIVE', 'INACTIVE');

-- tables

CREATE TABLE "public"."member" (
                                   "id"              varchar     NOT NULL,
                                   "first_name"      varchar     NOT NULL,
                                   "last_name"       varchar     NOT NULL,
                                   "birth_date"      date        NOT NULL,
                                   "enrolment_date"  timestamp   NOT NULL,
                                   "address"         text        NOT NULL,
                                   "email"           varchar     NOT NULL UNIQUE,
                                   "phone_number"    varchar     NOT NULL UNIQUE,
                                   "profession"      varchar     NOT NULL,
                                   "gender"          gender      NOT NULL,
                                   "superuser"       boolean     NOT NULL DEFAULT false,
                                   PRIMARY KEY ("id")
);

CREATE TABLE "public"."location" (
                                     "id"   varchar     NOT NULL,
                                     "name" varchar     NOT NULL,
                                     PRIMARY KEY ("id")
);

CREATE TABLE "public"."federation" (
                                       "id"                    varchar         NOT NULL,
                                       "cotisation_percentage" numeric(5,2)   NOT NULL DEFAULT 10.00,
                                       PRIMARY KEY ("id")
);

CREATE TABLE "public"."collectivity" (
                                         "id"                 varchar                NOT NULL,
                                         "number"             varchar               UNIQUE,
                                         "name"               varchar               UNIQUE,
                                         "speciality"         varchar               ,
                                         "creation_datetime"  timestamp             NOT NULL,
                                         "federation_approval" boolean DEFAULT FALSE,
                                         "authorization_date" timestamp,
                                         "id_federation"      varchar              NOT NULL,
                                         "location"        varchar                   NOT NULL,
                                         PRIMARY KEY ("id")
);

CREATE TABLE "public"."member_collectivity" (
                                         "id"              varchar                 NOT NULL,
                                         "id_member"       varchar                NOT NULL,
                                         "id_collectivity" varchar                NOT NULL,
                                         "occupation"      collectivity_occupation  NOT NULL,
                                         "start_date"      timestamp                 NOT NULL,
                                         "end_date"        timestamp,
                                         PRIMARY KEY ("id"),
                                         UNIQUE ("id_member", "id_collectivity")
);

CREATE TABLE "public"."member_referee" (
                                         "id"              varchar      NOT NULL,
                                         "id_candidate"    varchar      NOT NULL,
                                         "id_referee"      varchar      NOT NULL,
                                         "id_collectivity" varchar      NOT NULL,
                                         "relationship"    varchar     NOT NULL,
                                         "created_at"      timestamp   NOT NULL DEFAULT NOW(),
                                         PRIMARY KEY ("id"),
                                         UNIQUE ("id_candidate", "id_referee")
);

CREATE TABLE "public"."mandate_federation" (
                                       "id"            varchar                  NOT NULL,
                                       "id_member"     varchar                 NOT NULL,
                                       "id_federation" varchar                 NOT NULL,
                                       "occupation"    federation_occupation   NOT NULL,
                                       "start_date"    timestamp               NOT NULL,
                                       "end_date"      timestamp,
                                       PRIMARY KEY ("id")
);

-- Membership Fee (cotisation_plan)
CREATE TABLE "public"."cotisation_plan" (
                                     "id"              varchar                NOT NULL,
                                     "id_collectivity" varchar               NOT NULL,
                                     "label"           varchar               NOT NULL,
                                     "frequency"       frequency            NOT NULL,
                                     "amount"          numeric(15,2)         NOT NULL,
                                     "eligible_from"    date,
                                     "is_active"       boolean               NOT NULL DEFAULT true,
                                     PRIMARY KEY ("id")
);

-- Transaction
CREATE TABLE "public"."transaction" (
                                     "id"                  varchar          NOT NULL,
                                     "id_member"           varchar         NOT NULL,
                                     "id_collectivity"     varchar         NOT NULL,
                                     "id_cotisation_plan"  varchar,
                                     "id_account"          varchar        NOT NULL,
                                     "transaction_type"    transaction_type   NOT NULL DEFAULT 'IN',
                                     "amount"              numeric(15,2)   NOT NULL,
                                     "transaction_date"    timestamp       NOT NULL DEFAULT NOW(),
                                     "payment_mode"        payment_mode,
                                     "description"         text,
                                     PRIMARY KEY ("id")
);

-- Account (mère)
CREATE TABLE "public"."account" (
                                     "id"              varchar  NOT NULL,
                                     "id_collectivity" varchar,
                                     "id_federation"   varchar,
                                     PRIMARY KEY ("id"),
                                     CONSTRAINT "chk_account_owner" CHECK (
                                         ("id_collectivity" IS NOT NULL AND "id_federation" IS NULL) OR
                                         ("id_collectivity" IS NULL AND "id_federation" IS NOT NULL)
                                         )
);

-- Cash Account
CREATE TABLE "public"."cash_account" (
                                     "id"          varchar NOT NULL,
                                     "id_account"  varchar NOT NULL UNIQUE,

                                     PRIMARY KEY ("id")
);

-- Bank Account
CREATE TABLE "public"."bank_account" (
                                     "id"             varchar           NOT NULL,
                                     "id_account"     varchar           NOT NULL UNIQUE,
                                     "holder_name"    varchar          NOT NULL,
                                     "bank_name"      bank_name   NOT NULL,
                                     "bank_code"      char(5)          NOT NULL,
                                     "branch_code"    char(5)          NOT NULL,
                                     "account_number" char(11)         NOT NULL,
                                     "rib_key"        char(2)          NOT NULL,

                                     PRIMARY KEY ("id")
);

-- Mobile Money Account
CREATE TABLE "public"."mobile_money_account" (
                                       "id"           varchar               NOT NULL,
                                       "id_account"   varchar               NOT NULL UNIQUE,
                                       "holder_name"  varchar              NOT NULL,
                                       "service_name" mobile_money_service NOT NULL,
                                       "phone_number" varchar              NOT NULL UNIQUE,

                                       PRIMARY KEY ("id")
);

-- constraints

ALTER TABLE "public"."collectivity"
    ADD CONSTRAINT "fk_collectivity_federation"
        FOREIGN KEY (id_federation)
            REFERENCES "public"."federation"("id");

ALTER TABLE "public"."member_collectivity"
    ADD CONSTRAINT "fk_member_collectivity_member"
        FOREIGN KEY ("id_member")
            REFERENCES "public"."member"("id"),
    ADD CONSTRAINT "fk_member_collectivity_collectivity"
        FOREIGN KEY ("id_collectivity")
        REFERENCES "public"."collectivity"("id");

ALTER TABLE "public"."member_referee"
    ADD CONSTRAINT "fk_member_referee_candidate"
        FOREIGN KEY ("id_candidate")
            REFERENCES "public"."member"("id"),
    ADD CONSTRAINT "fk_member_referee_referee"
        FOREIGN KEY ("id_referee")
            REFERENCES "public"."member"("id"),
    ADD CONSTRAINT "fk_member_referee_collectivity"
        FOREIGN KEY ("id_collectivity")
        REFERENCES "public"."collectivity"("id");

ALTER TABLE "public"."mandate_federation"
    ADD CONSTRAINT "fk_mandate_federation_member"
        FOREIGN KEY ("id_member")
            REFERENCES "public"."member"("id"),
    ADD CONSTRAINT "fk_mandate_federation_federation"
        FOREIGN KEY ("id_federation")
        REFERENCES "public"."federation"("id");

ALTER TABLE "public"."cotisation_plan"
    ADD CONSTRAINT "fk_cotisation_plan_collectivity"
        FOREIGN KEY ("id_collectivity")
        REFERENCES "public"."collectivity"("id");

ALTER TABLE "public"."transaction"
    ADD CONSTRAINT "fk_transaction_member"
        FOREIGN KEY ("id_member")
            REFERENCES "public"."member"("id"),
    ADD CONSTRAINT "fk_transaction_collectivity"
        FOREIGN KEY ("id_collectivity")
        REFERENCES "public"."collectivity"("id"),
    ADD CONSTRAINT "fk_transaction_cotisation_plan"
        FOREIGN KEY ("id_cotisation_plan")
        REFERENCES "public"."cotisation_plan"("id"),
    ADD CONSTRAINT "fk_transaction_account"
        FOREIGN KEY ("id_account")
        REFERENCES "public"."account"("id");

ALTER TABLE "public"."account"
    ADD CONSTRAINT "fk_account_collectivity"
        FOREIGN KEY ("id_collectivity")
        REFERENCES "public"."collectivity"("id"),
    ADD CONSTRAINT "fk_account_federation"
        FOREIGN KEY ("id_federation")
        REFERENCES "public"."federation"("id");

ALTER TABLE "public"."cash_account"
    ADD CONSTRAINT "fk_cash_account"
        FOREIGN KEY ("id_account")
        REFERENCES "public"."account"("id");

ALTER TABLE "public"."bank_account"
    ADD CONSTRAINT "fk_bank_account"
        FOREIGN KEY ("id_account")
        REFERENCES "public"."account"("id");

ALTER TABLE "public"."mobile_money_account"
    ADD CONSTRAINT "fk_mobile_account"
        FOREIGN KEY ("id_account")
        REFERENCES "public"."account"("id");