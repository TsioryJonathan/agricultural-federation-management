CREATE SCHEMA IF NOT EXISTS "public";

CREATE TYPE "collectivity_post_name" AS ENUM ('PRESIDENT', 'DEPUTY_PRESIDENT', 'TREASURER', 'SECRETARY', 'JUNIOR', 'CONFIRMED');
CREATE TYPE "federation_post_name" AS ENUM ('PRESIDENT', 'DEPUTY_PRESIDENT', 'TREASURER', 'SECRETARY');
CREATE TYPE "gender" AS ENUM ('MALE', 'FEMALE');

CREATE TABLE "public"."member" (
    "id" serial NOT NULL,
    "first_name" varchar NOT NULL,
    "last_name" varchar NOT NULL,
    "birth_date" date NOT NULL,
    "enrolment_date" timestamp NOT NULL,
    "address" text NOT NULL,
    "email" varchar NOT NULL,
    "phone" varchar NOT NULL,
    "job" varchar NOT NULL,
    "gender" gender NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."collectivity" (
    "id" serial NOT NULL,
    "number" varchar NOT NULL UNIQUE,
    "name" varchar NOT NULL UNIQUE,
    "speciality" varchar NOT NULL,
    "creation_datetime" timestamp NOT NULL,
    "id_federation" int NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."federation" (
    "id" serial NOT NULL,
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."mandate_federation" (
    "id" serial NOT NULL,
    "id_member" int NOT NULL,
    "id_federation" int NOT NULL,
    "post_name" federation_post_name NOT NULL,
    "start_date" timestamp NOT NULL,
    "end_date" timestamp,
    PRIMARY KEY ("id")
);

CREATE TABLE "public"."member_collectivity" (
    "id" serial NOT NULL,
    "id_member" int NOT NULL,
    "id_collectivity" int NOT NULL,
    "post_name" collectivity_post_name NOT NULL,
    "start_date" timestamp NOT NULL,
    "end_date" timestamp,
    PRIMARY KEY ("id")
);

-- Foreign key constraints
-- Schema: public
ALTER TABLE "public"."member_collectivity" ADD CONSTRAINT "fk_member_collectivity_id_member_member_id" FOREIGN KEY("id_member") REFERENCES "public"."member"("id");
ALTER TABLE "public"."member_collectivity" ADD CONSTRAINT "fk_member_collectivity_id_collectivity_collectivity_id" FOREIGN KEY("id_collectivity") REFERENCES "public"."collectivity"("id");
ALTER TABLE "public"."mandate_federation" ADD CONSTRAINT "fk_mandate_federation_id_member_member_id" FOREIGN KEY("id_member") REFERENCES "public"."member"("id");
ALTER TABLE "public"."mandate_federation" ADD CONSTRAINT "fk_mandate_federation_id_federation_federation_id" FOREIGN KEY("id_federation") REFERENCES "public"."federation"("id");
ALTER TABLE "public"."collectivity" ADD CONSTRAINT "fk_collectivity_id_federation_federation_id" FOREIGN KEY("id_federation") REFERENCES "public"."federation"("id");