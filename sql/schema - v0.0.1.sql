-- enums

CREATE TYPE "collectivity_occupation"  AS ENUM ('PRESIDENT', 'VICE_PRESIDENT', 'TREASURER', 'SECRETARY', 'SENIOR', 'JUNIOR');
CREATE TYPE "federation_occupation"  AS ENUM ('PRESIDENT', 'VICE_PRESIDENT', 'TREASURER', 'SECRETARY');
CREATE TYPE "gender"                  AS ENUM ('MALE', 'FEMALE');
CREATE TYPE "collectivity_status"     AS ENUM ('PENDING', 'APPROVED', 'REJECTED');
CREATE TYPE "cotisation_frequency"    AS ENUM ('MONTHLY', 'ANNUAL', 'PUNCTUAL');
CREATE TYPE "payment_mode"            AS ENUM ('CASH', 'BANK_TRANSFER', 'MOBILE_MONEY');


-- tables

CREATE TABLE "public"."member" (
                                   "id"              serial      NOT NULL,
                                   "first_name"      varchar     NOT NULL,
                                   "last_name"       varchar     NOT NULL,
                                   "birth_date"      date        NOT NULL,
                                   "enrolment_date"  timestamp   NOT NULL,
                                   "address"         text        NOT NULL,
                                   "email"           varchar     NOT NULL UNIQUE,
                                   "phone_number"    varchar     NOT NULL UNIQUE,
                                   "profession"      varchar     NOT NULL,
                                   "gender"          gender      NOT NULL,
                                   PRIMARY KEY ("id")
);


CREATE TABLE "public"."location" (
                                     "id"   serial      NOT NULL,
                                     "name" varchar     NOT NULL,
                                     PRIMARY KEY ("id")
);

CREATE TABLE "public"."federation" (
                                       "id"                    serial         NOT NULL,
                                       "cotisation_percentage" numeric(5,2)   NOT NULL DEFAULT 10.00,
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
                                         "id_location"        int                   NOT NULL,
                                         PRIMARY KEY ("id")
);

CREATE TABLE "public"."member_collectivity" (
                                                "id"              serial                    NOT NULL,
                                                "id_member"       int                       NOT NULL,
                                                "id_collectivity" int                       NOT NULL,
                                                "occupation"      collectivity_occupation  NOT NULL,
                                                "start_date"      timestamp                 NOT NULL,
                                                "end_date"        timestamp,
                                                PRIMARY KEY ("id")
);

CREATE TABLE "public"."member_referee" (
                                        "id"              serial      NOT NULL,
                                        "id_candidate"    int         NOT NULL,
                                        "id_referee"      int         NOT NULL,
                                        "id_collectivity" int         NOT NULL,
                                        "relationship"    varchar     NOT NULL,
                                        "created_at"      timestamp   NOT NULL DEFAULT NOW(),
                                        PRIMARY KEY ("id"),
                                        UNIQUE ("id_candidate", "id_referee")
);

CREATE TABLE "public"."mandate_federation" (
                                               "id"            serial                  NOT NULL,
                                               "id_member"     int                     NOT NULL,
                                               "id_federation" int                     NOT NULL,
                                               "occupation"    federation_occupation   NOT NULL,
                                               "start_date"    timestamp               NOT NULL,
                                               "end_date"      timestamp,
                                               PRIMARY KEY ("id")
);

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

CREATE TABLE "public"."payment" (
                                    "id"                  serial          NOT NULL,
                                    "id_member"           int             NOT NULL,
                                    "id_collectivity"     int             NOT NULL,
                                    "id_cotisation_plan"  int,            -- si c'est null donc c'est le frais d'adhésion ou un coticota ponctuel
                                    "id_account"          int             NOT NULL,
                                    "amount"              numeric(15,2)   NOT NULL,
                                    "payment_date"        timestamp       NOT NULL,
                                    "payment_mode"        payment_mode    NOT NULL,
                                    PRIMARY KEY ("id")
);