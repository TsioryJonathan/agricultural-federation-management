-- ============================================
-- NETTOYAGE COMPLET
-- ============================================
DROP TABLE IF EXISTS activity_attendance CASCADE;
DROP TABLE IF EXISTS activity_member_occupation CASCADE;
DROP TABLE IF EXISTS activity CASCADE;
DROP TABLE IF EXISTS transaction CASCADE;
DROP TABLE IF EXISTS cotisation_plan CASCADE;
DROP TABLE IF EXISTS mobile_money_account CASCADE;
DROP TABLE IF EXISTS bank_account CASCADE;
DROP TABLE IF EXISTS cash_account CASCADE;
DROP TABLE IF EXISTS account CASCADE;
DROP TABLE IF EXISTS member_referee CASCADE;
DROP TABLE IF EXISTS member_collectivity CASCADE;
DROP TABLE IF EXISTS member CASCADE;
DROP TABLE IF EXISTS collectivity CASCADE;
DROP TABLE IF EXISTS federation CASCADE;

-- ============================================
-- CRÉATION DES TABLES
-- ============================================
CREATE TABLE IF NOT EXISTS federation (
                                          id VARCHAR PRIMARY KEY DEFAULT 'fed-1',
                                          name VARCHAR NOT NULL DEFAULT 'Fédération Agricole de Madagascar',
                                          creation_date DATE DEFAULT CURRENT_DATE
);

CREATE TABLE IF NOT EXISTS member (
                                      id VARCHAR PRIMARY KEY,
                                      first_name VARCHAR NOT NULL,
                                      last_name VARCHAR NOT NULL,
                                      birth_date DATE NOT NULL,
                                      gender VARCHAR(10) CHECK (gender IN ('MALE', 'FEMALE')),
                                      address VARCHAR,
                                      profession VARCHAR,
                                      phone_number VARCHAR,
                                      email VARCHAR UNIQUE NOT NULL,
                                      enrolment_date DATE DEFAULT CURRENT_DATE,
                                      is_superuser BOOLEAN DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS member_referee (
                                              id_candidate VARCHAR REFERENCES member(id),
                                              id_referee VARCHAR REFERENCES member(id),
                                              relationship VARCHAR,
                                              PRIMARY KEY (id_candidate, id_referee)
);

CREATE TABLE IF NOT EXISTS collectivity (
                                            id VARCHAR PRIMARY KEY,
                                            number VARCHAR UNIQUE,
                                            name VARCHAR UNIQUE,
                                            speciality VARCHAR NOT NULL,
                                            creation_date DATE DEFAULT CURRENT_DATE,
                                            federation_approval BOOLEAN NOT NULL,
                                            authorization_date DATE,
                                            location VARCHAR NOT NULL,
                                            id_federation VARCHAR REFERENCES federation(id)
);

CREATE TABLE IF NOT EXISTS member_collectivity (
                                                   id_member VARCHAR REFERENCES member(id),
                                                   id_collectivity VARCHAR REFERENCES collectivity(id),
                                                   occupation VARCHAR CHECK (occupation IN ('JUNIOR', 'SENIOR', 'SECRETARY', 'TREASURER', 'VICE_PRESIDENT', 'PRESIDENT')),
                                                   start_date DATE DEFAULT CURRENT_DATE,
                                                   end_date DATE,
                                                   PRIMARY KEY (id_member, id_collectivity, start_date)
);

CREATE TABLE IF NOT EXISTS account (
                                       id VARCHAR PRIMARY KEY,
                                       id_collectivity VARCHAR REFERENCES collectivity(id),
                                       id_federation VARCHAR REFERENCES federation(id),
                                       CHECK (id_collectivity IS NOT NULL OR id_federation IS NOT NULL)
);

CREATE TABLE IF NOT EXISTS cash_account (
                                            id VARCHAR PRIMARY KEY DEFAULT gen_random_uuid()::VARCHAR,
                                            id_account VARCHAR UNIQUE REFERENCES account(id)
);

CREATE TABLE IF NOT EXISTS bank_account (
                                            id VARCHAR PRIMARY KEY DEFAULT gen_random_uuid()::VARCHAR,
                                            id_account VARCHAR UNIQUE REFERENCES account(id),
                                            holder_name VARCHAR NOT NULL,
                                            bank_name VARCHAR CHECK (bank_name IN ('BRED', 'MCB', 'BMOI', 'BOA', 'BGFI', 'AFG', 'ACCES_BANQUE', 'BAOBAB', 'SIPEM')),
                                            bank_code VARCHAR(5),
                                            branch_code VARCHAR(5),
                                            account_number VARCHAR(11),
                                            rib_key VARCHAR(2)
);

CREATE TABLE IF NOT EXISTS mobile_money_account (
                                                    id VARCHAR PRIMARY KEY DEFAULT gen_random_uuid()::VARCHAR,
                                                    id_account VARCHAR UNIQUE REFERENCES account(id),
                                                    holder_name VARCHAR NOT NULL,
                                                    service_name VARCHAR CHECK (service_name IN ('ORANGE_MONEY', 'MVOLA', 'AIRTEL_MONEY')),
                                                    phone_number VARCHAR NOT NULL
);

CREATE TABLE IF NOT EXISTS cotisation_plan (
                                               id VARCHAR PRIMARY KEY,
                                               label VARCHAR NOT NULL,
                                               id_collectivity VARCHAR REFERENCES collectivity(id),
                                               status VARCHAR DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
                                               frequency VARCHAR CHECK (frequency IN ('WEEKLY', 'MONTHLY', 'ANNUALLY', 'PUNCTUALLY')),
                                               eligible_from DATE,
                                               amount DECIMAL(15,2)
);

CREATE TABLE IF NOT EXISTS transaction (
                                           id VARCHAR PRIMARY KEY DEFAULT gen_random_uuid()::VARCHAR,
                                           id_collectivity VARCHAR REFERENCES collectivity(id),
                                           id_member VARCHAR REFERENCES member(id),
                                           id_cotisation_plan VARCHAR REFERENCES cotisation_plan(id),
                                           transaction_type VARCHAR CHECK (transaction_type IN ('IN', 'OUT')),
                                           amount DECIMAL(15,2),
                                           transaction_date DATE DEFAULT CURRENT_DATE,
                                           payment_mode VARCHAR CHECK (payment_mode IN ('CASH', 'MOBILE_BANKING', 'BANK_TRANSFER')),
                                           description VARCHAR,
                                           id_account VARCHAR REFERENCES account(id)
);

CREATE TABLE IF NOT EXISTS activity (
                                        id VARCHAR PRIMARY KEY DEFAULT gen_random_uuid()::VARCHAR,
                                        id_collectivity VARCHAR REFERENCES collectivity(id),
                                        label VARCHAR NOT NULL,
                                        activity_type VARCHAR CHECK (activity_type IN ('MEETING', 'TRAINING', 'OTHER')),
                                        executive_date DATE,
                                        week_ordinal INTEGER CHECK (week_ordinal BETWEEN 1 AND 5),
                                        day_of_week VARCHAR CHECK (day_of_week IN ('MO', 'TU', 'WE', 'TH', 'FR', 'SA', 'SU')),
                                        creation_date DATE DEFAULT CURRENT_DATE
);

CREATE TABLE IF NOT EXISTS activity_member_occupation (
                                                          id_activity VARCHAR REFERENCES activity(id),
                                                          occupation VARCHAR CHECK (occupation IN ('JUNIOR', 'SENIOR', 'SECRETARY', 'TREASURER', 'VICE_PRESIDENT', 'PRESIDENT')),
                                                          PRIMARY KEY (id_activity, occupation)
);

CREATE TABLE IF NOT EXISTS activity_attendance (
                                                   id VARCHAR PRIMARY KEY DEFAULT gen_random_uuid()::VARCHAR,
                                                   id_activity VARCHAR REFERENCES activity(id),
                                                   id_member VARCHAR REFERENCES member(id),
                                                   attendance_status VARCHAR DEFAULT 'UNDEFINED' CHECK (attendance_status IN ('UNDEFINED', 'ATTENDED', 'MISSING'))
);

-- ============================================
-- DONNÉES DE BASE
-- ============================================

-- Federation
INSERT INTO federation (id, name) VALUES ('fed-1', 'Fédération Agricole de Madagascar');

-- Collectivities (Tableau 1)
INSERT INTO collectivity (id, number, name, speciality, creation_date, federation_approval, authorization_date, location, id_federation) VALUES
                                                                                                                                             ('col-1', '1', 'Mpanorina', 'Riziculture', '2026-01-01', TRUE, '2026-01-01', 'Ambatondrazaka', 'fed-1'),
                                                                                                                                             ('col-2', '2', 'Dobo voalahany', 'Pisciculture', '2026-01-01', TRUE, '2026-01-01', 'Ambatondrazaka', 'fed-1'),
                                                                                                                                             ('col-3', '3', 'Tantely mamy', 'Apiculture', '2026-01-01', TRUE, '2026-01-01', 'Brickaville', 'fed-1');

-- Members Collectivité 1 (Tableau 2) - date d'adhésion au 01/01/2026
INSERT INTO member (id, first_name, last_name, birth_date, gender, address, profession, phone_number, email, enrolment_date) VALUES
                                                                                                                                 ('C1-M1', 'Nom membre 1', 'Prénom membre 1', '1980-02-01', 'MALE', 'Lot II V M Ambato.', 'Riziculteur', '0341234567', 'member.1@fed-agri.mg', '2026-01-01'),
                                                                                                                                 ('C1-M2', 'Nom membre 2', 'Prénom membre 2', '1982-03-05', 'MALE', 'Lot II F Ambato.', 'Agriculteur', '0321234567', 'member.2@fed-agri.mg', '2026-01-01'),
                                                                                                                                 ('C1-M3', 'Nom membre 3', 'Prénom membre 3', '1992-03-10', 'MALE', 'Lot II J Ambato.', 'Collecteur', '0331234567', 'member.3@fed-agrimg', '2026-01-01'),
                                                                                                                                 ('C1-M4', 'Nom membre 4', 'Prénom membre 4', '1988-05-22', 'FEMALE', 'Lot A K 50 Ambato.', 'Distributeur', '0381234567', 'member.4@fed-agri.mg', '2026-01-01'),
                                                                                                                                 ('C1-M5', 'Nom membre 5', 'Prénom membre 5', '1999-08-21', 'MALE', 'Lot UV 80 Ambato.', 'Riziculteur', '0373434567', 'member.5@fed-agri.mg', '2026-01-01'),
                                                                                                                                 ('C1-M6', 'Nom membre 6', 'Prénom membre 6', '1998-08-22', 'FEMALE', 'Lot UV 6 Ambato.', 'Riziculteur', '0372234567', 'member.6@fed-agri.mg', '2026-01-01'),
                                                                                                                                 ('C1-M7', 'Nom membre 7', 'Prénom membre 7', '1998-01-31', 'MALE', 'Lot UV 7 Ambato.', 'Riziculteur', '0374234567', 'member.7@fed-agri.mg', '2026-01-01'),
                                                                                                                                 ('C1-M8', 'Nom membre 8', 'Prénom membre 6', '1975-08-20', 'MALE', 'Lot UV 8 Ambato.', 'Riziculteur', '0370234567', 'member.8@fed-agri.mg', '2026-01-01');

-- Members Collectivité 2 (Tableau 3) - date d'adhésion au 01/01/2026
INSERT INTO member (id, first_name, last_name, birth_date, gender, address, profession, phone_number, email, enrolment_date) VALUES
                                                                                                                                 ('C2-M1', 'Nom membre 1', 'Prénom membre 1', '1980-02-01', 'MALE', 'Lot II V M Ambato.', 'Riziculteur', '0341234567', 'c2.member.1@fed-agri.mg', '2026-01-01'),
                                                                                                                                 ('C2-M2', 'Nom membre 2', 'Prénom membre 2', '1982-03-05', 'MALE', 'Lot II F Ambato.', 'Agriculteur', '0321234567', 'c2.member.2@fed-agri.mg', '2026-01-01'),
                                                                                                                                 ('C2-M3', 'Nom membre 3', 'Prénom membre 3', '1992-03-10', 'MALE', 'Lot II J Ambato.', 'Collecteur', '0331234567', 'c2.member.3@fed-agri.mg', '2026-01-01'),
                                                                                                                                 ('C2-M4', 'Nom membre 4', 'Prénom membre 4', '1988-05-22', 'FEMALE', 'Lot A K 50 Ambato.', 'Distributeur', '0381234567', 'c2.member.4@fed-agri.mg', '2026-01-01'),
                                                                                                                                 ('C2-M5', 'Nom membre 5', 'Prénom membre 5', '1999-08-21', 'MALE', 'Lot UV 80 Ambato.', 'Riziculteur', '0373434567', 'c2.member.5@fed-agri.mg', '2026-01-01'),
                                                                                                                                 ('C2-M6', 'Nom membre 6', 'Prénom membre 6', '1998-08-22', 'FEMALE', 'Lot UV 6 Ambato.', 'Riziculteur', '0372234567', 'c2.member.6@fed-agri.mg', '2026-01-01'),
                                                                                                                                 ('C2-M7', 'Nom membre 7', 'Prénom membre 7', '1998-01-31', 'MALE', 'Lot UV 7 Ambato.', 'Riziculteur', '0374234567', 'c2.member.7@fed-agri.mg', '2026-01-01'),
                                                                                                                                 ('C2-M8', 'Nom membre 8', 'Prénom membre 6', '1975-08-20', 'MALE', 'Lot UV 8 Ambato.', 'Riziculteur', '0370234567', 'c2.member.8@fed-agri.mg', '2026-01-01');

-- Members Collectivité 3 (Tableau 4) - date d'adhésion au 01/01/2026
INSERT INTO member (id, first_name, last_name, birth_date, gender, address, profession, phone_number, email, enrolment_date) VALUES
                                                                                                                                 ('C3-M1', 'Nom membre 9', 'Prénom membre 9', '1988-01-02', 'MALE', 'Lot 33 J Antsirabe', 'Apiculteur', '034034567', 'member.9@fed-agri.mg', '2026-01-01'),
                                                                                                                                 ('C3-M2', 'Nom membre 10', 'Prénom membre 10', '1982-03-05', 'MALE', 'Lot 2 J Antsirabe', 'Agriculteur', '0338634567', 'member.10@fed-agri.mg', '2026-01-01'),
                                                                                                                                 ('C3-M3', 'Nom membre 11', 'Prénom membre 11', '1992-03-12', 'MALE', 'Lot 8 KM Antsirabe', 'Collecteur', '0338234567', 'member.11@fed-agrimg', '2026-01-01'),
                                                                                                                                 ('C3-M4', 'Nom membre 12', 'Prénom membre 12', '1988-05-10', 'FEMALE', 'Lot A K 50 Antsirabe', 'Distributeur', '0382334567', 'member.12@fed-agri.mg', '2026-01-01'),
                                                                                                                                 ('C3-M5', 'Nom membre 13', 'Prénom membre 13', '1999-08-11', 'MALE', 'Lot UV 80 Antsirabe.', 'Apiculteur', '0373365567', 'member.13@fed-agri.mg', '2026-01-01'),
                                                                                                                                 ('C3-M6', 'Nom membre 14', 'Prénom membre 14', '1998-08-09', 'FEMALE', 'Lot UV 6 Antsirabe.', 'Apiculteur', '0378234567', 'member.14@fed-agri.mg', '2026-01-01'),
                                                                                                                                 ('C3-M7', 'Nom membre 15', 'Prénom membre 15', '1998-01-13', 'MALE', 'Lot UV 7 Antsirabe', 'Apiculteur', '0374914567', 'member.15@fed-agri.mg', '2026-01-01'),
                                                                                                                                 ('C3-M8', 'Nom membre 16', 'Prénom membre 16', '1975-08-02', 'MALE', 'Lot UV 8 Antsirabe', 'Apiculteur', '0370634567', 'member.16@fed-agri.mg', '2026-01-01');

-- Member-Collectivity associations
INSERT INTO member_collectivity (id_member, id_collectivity, occupation, start_date) VALUES
                                                                                         ('C1-M1', 'col-1', 'PRESIDENT', '2026-01-01'),
                                                                                         ('C1-M2', 'col-1', 'VICE_PRESIDENT', '2026-01-01'),
                                                                                         ('C1-M3', 'col-1', 'SECRETARY', '2026-01-01'),
                                                                                         ('C1-M4', 'col-1', 'TREASURER', '2026-01-01'),
                                                                                         ('C1-M5', 'col-1', 'SENIOR', '2026-01-01'),
                                                                                         ('C1-M6', 'col-1', 'SENIOR', '2026-01-01'),
                                                                                         ('C1-M7', 'col-1', 'SENIOR', '2026-01-01'),
                                                                                         ('C1-M8', 'col-1', 'SENIOR', '2026-01-01'),
                                                                                         ('C2-M1', 'col-2', 'SENIOR', '2026-01-01'),
                                                                                         ('C2-M2', 'col-2', 'SENIOR', '2026-01-01'),
                                                                                         ('C2-M3', 'col-2', 'SENIOR', '2026-01-01'),
                                                                                         ('C2-M4', 'col-2', 'SENIOR', '2026-01-01'),
                                                                                         ('C2-M5', 'col-2', 'PRESIDENT', '2026-01-01'),
                                                                                         ('C2-M6', 'col-2', 'VICE_PRESIDENT', '2026-01-01'),
                                                                                         ('C2-M7', 'col-2', 'SECRETARY', '2026-01-01'),
                                                                                         ('C2-M8', 'col-2', 'TREASURER', '2026-01-01'),
                                                                                         ('C3-M1', 'col-3', 'PRESIDENT', '2026-01-01'),
                                                                                         ('C3-M2', 'col-3', 'VICE_PRESIDENT', '2026-01-01'),
                                                                                         ('C3-M3', 'col-3', 'SECRETARY', '2026-01-01'),
                                                                                         ('C3-M4', 'col-3', 'TREASURER', '2026-01-01'),
                                                                                         ('C3-M5', 'col-3', 'SENIOR', '2026-01-01'),
                                                                                         ('C3-M6', 'col-3', 'SENIOR', '2026-01-01'),
                                                                                         ('C3-M7', 'col-3', 'SENIOR', '2026-01-01'),
                                                                                         ('C3-M8', 'col-3', 'SENIOR', '2026-01-01');

-- Referees
INSERT INTO member_referee (id_candidate, id_referee, relationship) VALUES
                                                                        ('C1-M3', 'C1-M1', 'AMI'), ('C1-M3', 'C1-M2', 'AMI'),
                                                                        ('C1-M4', 'C1-M1', 'AMI'), ('C1-M4', 'C1-M2', 'AMI'),
                                                                        ('C1-M5', 'C1-M1', 'AMI'), ('C1-M5', 'C1-M2', 'AMI'),
                                                                        ('C1-M6', 'C1-M1', 'AMI'), ('C1-M6', 'C1-M2', 'AMI'),
                                                                        ('C1-M7', 'C1-M1', 'AMI'), ('C1-M7', 'C1-M2', 'AMI'),
                                                                        ('C1-M8', 'C1-M6', 'AMI'), ('C1-M8', 'C1-M7', 'AMI'),
                                                                        ('C2-M3', 'C1-M1', 'AMI'), ('C2-M3', 'C1-M2', 'AMI'),
                                                                        ('C2-M4', 'C1-M1', 'AMI'), ('C2-M4', 'C1-M2', 'AMI'),
                                                                        ('C2-M5', 'C1-M1', 'AMI'), ('C2-M5', 'C1-M2', 'AMI'),
                                                                        ('C2-M6', 'C1-M1', 'AMI'), ('C2-M6', 'C1-M2', 'AMI'),
                                                                        ('C2-M7', 'C1-M1', 'AMI'), ('C2-M7', 'C1-M2', 'AMI'),
                                                                        ('C2-M8', 'C1-M6', 'AMI'), ('C2-M8', 'C1-M7', 'AMI'),
                                                                        ('C3-M1', 'C1-M1', 'AMI'), ('C3-M1', 'C1-M2', 'AMI'),
                                                                        ('C3-M2', 'C1-M1', 'AMI'), ('C3-M2', 'C1-M2', 'AMI'),
                                                                        ('C3-M3', 'C3-M1', 'AMI'), ('C3-M3', 'C3-M2', 'AMI'),
                                                                        ('C3-M4', 'C3-M1', 'AMI'), ('C3-M4', 'C3-M2', 'AMI'),
                                                                        ('C3-M5', 'C3-M1', 'AMI'), ('C3-M5', 'C3-M2', 'AMI'),
                                                                        ('C3-M6', 'C3-M1', 'AMI'), ('C3-M6', 'C3-M2', 'AMI'),
                                                                        ('C3-M7', 'C3-M1', 'AMI'), ('C3-M7', 'C3-M2', 'AMI'),
                                                                        ('C3-M8', 'C3-M1', 'AMI'), ('C3-M8', 'C3-M2', 'AMI');

-- ============================================
-- COMPTES FINANCIERS
-- ============================================

-- Comptes de base
INSERT INTO account (id, id_collectivity) VALUES
                                              ('C1-A-CASH', 'col-1'),
                                              ('C1-A-MOBILE-1', 'col-1'),
                                              ('C2-A-CASH', 'col-2'),
                                              ('C2-A-MOBILE-1', 'col-2'),
                                              ('C3-A-CASH', 'col-3');

INSERT INTO cash_account (id_account) VALUES
                                          ('C1-A-CASH'), ('C2-A-CASH'), ('C3-A-CASH');

INSERT INTO mobile_money_account (id_account, holder_name, service_name, phone_number) VALUES
                                                                                           ('C1-A-MOBILE-1', 'Mpanorina', 'ORANGE_MONEY', '0370489612'),
                                                                                           ('C2-A-MOBILE-1', 'Dobo voalohany', 'ORANGE_MONEY', '0320489612');

-- Nouveaux comptes col-3
INSERT INTO account (id, id_collectivity) VALUES
                                              ('C3-A-BANK-1', 'col-3'),
                                              ('C3-A-BANK-2', 'col-3'),
                                              ('C3-A-MOBILE-1', 'col-3');

INSERT INTO bank_account (id_account, holder_name, bank_name, bank_code, branch_code, account_number, rib_key) VALUES
                                                                                                                   ('C3-A-BANK-1', 'Koto', 'BMOI', '00004', '00001', '1234567890', '12'),
                                                                                                                   ('C3-A-BANK-2', 'Naivo', 'BRED', '00008', '00003', '4567890123', '58');

INSERT INTO mobile_money_account (id_account, holder_name, service_name, phone_number) VALUES
    ('C3-A-MOBILE-1', 'Koloa', 'MVOLA', '0341889612');

-- ============================================
-- COTISATIONS
-- ============================================

INSERT INTO cotisation_plan (id, label, id_collectivity, status, frequency, eligible_from, amount) VALUES
                                                                                                       ('cot-1', 'Cotisation annuelle', 'col-1', 'ACTIVE', 'ANNUALLY', '2026-01-01', 200000),
                                                                                                       ('cot-2', 'Famangiana', 'col-1', 'ACTIVE', 'PUNCTUALLY', '2026-04-30', 20000),
                                                                                                       ('cot-3', 'Cotisation annuelle', 'col-2', 'ACTIVE', 'ANNUALLY', '2026-01-01', 200000),
                                                                                                       ('cot-4', 'Cotisation 2025', 'col-2', 'INACTIVE', 'ANNUALLY', '2025-01-01', 100000),
                                                                                                       ('cot-5', 'Cotisation mensuelle', 'col-3', 'ACTIVE', 'MONTHLY', '2026-04-01', 25000);

-- ============================================
-- PAIEMENTS / TRANSACTIONS
-- ============================================

-- Col-1
INSERT INTO transaction (id, id_collectivity, id_member, id_cotisation_plan, transaction_type, amount, transaction_date, payment_mode, id_account) VALUES
                                                                                                                                                       ('trx-1-1', 'col-1', 'C1-M1', 'cot-1', 'IN', 200000, '2026-01-01', 'CASH', 'C1-A-CASH'),
                                                                                                                                                       ('trx-1-2', 'col-1', 'C1-M2', 'cot-1', 'IN', 200000, '2026-01-01', 'CASH', 'C1-A-CASH'),
                                                                                                                                                       ('trx-1-3', 'col-1', 'C1-M3', 'cot-1', 'IN', 200000, '2026-01-01', 'MOBILE_BANKING', 'C1-A-MOBILE-1'),
                                                                                                                                                       ('trx-1-4', 'col-1', 'C1-M4', 'cot-1', 'IN', 200000, '2026-01-01', 'MOBILE_BANKING', 'C1-A-MOBILE-1'),
                                                                                                                                                       ('trx-1-5', 'col-1', 'C1-M5', 'cot-1', 'IN', 150000, '2026-01-01', 'MOBILE_BANKING', 'C1-A-MOBILE-1'),
                                                                                                                                                       ('trx-1-6', 'col-1', 'C1-M6', 'cot-1', 'IN', 100000, '2026-05-01', 'CASH', 'C1-A-CASH'),
                                                                                                                                                       ('trx-1-7', 'col-1', 'C1-M7', 'cot-1', 'IN', 60000, '2026-05-01', 'CASH', 'C1-A-CASH'),
                                                                                                                                                       ('trx-1-8', 'col-1', 'C1-M8', 'cot-1', 'IN', 90000, '2026-05-01', 'CASH', 'C1-A-CASH');

-- Col-2
INSERT INTO transaction (id, id_collectivity, id_member, id_cotisation_plan, transaction_type, amount, transaction_date, payment_mode, id_account) VALUES
                                                                                                                                                       ('trx-2-1', 'col-2', 'C2-M1', 'cot-3', 'IN', 120000, '2026-01-01', 'CASH', 'C2-A-CASH'),
                                                                                                                                                       ('trx-2-2', 'col-2', 'C2-M2', 'cot-3', 'IN', 180000, '2026-01-01', 'CASH', 'C2-A-CASH'),
                                                                                                                                                       ('trx-2-3', 'col-2', 'C2-M3', 'cot-3', 'IN', 200000, '2026-01-01', 'CASH', 'C2-A-CASH'),
                                                                                                                                                       ('trx-2-4', 'col-2', 'C2-M4', 'cot-3', 'IN', 200000, '2026-01-01', 'CASH', 'C2-A-CASH'),
                                                                                                                                                       ('trx-2-5', 'col-2', 'C2-M5', 'cot-3', 'IN', 200000, '2026-01-01', 'CASH', 'C2-A-CASH'),
                                                                                                                                                       ('trx-2-6', 'col-2', 'C2-M6', 'cot-3', 'IN', 200000, '2026-01-01', 'CASH', 'C2-A-CASH'),
                                                                                                                                                       ('trx-2-7', 'col-2', 'C2-M7', 'cot-3', 'IN', 80000, '2026-01-01', 'MOBILE_BANKING', 'C2-A-MOBILE-1'),
                                                                                                                                                       ('trx-2-8', 'col-2', 'C2-M8', 'cot-3', 'IN', 120000, '2026-01-01', 'MOBILE_BANKING', 'C2-A-MOBILE-1');

-- Col-3 Avril
INSERT INTO transaction (id, id_collectivity, id_member, id_cotisation_plan, transaction_type, amount, transaction_date, payment_mode, id_account) VALUES
                                                                                                                                                       ('trx-3-1', 'col-3', 'C3-M1', 'cot-5', 'IN', 25000, '2026-04-01', 'BANK_TRANSFER', 'C3-A-BANK-1'),
                                                                                                                                                       ('trx-3-2', 'col-3', 'C3-M2', 'cot-5', 'IN', 25000, '2026-04-01', 'BANK_TRANSFER', 'C3-A-BANK-1'),
                                                                                                                                                       ('trx-3-3', 'col-3', 'C3-M3', 'cot-5', 'IN', 25000, '2026-04-01', 'BANK_TRANSFER', 'C3-A-BANK-1'),
                                                                                                                                                       ('trx-3-4', 'col-3', 'C3-M4', 'cot-5', 'IN', 25000, '2026-04-01', 'BANK_TRANSFER', 'C3-A-BANK-1'),
                                                                                                                                                       ('trx-3-5', 'col-3', 'C3-M5', 'cot-5', 'IN', 25000, '2026-04-01', 'BANK_TRANSFER', 'C3-A-BANK-2'),
                                                                                                                                                       ('trx-3-6', 'col-3', 'C3-M6', 'cot-5', 'IN', 25000, '2026-04-01', 'BANK_TRANSFER', 'C3-A-BANK-2'),
                                                                                                                                                       ('trx-3-7', 'col-3', 'C3-M7', 'cot-5', 'IN', 25000, '2026-04-01', 'CASH', 'C3-A-CASH'),
                                                                                                                                                       ('trx-3-8', 'col-3', 'C3-M8', 'cot-5', 'IN', 25000, '2026-04-01', 'CASH', 'C3-A-CASH');

-- Col-3 Mai
INSERT INTO transaction (id, id_collectivity, id_member, id_cotisation_plan, transaction_type, amount, transaction_date, payment_mode, id_account) VALUES
                                                                                                                                                       ('trx-3-9', 'col-3', 'C3-M1', 'cot-5', 'IN', 25000, '2026-05-01', 'BANK_TRANSFER', 'C3-A-BANK-1'),
                                                                                                                                                       ('trx-3-10', 'col-3', 'C3-M2', 'cot-5', 'IN', 25000, '2026-05-01', 'BANK_TRANSFER', 'C3-A-BANK-1'),
                                                                                                                                                       ('trx-3-11', 'col-3', 'C3-M3', 'cot-5', 'IN', 15000, '2026-05-01', 'BANK_TRANSFER', 'C3-A-MOBILE-1'),
                                                                                                                                                       ('trx-3-12', 'col-3', 'C3-M4', 'cot-5', 'IN', 15000, '2026-05-01', 'BANK_TRANSFER', 'C3-A-MOBILE-1'),
                                                                                                                                                       ('trx-3-13', 'col-3', 'C3-M5', 'cot-5', 'IN', 20000, '2026-05-01', 'BANK_TRANSFER', 'C3-A-BANK-2'),
                                                                                                                                                       ('trx-3-14', 'col-3', 'C3-M6', 'cot-5', 'IN', 25000, '2026-05-01', 'BANK_TRANSFER', 'C3-A-BANK-2'),
                                                                                                                                                       ('trx-3-15', 'col-3', 'C3-M7', 'cot-5', 'IN', 5000, '2026-05-01', 'CASH', 'C3-A-CASH'),
                                                                                                                                                       ('trx-3-16', 'col-3', 'C3-M8', 'cot-5', 'IN', 5000, '2026-05-01', 'CASH', 'C3-A-CASH');

-- ============================================
-- NOUVEAUX MEMBRES
-- ============================================

-- Col-1
INSERT INTO member (id, first_name, last_name, birth_date, gender, address, profession, phone_number, email, enrolment_date) VALUES
                                                                                                                                 ('C1-M9', 'Nouveau', 'Membre9', '1990-01-15', 'MALE', 'Lot ABC Ambato.', 'Agriculteur', '0341111111', 'c1.member.9@fed-agri.mg', '2026-04-01'),
                                                                                                                                 ('C1-M10', 'Nouveau', 'Membre10', '1995-06-20', 'FEMALE', 'Lot DEF Ambato.', 'Eleveur', '0342222222', 'c1.member.10@fed-agri.mg', '2026-04-01'),
                                                                                                                                 ('C1-M11', 'Nouveau', 'Membre11', '1985-03-10', 'MALE', 'Lot GHI Ambato.', 'Pêcheur', '0343333333', 'c1.member.11@fed-agri.mg', '2026-05-01'),
                                                                                                                                 ('C1-M12', 'Nouveau', 'Membre12', '1992-11-25', 'FEMALE', 'Lot JKL Ambato.', 'Artisan', '0344444444', 'c1.member.12@fed-agri.mg', '2026-06-01');

INSERT INTO member_collectivity (id_member, id_collectivity, occupation, start_date) VALUES
                                                                                         ('C1-M9', 'col-1', 'JUNIOR', '2026-04-01'),
                                                                                         ('C1-M10', 'col-1', 'JUNIOR', '2026-04-01'),
                                                                                         ('C1-M11', 'col-1', 'JUNIOR', '2026-05-01'),
                                                                                         ('C1-M12', 'col-1', 'JUNIOR', '2026-06-01');

INSERT INTO member_referee (id_candidate, id_referee, relationship) VALUES
                                                                        ('C1-M9', 'C1-M1', 'AMI'), ('C1-M9', 'C1-M2', 'AMI'),
                                                                        ('C1-M10', 'C1-M1', 'AMI'), ('C1-M10', 'C1-M2', 'AMI'),
                                                                        ('C1-M11', 'C1-M1', 'AMI'), ('C1-M11', 'C1-M2', 'AMI'),
                                                                        ('C1-M12', 'C1-M1', 'AMI'), ('C1-M12', 'C1-M2', 'AMI');

-- Col-2
INSERT INTO member (id, first_name, last_name, birth_date, gender, address, profession, phone_number, email, enrolment_date) VALUES
                                                                                                                                 ('C2-M9', 'Nouveau', 'Membre9', '1991-02-10', 'MALE', 'Lot MNO Brickaville', 'Apiculteur', '0321111111', 'c2.member.9@fed-agri.mg', '2026-03-01'),
                                                                                                                                 ('C2-M10', 'Nouveau', 'Membre10', '1993-07-05', 'FEMALE', 'Lot PQR Brickaville', 'Pisciculteur', '0322222222', 'c2.member.10@fed-agri.mg', '2026-03-01'),
                                                                                                                                 ('C2-M11', 'Nouveau', 'Membre11', '1989-09-30', 'MALE', 'Lot STU Brickaville', 'Agriculteur', '0323333333', 'c2.member.11@fed-agri.mg', '2026-03-01');

INSERT INTO member_collectivity (id_member, id_collectivity, occupation, start_date) VALUES
                                                                                         ('C2-M9', 'col-2', 'JUNIOR', '2026-03-01'),
                                                                                         ('C2-M10', 'col-2', 'JUNIOR', '2026-03-01'),
                                                                                         ('C2-M11', 'col-2', 'JUNIOR', '2026-03-01');

INSERT INTO member_referee (id_candidate, id_referee, relationship) VALUES
                                                                        ('C2-M9', 'C1-M1', 'AMI'), ('C2-M9', 'C1-M2', 'AMI'),
                                                                        ('C2-M10', 'C1-M1', 'AMI'), ('C2-M10', 'C1-M2', 'AMI'),
                                                                        ('C2-M11', 'C1-M1', 'AMI'), ('C2-M11', 'C1-M2', 'AMI');

-- Col-3
INSERT INTO member (id, first_name, last_name, birth_date, gender, address, profession, phone_number, email, enrolment_date) VALUES
                                                                                                                                 ('C3-M9', 'Nouveau', 'Membre9', '1994-04-15', 'MALE', 'Lot VWX Antsirabe', 'Apiculteur', '0331111111', 'c3.member.9@fed-agri.mg', '2026-01-01'),
                                                                                                                                 ('C3-M10', 'Nouveau', 'Membre10', '1996-08-20', 'FEMALE', 'Lot YZA Antsirabe', 'Apicultrice', '0332222222', 'c3.member.10@fed-agri.mg', '2026-02-01'),
                                                                                                                                 ('C3-M11', 'Nouveau', 'Membre11', '1987-12-05', 'MALE', 'Lot BCD Antsirabe', 'Apiculteur', '0333333333', 'c3.member.11@fed-agri.mg', '2026-02-01'),
                                                                                                                                 ('C3-M12', 'Nouveau', 'Membre12', '1998-03-10', 'FEMALE', 'Lot EFG Antsirabe', 'Apicultrice', '0334444444', 'c3.member.12@fed-agri.mg', '2026-03-01'),
                                                                                                                                 ('C3-M13', 'Nouveau', 'Membre13', '1990-06-25', 'MALE', 'Lot HIJ Antsirabe', 'Apiculteur', '0335555555', 'c3.member.13@fed-agri.mg', '2026-03-01'),
                                                                                                                                 ('C3-M14', 'Nouveau', 'Membre14', '1997-11-30', 'FEMALE', 'Lot KLM Antsirabe', 'Apicultrice', '0336666666', 'c3.member.14@fed-agri.mg', '2026-03-01');

INSERT INTO member_collectivity (id_member, id_collectivity, occupation, start_date) VALUES
                                                                                         ('C3-M9', 'col-3', 'JUNIOR', '2026-01-01'),
                                                                                         ('C3-M10', 'col-3', 'JUNIOR', '2026-02-01'),
                                                                                         ('C3-M11', 'col-3', 'JUNIOR', '2026-02-01'),
                                                                                         ('C3-M12', 'col-3', 'JUNIOR', '2026-03-01'),
                                                                                         ('C3-M13', 'col-3', 'JUNIOR', '2026-03-01'),
                                                                                         ('C3-M14', 'col-3', 'JUNIOR', '2026-03-01');

INSERT INTO member_referee (id_candidate, id_referee, relationship) VALUES
                                                                        ('C3-M9', 'C3-M1', 'AMI'), ('C3-M9', 'C3-M2', 'AMI'),
                                                                        ('C3-M10', 'C3-M1', 'AMI'), ('C3-M10', 'C3-M2', 'AMI'),
                                                                        ('C3-M11', 'C3-M1', 'AMI'), ('C3-M11', 'C3-M2', 'AMI'),
                                                                        ('C3-M12', 'C3-M1', 'AMI'), ('C3-M12', 'C3-M2', 'AMI'),
                                                                        ('C3-M13', 'C3-M1', 'AMI'), ('C3-M13', 'C3-M2', 'AMI'),
                                                                        ('C3-M14', 'C3-M1', 'AMI'), ('C3-M14', 'C3-M2', 'AMI');


