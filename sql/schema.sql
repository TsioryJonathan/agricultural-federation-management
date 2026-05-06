-- ============================================
-- COMPLETE DATABASE SCHEMA AND DATA FROM PDF ONLY (FIXED)
-- ============================================

-- Federation table
CREATE TABLE IF NOT EXISTS federation (
                                          id VARCHAR PRIMARY KEY DEFAULT 'fed-1',
                                          name VARCHAR NOT NULL DEFAULT 'Fédération Agricole de Madagascar',
                                          creation_date DATE DEFAULT CURRENT_DATE
);

-- Member table
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

-- Member referee relationship
CREATE TABLE IF NOT EXISTS member_referee (
                                              id_candidate VARCHAR REFERENCES member(id),
                                              id_referee VARCHAR REFERENCES member(id),
                                              relationship VARCHAR,
                                              PRIMARY KEY (id_candidate, id_referee)
);

-- Collectivity table
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

-- Member collectivity association
CREATE TABLE IF NOT EXISTS member_collectivity (
                                                   id_member VARCHAR REFERENCES member(id),
                                                   id_collectivity VARCHAR REFERENCES collectivity(id),
                                                   occupation VARCHAR CHECK (
                                                       occupation IN ('JUNIOR', 'SENIOR', 'SECRETARY', 'TREASURER', 'VICE_PRESIDENT', 'PRESIDENT')
                                                       ),
                                                   start_date DATE DEFAULT CURRENT_DATE,
                                                   end_date DATE,
                                                   PRIMARY KEY (id_member, id_collectivity, start_date)
);

-- Account table
CREATE TABLE IF NOT EXISTS account (
                                       id VARCHAR PRIMARY KEY,
                                       id_collectivity VARCHAR REFERENCES collectivity(id),
                                       id_federation VARCHAR REFERENCES federation(id),
                                       CHECK (id_collectivity IS NOT NULL OR id_federation IS NOT NULL)
);

-- Cash account
CREATE TABLE IF NOT EXISTS cash_account (
                                            id VARCHAR PRIMARY KEY DEFAULT gen_random_uuid()::VARCHAR,
                                            id_account VARCHAR UNIQUE REFERENCES account(id)
);

-- Bank account
CREATE TABLE IF NOT EXISTS bank_account (
                                            id VARCHAR PRIMARY KEY DEFAULT gen_random_uuid()::VARCHAR,
                                            id_account VARCHAR UNIQUE REFERENCES account(id),
                                            holder_name VARCHAR NOT NULL,
                                            bank_name VARCHAR CHECK (
                                                bank_name IN ('BRED', 'MCB', 'BMOI', 'BOA', 'BGFI', 'AFG', 'ACCES_BANQUE', 'BAOBAB', 'SIPEM')
                                                ),
                                            bank_code VARCHAR(5),
                                            branch_code VARCHAR(5),
                                            account_number VARCHAR(11),
                                            rib_key VARCHAR(2)
);

-- Mobile money account
CREATE TABLE IF NOT EXISTS mobile_money_account (
                                                    id VARCHAR PRIMARY KEY DEFAULT gen_random_uuid()::VARCHAR,
                                                    id_account VARCHAR UNIQUE REFERENCES account(id),
                                                    holder_name VARCHAR NOT NULL,
                                                    service_name VARCHAR CHECK (
                                                        service_name IN ('ORANGE_MONEY', 'MVOLA', 'AIRTEL_MONEY')
                                                        ),
                                                    phone_number VARCHAR NOT NULL
);

-- Cotisation plan
CREATE TABLE IF NOT EXISTS cotisation_plan (
                                               id VARCHAR PRIMARY KEY,
                                               label VARCHAR NOT NULL,
                                               id_collectivity VARCHAR REFERENCES collectivity(id),
                                               status VARCHAR DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
                                               frequency VARCHAR CHECK (frequency IN ('WEEKLY', 'MONTHLY', 'ANNUALLY', 'PUNCTUALLY')),
                                               eligible_from DATE,
                                               amount DECIMAL(15,2)
);

-- Transaction
CREATE TABLE IF NOT EXISTS transaction (
                                           id VARCHAR PRIMARY KEY DEFAULT gen_random_uuid()::VARCHAR,
                                           id_collectivity VARCHAR REFERENCES collectivity(id),
                                           id_member VARCHAR REFERENCES member(id),
                                           id_cotisation_plan VARCHAR REFERENCES cotisation_plan(id),
                                           transaction_type VARCHAR CHECK (transaction_type IN ('IN', 'OUT')),
                                           amount DECIMAL(15,2),
                                           transaction_date DATE DEFAULT CURRENT_DATE,
                                           payment_mode VARCHAR CHECK (
                                               payment_mode IN ('CASH', 'MOBILE_BANKING', 'BANK_TRANSFER')
                                               ),
                                           description VARCHAR,
                                           id_account VARCHAR REFERENCES account(id)
);

-- Activity table
CREATE TABLE IF NOT EXISTS activity (
                                        id VARCHAR PRIMARY KEY DEFAULT gen_random_uuid()::VARCHAR,
                                        id_collectivity VARCHAR REFERENCES collectivity(id),
                                        label VARCHAR NOT NULL,
                                        activity_type VARCHAR CHECK (activity_type IN ('MEETING', 'TRAINING', 'OTHER')),
                                        executive_date DATE,
                                        week_ordinal INTEGER CHECK (week_ordinal BETWEEN 1 AND 5),
                                        day_of_week VARCHAR CHECK (
                                            day_of_week IN ('MO', 'TU', 'WE', 'TH', 'FR', 'SA', 'SU')
                                            ),
                                        creation_date DATE DEFAULT CURRENT_DATE
);

-- Activity member occupation
CREATE TABLE IF NOT EXISTS activity_member_occupation (
                                                          id_activity VARCHAR REFERENCES activity(id),
                                                          occupation VARCHAR CHECK (
                                                              occupation IN ('JUNIOR', 'SENIOR', 'SECRETARY', 'TREASURER', 'VICE_PRESIDENT', 'PRESIDENT')
                                                              ),
                                                          PRIMARY KEY (id_activity, occupation)
);

-- Activity attendance
CREATE TABLE IF NOT EXISTS activity_attendance (
                                                   id VARCHAR PRIMARY KEY DEFAULT gen_random_uuid()::VARCHAR,
                                                   id_activity VARCHAR REFERENCES activity(id),
                                                   id_member VARCHAR REFERENCES member(id),
                                                   attendance_status VARCHAR DEFAULT 'UNDEFINED' CHECK (
                                                       attendance_status IN ('UNDEFINED', 'ATTENDED', 'MISSING')
                                                       ),
                                                   UNIQUE (id_activity, id_member)
);

-- ============================================
-- INSERT DATA FROM PDF ONLY
-- ============================================

-- Federation
INSERT INTO federation (id, name) VALUES ('fed-1', 'Fédération Agricole de Madagascar');

-- Collectivities (Tableau 1, Page 1)
INSERT INTO collectivity (id, number, name, speciality, creation_date, federation_approval, authorization_date, location, id_federation) VALUES
                                                                                                                                             ('col-1', '1', 'Mpanorina', 'Riziculture', '2026-01-01', TRUE, '2026-01-01', 'Ambatondrazaka', 'fed-1'),
                                                                                                                                             ('col-2', '2', 'Dobo voalahany', 'Pisciculture', '2026-01-01', TRUE, '2026-01-01', 'Ambatondrazaka', 'fed-1'),
                                                                                                                                             ('col-3', '3', 'Tantely mamy', 'Apiculture', '2026-01-01', TRUE, '2026-01-01', 'Brickaville', 'fed-1');

-- Members for Collectivity 1 (Tableau 2, Page 2)
INSERT INTO member (id, first_name, last_name, birth_date, gender, address, profession, phone_number, email, enrolment_date) VALUES
                                                                                                                                 ('C1-M1', 'Nom membre 1', 'Prénom membre 1', '1980-02-01', 'MALE', 'Lot II V M Ambato.', 'Riziculteur', '0341234567', 'member.1@fed-agri.mg', '2025-01-01'),
                                                                                                                                 ('C1-M2', 'Nom membre 2', 'Prénom membre 2', '1982-03-05', 'MALE', 'Lot II F Ambato.', 'Agriculteur', '0321234567', 'member.2@fed-agri.mg', '2025-01-01'),
                                                                                                                                 ('C1-M3', 'Nom membre 3', 'Prénom membre 3', '1992-03-10', 'MALE', 'Lot II J Ambato.', 'Collecteur', '0331234567', 'member.3@fed-agrimg', '2025-01-01'),
                                                                                                                                 ('C1-M4', 'Nom membre 4', 'Prénom membre 4', '1988-05-22', 'FEMALE', 'Lot A K 50 Ambato.', 'Distributeur', '0381234567', 'member.4@fed-agri.mg', '2025-01-01'),
                                                                                                                                 ('C1-M5', 'Nom membre 5', 'Prénom membre 5', '1999-08-21', 'MALE', 'Lot UV 80 Ambato.', 'Riziculteur', '0373434567', 'member.5@fed-agri.mg', '2025-01-01'),
                                                                                                                                 ('C1-M6', 'Nom membre 6', 'Prénom membre 6', '1998-08-22', 'FEMALE', 'Lot UV 6 Ambato.', 'Riziculteur', '0372234567', 'member.6@fed-agri.mg', '2025-01-01'),
                                                                                                                                 ('C1-M7', 'Nom membre 7', 'Prénom membre 7', '1998-01-31', 'MALE', 'Lot UV 7 Ambato.', 'Riziculteur', '0374234567', 'member.7@fed-agri.mg', '2025-01-01'),
                                                                                                                                 ('C1-M8', 'Nom membre 8', 'Prénom membre 6', '1975-08-20', 'MALE', 'Lot UV 8 Ambato.', 'Riziculteur', '0370234567', 'member.8@fed-agri.mg', '2025-01-01');

-- Members for Collectivity 2 (Tableau Page 3) - Different emails from C1
INSERT INTO member (id, first_name, last_name, birth_date, gender, address, profession, phone_number, email, enrolment_date) VALUES
                                                                                                                                 ('C2-M1', 'Nom membre 1', 'Prénom membre 1', '1980-02-01', 'MALE', 'Lot II V M Ambato.', 'Riziculteur', '0341234567', 'c2.member.1@fed-agri.mg', '2025-01-01'),
                                                                                                                                 ('C2-M2', 'Nom membre 2', 'Prénom membre 2', '1982-03-05', 'MALE', 'Lot II F Ambato.', 'Agriculteur', '0321234567', 'c2.member.2@fed-agri.mg', '2025-01-01'),
                                                                                                                                 ('C2-M3', 'Nom membre 3', 'Prénom membre 3', '1992-03-10', 'MALE', 'Lot II J Ambato.', 'Collecteur', '0331234567', 'c2.member.3@fed-agri.mg', '2025-01-01'),
                                                                                                                                 ('C2-M4', 'Nom membre 4', 'Prénom membre 4', '1988-05-22', 'FEMALE', 'Lot A K 50 Ambato.', 'Distributeur', '0381234567', 'c2.member.4@fed-agri.mg', '2025-01-01'),
                                                                                                                                 ('C2-M5', 'Nom membre 5', 'Prénom membre 5', '1999-08-21', 'MALE', 'Lot UV 80 Ambato.', 'Riziculteur', '0373434567', 'c2.member.5@fed-agri.mg', '2025-01-01'),
                                                                                                                                 ('C2-M6', 'Nom membre 6', 'Prénom membre 6', '1998-08-22', 'FEMALE', 'Lot UV 6 Ambato.', 'Riziculteur', '0372234567', 'c2.member.6@fed-agri.mg', '2025-01-01'),
                                                                                                                                 ('C2-M7', 'Nom membre 7', 'Prénom membre 7', '1998-01-31', 'MALE', 'Lot UV 7 Ambato.', 'Riziculteur', '0374234567', 'c2.member.7@fed-agri.mg', '2025-01-01'),
                                                                                                                                 ('C2-M8', 'Nom membre 8', 'Prénom membre 6', '1975-08-20', 'MALE', 'Lot UV 8 Ambato.', 'Riziculteur', '0370234567', 'c2.member.8@fed-agri.mg', '2025-01-01');

-- Members for Collectivity 3 (Tableau Page 4)
INSERT INTO member (id, first_name, last_name, birth_date, gender, address, profession, phone_number, email, enrolment_date) VALUES
                                                                                                                                 ('C3-M1', 'Nom membre 9', 'Prénom membre 9', '1988-01-02', 'MALE', 'Lot 33 J Antsirabe', 'Apiculteur', '034034567', 'member.9@fed-agri.mg', '2025-06-01'),
                                                                                                                                 ('C3-M2', 'Nom membre 10', 'Prénom membre 10', '1982-03-05', 'MALE', 'Lot 2 J Antsirabe', 'Agriculteur', '0338634567', 'member.10@fed-agri.mg', '2025-06-01'),
                                                                                                                                 ('C3-M3', 'Nom membre 11', 'Prénom membre 11', '1992-03-12', 'MALE', 'Lot 8 KM Antsirabe', 'Collecteur', '0338234567', 'member.11@fed-agrimg', '2025-06-01'),
                                                                                                                                 ('C3-M4', 'Nom membre 12', 'Prénom membre 12', '1988-05-10', 'FEMALE', 'Lot A K 50 Antsirabe', 'Distributeur', '0382334567', 'member.12@fed-agri.mg', '2025-06-01'),
                                                                                                                                 ('C3-M5', 'Nom membre 13', 'Prénom membre 13', '1999-08-11', 'MALE', 'Lot UV 80 Antsirabe.', 'Apiculteur', '0373365567', 'member.13@fed-agri.mg', '2025-06-01'),
                                                                                                                                 ('C3-M6', 'Nom membre 14', 'Prénom membre 14', '1998-08-09', 'FEMALE', 'Lot UV 6 Antsirabe.', 'Apiculteur', '0378234567', 'member.14@fed-agri.mg', '2025-06-01'),
                                                                                                                                 ('C3-M7', 'Nom membre 15', 'Prénom membre 15', '1998-01-13', 'MALE', 'Lot UV 7 Antsirabe', 'Apiculteur', '0374914567', 'member.15@fed-agri.mg', '2025-06-01'),
                                                                                                                                 ('C3-M8', 'Nom membre 16', 'Prénom membre 16', '1975-08-02', 'MALE', 'Lot UV 8 Antsirabe', 'Apiculteur', '0370634567', 'member.16@fed-agri.mg', '2025-06-01');

-- Member collectivity associations for Collectivity 1 (Tableau 2, Page 2)
INSERT INTO member_collectivity (id_member, id_collectivity, occupation, start_date) VALUES
                                                                                         ('C1-M1', 'col-1', 'PRESIDENT', '2026-01-01'),
                                                                                         ('C1-M2', 'col-1', 'VICE_PRESIDENT', '2026-01-01'),
                                                                                         ('C1-M3', 'col-1', 'SECRETARY', '2026-01-01'),
                                                                                         ('C1-M4', 'col-1', 'TREASURER', '2026-01-01'),
                                                                                         ('C1-M5', 'col-1', 'SENIOR', '2026-01-01'),
                                                                                         ('C1-M6', 'col-1', 'SENIOR', '2026-01-01'),
                                                                                         ('C1-M7', 'col-1', 'SENIOR', '2026-01-01'),
                                                                                         ('C1-M8', 'col-1', 'SENIOR', '2026-01-01');

-- Member collectivity associations for Collectivity 2 (Tableau Page 3)
INSERT INTO member_collectivity (id_member, id_collectivity, occupation, start_date) VALUES
                                                                                         ('C2-M1', 'col-2', 'SENIOR', '2026-01-01'),
                                                                                         ('C2-M2', 'col-2', 'SENIOR', '2026-01-01'),
                                                                                         ('C2-M3', 'col-2', 'SENIOR', '2026-01-01'),
                                                                                         ('C2-M4', 'col-2', 'SENIOR', '2026-01-01'),
                                                                                         ('C2-M5', 'col-2', 'PRESIDENT', '2026-01-01'),
                                                                                         ('C2-M6', 'col-2', 'VICE_PRESIDENT', '2026-01-01'),
                                                                                         ('C2-M7', 'col-2', 'SECRETARY', '2026-01-01'),
                                                                                         ('C2-M8', 'col-2', 'TREASURER', '2026-01-01');

-- Member collectivity associations for Collectivity 3 (Tableau Page 4)
INSERT INTO member_collectivity (id_member, id_collectivity, occupation, start_date) VALUES
                                                                                         ('C3-M1', 'col-3', 'PRESIDENT', '2026-01-01'),
                                                                                         ('C3-M2', 'col-3', 'VICE_PRESIDENT', '2026-01-01'),
                                                                                         ('C3-M3', 'col-3', 'SECRETARY', '2026-01-01'),
                                                                                         ('C3-M4', 'col-3', 'TREASURER', '2026-01-01'),
                                                                                         ('C3-M5', 'col-3', 'SENIOR', '2026-01-01'),
                                                                                         ('C3-M6', 'col-3', 'SENIOR', '2026-01-01'),
                                                                                         ('C3-M7', 'col-3', 'SENIOR', '2026-01-01'),
                                                                                         ('C3-M8', 'col-3', 'SENIOR', '2026-01-01');

-- Referees (from ID des membres référents column in PDF)
INSERT INTO member_referee (id_candidate, id_referee, relationship) VALUES
-- Collectivity 1 referees (Page 2)
('C1-M3', 'C1-M1', 'Parrainage'),
('C1-M3', 'C1-M2', 'Parrainage'),
('C1-M4', 'C1-M1', 'Parrainage'),
('C1-M4', 'C1-M2', 'Parrainage'),
('C1-M5', 'C1-M1', 'Parrainage'),
('C1-M5', 'C1-M2', 'Parrainage'),
('C1-M6', 'C1-M1', 'Parrainage'),
('C1-M6', 'C1-M2', 'Parrainage'),
('C1-M7', 'C1-M1', 'Parrainage'),
('C1-M7', 'C1-M2', 'Parrainage'),
('C1-M8', 'C1-M6', 'Parrainage'),
('C1-M8', 'C1-M7', 'Parrainage'),
-- Collectivity 2 referees (Page 3)
('C2-M3', 'C1-M1', 'Parrainage'),
('C2-M3', 'C1-M2', 'Parrainage'),
('C2-M4', 'C1-M1', 'Parrainage'),
('C2-M4', 'C1-M2', 'Parrainage'),
('C2-M5', 'C1-M1', 'Parrainage'),
('C2-M5', 'C1-M2', 'Parrainage'),
('C2-M6', 'C1-M1', 'Parrainage'),
('C2-M6', 'C1-M2', 'Parrainage'),
('C2-M7', 'C1-M1', 'Parrainage'),
('C2-M7', 'C1-M2', 'Parrainage'),
('C2-M8', 'C1-M6', 'Parrainage'),
('C2-M8', 'C1-M7', 'Parrainage'),
-- Collectivity 3 referees (Page 4)
('C3-M1', 'C1-M1', 'Parrainage'),
('C3-M1', 'C1-M2', 'Parrainage'),
('C3-M2', 'C1-M1', 'Parrainage'),
('C3-M2', 'C1-M2', 'Parrainage'),
('C3-M3', 'C3-M1', 'Parrainage'),
('C3-M3', 'C3-M2', 'Parrainage'),
('C3-M4', 'C3-M1', 'Parrainage'),
('C3-M4', 'C3-M2', 'Parrainage'),
('C3-M5', 'C3-M1', 'Parrainage'),
('C3-M5', 'C3-M2', 'Parrainage'),
('C3-M6', 'C3-M1', 'Parrainage'),
('C3-M6', 'C3-M2', 'Parrainage'),
('C3-M7', 'C3-M1', 'Parrainage'),
('C3-M7', 'C3-M2', 'Parrainage'),
('C3-M8', 'C3-M1', 'Parrainage'),
('C3-M8', 'C3-M2', 'Parrainage');

-- Accounts (Tableau Page 6)
INSERT INTO account (id, id_collectivity) VALUES
                                              ('C1-A-CASH', 'col-1'),
                                              ('C1-A-MOBILE-1', 'col-1'),
                                              ('C2-A-CASH', 'col-2'),
                                              ('C2-A-MOBILE-1', 'col-2'),
                                              ('C3-A-CASH', 'col-3');

-- Cash accounts (Tableau Page 6)
INSERT INTO cash_account (id_account) VALUES
                                          ('C1-A-CASH'),
                                          ('C2-A-CASH'),
                                          ('C3-A-CASH');

-- Mobile money accounts (Tableau Page 6)
INSERT INTO mobile_money_account (id_account, holder_name, service_name, phone_number) VALUES
                                                                                           ('C1-A-MOBILE-1', 'Mpanorina', 'ORANGE_MONEY', '0370489612'),
                                                                                           ('C2-A-MOBILE-1', 'Dobo voalohany', 'ORANGE_MONEY', '0320489612');

-- Cotisation plans (Tableau 5, 6, 7 Page 5)
INSERT INTO cotisation_plan (id, label, id_collectivity, status, frequency, eligible_from, amount) VALUES
                                                                                                       ('cot-1', 'Cotisation annuelle', 'col-1', 'ACTIVE', 'ANNUALLY', '2026-01-01', 100000),
                                                                                                       ('cot-2', 'Cotisation annuelle', 'col-2', 'ACTIVE', 'ANNUALLY', '2026-01-01', 100000),
                                                                                                       ('cot-3', 'Cotisation annuelle', 'col-3', 'ACTIVE', 'ANNUALLY', '2026-01-01', 50000);

-- Transactions/Payments for Collectivity 1 (Tableau 8 Page 7, Tableau 9 Page 8)
INSERT INTO transaction (id, id_collectivity, id_member, transaction_type, amount, transaction_date, payment_mode, id_account) VALUES
                                                                                                                                   ('trx-1-1', 'col-1', 'C1-M1', 'IN', 100000, '2026-01-01', 'CASH', 'C1-A-CASH'),
                                                                                                                                   ('trx-1-2', 'col-1', 'C1-M2', 'IN', 100000, '2026-01-01', 'CASH', 'C1-A-CASH'),
                                                                                                                                   ('trx-1-3', 'col-1', 'C1-M3', 'IN', 100000, '2026-01-01', 'CASH', 'C1-A-CASH'),
                                                                                                                                   ('trx-1-4', 'col-1', 'C1-M4', 'IN', 100000, '2026-01-01', 'CASH', 'C1-A-CASH'),
                                                                                                                                   ('trx-1-5', 'col-1', 'C1-M5', 'IN', 100000, '2026-01-01', 'CASH', 'C1-A-CASH'),
                                                                                                                                   ('trx-1-6', 'col-1', 'C1-M6', 'IN', 100000, '2026-01-01', 'CASH', 'C1-A-CASH'),
                                                                                                                                   ('trx-1-7', 'col-1', 'C1-M7', 'IN', 60000, '2026-01-01', 'CASH', 'C1-A-CASH'),
                                                                                                                                   ('trx-1-8', 'col-1', 'C1-M8', 'IN', 90000, '2026-01-01', 'CASH', 'C1-A-CASH');

-- Transactions/Payments for Collectivity 2 (Tableau 10 Page 9, Tableau 11 Page 10)
INSERT INTO transaction (id, id_collectivity, id_member, transaction_type, amount, transaction_date, payment_mode, id_account) VALUES
                                                                                                                                   ('trx-2-1', 'col-2', 'C2-M1', 'IN', 60000, '2026-01-01', 'CASH', 'C2-A-CASH'),
                                                                                                                                   ('trx-2-2', 'col-2', 'C2-M2', 'IN', 90000, '2026-01-01', 'CASH', 'C2-A-CASH'),
                                                                                                                                   ('trx-2-3', 'col-2', 'C2-M3', 'IN', 100000, '2026-01-01', 'CASH', 'C2-A-CASH'),
                                                                                                                                   ('trx-2-4', 'col-2', 'C2-M4', 'IN', 100000, '2026-01-01', 'CASH', 'C2-A-CASH'),
                                                                                                                                   ('trx-2-5', 'col-2', 'C2-M5', 'IN', 100000, '2026-01-01', 'CASH', 'C2-A-CASH'),
                                                                                                                                   ('trx-2-6', 'col-2', 'C2-M6', 'IN', 100000, '2026-01-01', 'CASH', 'C2-A-CASH'),
                                                                                                                                   ('trx-2-7', 'col-2', 'C2-M7', 'IN', 40000, '2026-01-01', 'MOBILE_BANKING', 'C2-A-MOBILE-1'),
                                                                                                                                   ('trx-2-8', 'col-2', 'C2-M8', 'IN', 60000, '2026-01-01', 'MOBILE_BANKING', 'C2-A-MOBILE-1');