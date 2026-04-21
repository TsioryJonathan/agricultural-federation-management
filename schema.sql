-- ============================================================
-- TYPES (ENUMS)
-- ============================================================

CREATE TYPE "collectivity_post_name"  AS ENUM ('PRESIDENT', 'DEPUTY_PRESIDENT', 'TREASURER', 'SECRETARY', 'CONFIRMED', 'JUNIOR');
CREATE TYPE "federation_post_name"    AS ENUM ('PRESIDENT', 'DEPUTY_PRESIDENT', 'TREASURER', 'SECRETARY');
CREATE TYPE "gender"                  AS ENUM ('MALE', 'FEMALE');
CREATE TYPE "payment_mode"            AS ENUM ('CASH', 'BANK_TRANSFER', 'MOBILE_MONEY');
CREATE TYPE "cotisation_frequency"    AS ENUM ('MONTHLY', 'ANNUAL', 'PUNCTUAL');
CREATE TYPE "bank_name_enum"          AS ENUM ('BRED', 'MCB', 'BMOI', 'BOA', 'BGFI', 'AFG', 'ACCES_BANQUE', 'BAOBAB', 'SIPEM');
CREATE TYPE "mobile_money_service"    AS ENUM ('ORANGE_MONEY', 'MVOLA', 'AIRTEL_MONEY');
CREATE TYPE "activity_type"           AS ENUM ('MONTHLY_GA', 'JUNIOR_TRAINING', 'EXCEPTIONAL');
CREATE TYPE "collectivity_status"     AS ENUM ('PENDING', 'APPROVED', 'REJECTED');

-- ============================================================
-- TABLES DE BASE
-- ============================================================

CREATE TABLE "public"."city" (
    "id"   serial      NOT NULL,
    "name" varchar     NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."federation" (
    "id"                    serial         NOT NULL,
    -- % des cotisations périodiques reversé à la fédération (Section C)
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
    "authorization_date" timestamp,                        -- Section A : date d'autorisation
    "id_federation"      int                   NOT NULL,
    "id_city"            int                   NOT NULL,
    PRIMARY KEY ("id")
);

-- ============================================================
-- SECTION A/B  –  Adhésion et postes
-- member_collectivity : historique des postes + appartenance à une collectivité
-- ============================================================

CREATE TABLE "public"."member_collectivity" (
    "id"              serial                    NOT NULL,
    "id_member"       int                       NOT NULL,
    "id_collectivity" int                       NOT NULL,
    "post_name"       collectivity_post_name    NOT NULL,
    "start_date"      timestamp                 NOT NULL,
    "end_date"        timestamp,                           -- NULL = actif ; renseigné = mandat terminé ou démission
    PRIMARY KEY ("id")
);

-- ============================================================
-- SECTION B-2 – Parrainage multiple (nouvelles conditions d'admission)
-- ============================================================

CREATE TABLE "public"."sponsorship" (
    "id"              serial      NOT NULL,
    "id_candidate"    int         NOT NULL,   -- membre en cours d'admission
    "id_sponsor"      int         NOT NULL,   -- membre confirmé parrain
    "id_collectivity" int         NOT NULL,   -- collectivité visée par le candidat
    "relationship"    varchar     NOT NULL,   -- famille, ami, collègue, etc.
    "created_at"      timestamp   NOT NULL DEFAULT NOW(),
    PRIMARY KEY ("id"),
    UNIQUE ("id_candidate", "id_sponsor")
);

-- ============================================================
-- SECTION A  –  Mandat de la fédération (2 ans)
-- ============================================================

CREATE TABLE "public"."mandate_federation" (
    "id"           serial                  NOT NULL,
    "id_member"    int                     NOT NULL,
    "id_federation" int                    NOT NULL,
    "post_name"    federation_post_name    NOT NULL,
    "start_date"   timestamp               NOT NULL,
    "end_date"     timestamp,
    PRIMARY KEY ("id")
);

-- ============================================================
-- SECTION C  –  Plans de cotisation & paiements
-- ============================================================

-- Plan de cotisation défini par la collectivité (ex : cotisation annuelle 200 000 Ar)
CREATE TABLE "public"."cotisation_plan" (
    "id"              serial                NOT NULL,
    "id_collectivity" int                   NOT NULL,
    "label"           varchar               NOT NULL,
    "frequency"       cotisation_frequency  NOT NULL,
    "amount"          numeric(15,2)         NOT NULL,
    "year"            int,                              -- année d'applicabilité
    "is_active"       boolean               NOT NULL DEFAULT true,
    PRIMARY KEY ("id")
);

-- Encaissement : trace de chaque paiement effectué par un membre (Section C)
CREATE TABLE "public"."payment" (
    "id"                  serial          NOT NULL,
    "id_member"           int             NOT NULL,
    "id_collectivity"     int             NOT NULL,
    "id_cotisation_plan"  int,                        -- NULL = frais d'adhésion ou ponctuel libre
    "id_account"          int             NOT NULL,   -- compte qui reçoit le paiement (Section D)
    "amount"              numeric(15,2)   NOT NULL,
    "payment_date"        timestamp       NOT NULL,
    "payment_mode"        payment_mode    NOT NULL,
    "recorded_by"         int             NOT NULL,   -- id du trésorier qui saisit
    PRIMARY KEY ("id")
);

-- ============================================================
-- SECTION D  –  Comptes (caisse, bancaire, mobile money)
-- ============================================================

-- Table mère : un compte appartient soit à une collectivité, soit à la fédération
CREATE TABLE "public"."account" (
    "id"              serial          NOT NULL,
    "id_collectivity" int,
    "id_federation"   int,
    PRIMARY KEY ("id"),

    -- Exactly one owner (collectivity OR federation)
    CONSTRAINT "chk_account_owner" CHECK (
        ("id_collectivity" IS NOT NULL AND "id_federation" IS NULL) OR
        ("id_collectivity" IS NULL AND "id_federation" IS NOT NULL)
    )
);

CREATE TABLE "public"."cash_account" (
    "id"          serial NOT NULL,
    "id_account"  int    NOT NULL UNIQUE,

    PRIMARY KEY ("id"),

    CONSTRAINT "fk_cash_account"
        FOREIGN KEY ("id_account")
        REFERENCES "public"."account" ("id")
        ON DELETE CASCADE
);

CREATE TYPE movement_type AS ENUM ('IN', 'OUT');

CREATE TABLE "public"."account_movement" (
    "id"          serial          NOT NULL,
    "id_account"  int             NOT NULL,
    "type"        movement_type   NOT NULL,
    "amount"      numeric(15,2)   NOT NULL CHECK (amount > 0),
    "created_at"  timestamp       NOT NULL DEFAULT now(),

    PRIMARY KEY ("id"),

    CONSTRAINT "fk_movement_account"
        FOREIGN KEY ("id_account")
        REFERENCES "public"."account" ("id")
        ON DELETE CASCADE
);


-- Détails d'un compte bancaire (RIB décomposé)
CREATE TABLE "public"."bank_account" (
    "id"             serial           NOT NULL,
    "id_account"     int              NOT NULL UNIQUE,
    "holder_name"    varchar          NOT NULL,
    "bank_name"      bank_name_enum   NOT NULL,
    "bank_code"      char(5)          NOT NULL,
    "branch_code"    char(5)          NOT NULL,
    "account_number" char(11)         NOT NULL,
    "rib_key"        char(2)          NOT NULL,

    PRIMARY KEY ("id"),

    CONSTRAINT "fk_bank_account"
        FOREIGN KEY ("id_account")
        REFERENCES "public"."account" ("id")
        ON DELETE CASCADE
);

-- Détails d'un compte mobile money
CREATE TABLE "public"."mobile_money_account" (
    "id"           serial               NOT NULL,
    "id_account"   int                  NOT NULL UNIQUE,
    "holder_name"  varchar              NOT NULL,
    "service_name" mobile_money_service NOT NULL,
    "phone_number" varchar              NOT NULL UNIQUE,

    PRIMARY KEY ("id"),

    CONSTRAINT "fk_mobile_account"
        FOREIGN KEY ("id_account")
        REFERENCES "public"."account" ("id")
        ON DELETE CASCADE
);


CREATE UNIQUE INDEX uq_cash_account_unique
ON cash_account (
    (SELECT id_collectivity FROM account WHERE account.id = cash_account.id_account),
    (SELECT id_federation FROM account WHERE account.id = cash_account.id_account)
);

-- ============================================================
-- SECTION E  –  Activités
-- ============================================================

CREATE TABLE "public"."activity" (
    "id"               serial          NOT NULL,
    "title"            varchar         NOT NULL,
    "description"      text,
    "activity_date"    timestamp       NOT NULL,
    "activity_type"    activity_type   NOT NULL,
    -- true = obligatoire pour tous les membres concernés
    "is_mandatory_all" boolean         NOT NULL DEFAULT false,
    "id_collectivity"  int,   -- NULL si activité fédération
    "id_federation"    int,   -- NULL si activité collectivité
    PRIMARY KEY ("id"),
    CONSTRAINT "chk_activity_owner" CHECK (
        ("id_collectivity" IS NOT NULL AND "id_federation" IS NULL) OR
        ("id_collectivity" IS NULL  AND "id_federation"   IS NOT NULL)
    )
);

-- Pour les activités exceptionnelles : postes spécifiquement obligés d'assister
CREATE TABLE "public"."activity_mandatory_role" (
    "id"          serial                    NOT NULL,
    "id_activity" int                       NOT NULL,
    "post_name"   collectivity_post_name    NOT NULL,
    PRIMARY KEY ("id"),
    UNIQUE ("id_activity", "post_name")
);

-- ============================================================
-- SECTION F  –  Feuille de présence
-- ============================================================

CREATE TABLE "public"."attendance" (
    "id"                    serial    NOT NULL,
    "id_activity"           int       NOT NULL,
    "id_member"             int       NOT NULL,
    "is_present"            boolean   NOT NULL DEFAULT false,
    "is_excused"            boolean   NOT NULL DEFAULT false,
    "excuse_reason"         text,
    -- Collectivité d'appartenance du membre au moment de la présence
    -- (permet de distinguer les invités d'autres collectivités – Section F)
    "id_member_collectivity" int      NOT NULL,
    PRIMARY KEY ("id"),
    UNIQUE ("id_activity", "id_member")
);

-- ============================================================
-- CONTRAINTES DE CLÉ ÉTRANGÈRE
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

ALTER TABLE "public"."bank_account"
    ADD CONSTRAINT "fk_ba_account"                 FOREIGN KEY ("id_account")       REFERENCES "public"."account"("id");

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


CREATE UNIQUE INDEX uq_unique_active_post_per_collectivity
ON member_collectivity (id_collectivity, post_name)
WHERE end_date IS NULL
AND post_name IN ('PRESIDENT', 'DEPUTY_PRESIDENT', 'TREASURER', 'SECRETARY');

ALTER TABLE member_collectivity
ADD CONSTRAINT chk_mandate_duration
CHECK (end_date IS NULL OR end_date <= start_date + INTERVAL '1 year');