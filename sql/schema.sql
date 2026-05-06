-- ============================================
-- DATABASE SCHEMA
-- ============================================

-- Federation table
CREATE TABLE IF NOT EXISTS federation (
                                          id VARCHAR PRIMARY KEY DEFAULT 'fed-1',
                                          name VARCHAR NOT NULL DEFAULT 'Fédération Agricole de Madagascar',
                                          creation_date TIMESTAMP DEFAULT NOW()
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
                                      phone_number INTEGER,
                                      email VARCHAR UNIQUE NOT NULL,
                                      enrolment_date TIMESTAMP DEFAULT NOW(),
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
                                            creation_datetime TIMESTAMP DEFAULT NOW(),
                                            federation_approval BOOLEAN NOT NULL,
                                            authorization_date TIMESTAMP,
                                            location VARCHAR NOT NULL,
                                            id_federation VARCHAR REFERENCES federation(id)
);

-- Member collectivity association
CREATE TABLE IF NOT EXISTS member_collectivity (
                                                   id_member VARCHAR REFERENCES member(id),
                                                   id_collectivity VARCHAR REFERENCES collectivity(id),
                                                   occupation VARCHAR CHECK (occupation IN ('JUNIOR', 'SENIOR', 'SECRETARY', 'TREASURER', 'VICE_PRESIDENT', 'PRESIDENT')),
                                                   start_date TIMESTAMP DEFAULT NOW(),
                                                   end_date TIMESTAMP,
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
                                            bank_name VARCHAR CHECK (bank_name IN ('BRED', 'MCB', 'BMOI', 'BOA', 'BGFI', 'AFG', 'ACCES_BANQUE', 'BAOBAB', 'SIPEM')),
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
                                                    service_name VARCHAR CHECK (service_name IN ('ORANGE_MONEY', 'MVOLA', 'AIRTEL_MONEY')),
                                                    phone_number VARCHAR NOT NULL
);

-- Cotisation (Membership fee) plan
CREATE TABLE IF NOT EXISTS mobile_money_account (
                                                    id VARCHAR PRIMARY KEY DEFAULT gen_random_uuid()::VARCHAR,
                                                    id_account VARCHAR UNIQUE REFERENCES account(id),
                                                    holder_name VARCHAR NOT NULL,
                                                    service_name VARCHAR CHECK (service_name IN ('ORANGE_MONEY', 'MVOLA', 'AIRTEL_MONEY')),
                                                    phone_number INTEGER NOT NULL
);

-- Transaction
CREATE TABLE IF NOT EXISTS transaction (
                                           id VARCHAR PRIMARY KEY DEFAULT gen_random_uuid()::VARCHAR,
                                           id_collectivity VARCHAR REFERENCES collectivity(id),
                                           id_member VARCHAR REFERENCES member(id),
                                           id_cotisation_plan VARCHAR REFERENCES cotisation_plan(id),
                                           transaction_type VARCHAR CHECK (transaction_type IN ('IN', 'OUT')),
                                           amount DECIMAL(15,2),
                                           transaction_date TIMESTAMP DEFAULT NOW(),
                                           payment_mode VARCHAR CHECK (payment_mode IN ('CASH', 'MOBILE_BANKING', 'BANK_TRANSFER')),
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
                                        day_of_week VARCHAR CHECK (day_of_week IN ('MO', 'TU', 'WE', 'TH', 'FR', 'SA', 'SU')),
                                        creation_date TIMESTAMP DEFAULT NOW()
);

-- Activity member occupation concern
CREATE TABLE IF NOT EXISTS activity_member_occupation (
                                                          id_activity VARCHAR REFERENCES activity(id),
                                                          occupation VARCHAR CHECK (occupation IN ('JUNIOR', 'SENIOR', 'SECRETARY', 'TREASURER', 'VICE_PRESIDENT', 'PRESIDENT')),
                                                          PRIMARY KEY (id_activity, occupation)
);

-- Activity attendance
CREATE TABLE IF NOT EXISTS activity_attendance (
                                                   id VARCHAR PRIMARY KEY DEFAULT gen_random_uuid()::VARCHAR,
                                                   id_activity VARCHAR REFERENCES activity(id),
                                                   id_member VARCHAR REFERENCES member(id),
                                                   attendance_status VARCHAR DEFAULT 'UNDEFINED' CHECK (attendance_status IN ('UNDEFINED', 'ATTENDED', 'MISSING')),
                                                   UNIQUE (id_activity, id_member)
);

-- Insert federation
INSERT INTO federation (id, name) VALUES ('fed-1', 'Fédération Agricole de Madagascar');

-- Insert sample data from PDF
INSERT INTO collectivity (id, number, name, speciality, creation_datetime, federation_approval, authorization_date, location, id_federation) VALUES
                                                                                                                                                 ('col-1', '1', 'Mpanorina', 'Riziculture', '2026-01-01', TRUE, '2026-01-01', 'Ambatondrazaka', 'fed-1'),
                                                                                                                                                 ('col-2', '2', 'Dobo voalahany', 'Pisciculture', '2026-01-01', TRUE, '2026-01-01', 'Ambatondrazaka', 'fed-1'),
                                                                                                                                                 ('col-3', '3', 'Tantely mamy', 'Apiculture', '2026-01-01', TRUE, '2026-01-01', 'Brickaville', 'fed-1');

-- Insert accounts from PDF
INSERT INTO account (id, id_collectivity) VALUES
                                              ('C1-A-CASH', 'col-1'),
                                              ('C1-A-MOBILE-1', 'col-1'),
                                              ('C2-A-CASH', 'col-2'),
                                              ('C2-A-MOBILE-1', 'col-2'),
                                              ('C3-A-CASH', 'col-3');

INSERT INTO cash_account (id_account) VALUES
                                          ('C1-A-CASH'),
                                          ('C2-A-CASH'),
                                          ('C3-A-CASH');

INSERT INTO mobile_money_account (id_account, holder_name, service_name, phone_number) VALUES
                                                                                           ('C1-A-MOBILE-1', 'Mpanorina', 'ORANGE_MONEY', '0370489612'),
                                                                                           ('C2-A-MOBILE-1', 'Dobo voalohany', 'ORANGE_MONEY', '0320489612');

-- Insert cotisation plans from PDF
INSERT INTO cotisation_plan (id, label, id_collectivity, status, frequency, eligible_from, amount) VALUES
                                                                                                       ('cot-1', 'Cotisation annuelle', 'col-1', 'ACTIVE', 'ANNUALLY', '2026-01-01', 100000),
                                                                                                       ('cot-2', 'Cotisation annuelle', 'col-2', 'ACTIVE', 'ANNUALLY', '2026-01-01', 100000),
                                                                                                       ('cot-3', 'Cotisation annuelle', 'col-3', 'ACTIVE', 'ANNUALLY', '2026-01-01', 50000);