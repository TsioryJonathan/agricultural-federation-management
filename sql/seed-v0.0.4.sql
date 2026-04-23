-- ============================================================
-- SEED DATA v0.0.4 - Agricultural Federation Management
-- Compatible with schema v0.0.4
-- ============================================================

-- FEDERATION
INSERT INTO federation (id, cotisation_percentage) VALUES ('1', 10.00);

-- COLLECTIVITIES
INSERT INTO collectivity (id, number, name, speciality, creation_datetime, federation_approval, authorization_date, id_federation, location)
VALUES
    ('col-1', '1', 'Mpanorina', 'Riziculture', '2026-01-01 00:00:00'::timestamp, true, '2026-01-01 00:00:00'::timestamp, '1', 'Ambatondrazaka'),
    ('col-2', '2', 'Dobo voalohany', 'Pisciculture', '2026-01-01 00:00:00'::timestamp, true, '2026-01-01 00:00:00'::timestamp, '1', 'Ambatondrazaka'),
    ('col-3', '3', 'Tantely mamy', 'Apiculture', '2026-01-01 00:00:00'::timestamp, true, '2026-01-01 00:00:00'::timestamp, '1', 'Brickaville');

-- ============================================================
-- MEMBERS
-- ============================================================

-- Collectivité 1 members
INSERT INTO member (id, first_name, last_name, birth_date, enrolment_date, address, email, phone_number, profession, gender)
VALUES
    ('C1-M1', 'Nom membre 1', 'Prénom membre 1', '1980-02-01', '2026-01-01 00:00:00'::timestamp, 'Lot II V M Ambato.', 'c1m1@fed-agri.mg', '0341111111', 'Riziculteur', 'MALE'),
    ('C1-M2', 'Nom membre 2', 'Prénom membre 2', '1982-03-05', '2026-01-01 00:00:00'::timestamp, 'Lot II F Ambato.', 'c1m2@fed-agri.mg', '0321222222', 'Agriculteur', 'MALE'),
    ('C1-M3', 'Nom membre 3', 'Prénom membre 3', '1992-03-10', '2026-01-01 00:00:00'::timestamp, 'Lot II J Ambato.', 'c1m3@fed-agri.mg', '0331333333', 'Collecteur', 'MALE'),
    ('C1-M4', 'Nom membre 4', 'Prénom membre 4', '1988-05-22', '2026-01-01 00:00:00'::timestamp, 'Lot A K 50 Ambato.', 'c1m4@fed-agri.mg', '0381444444', 'Distributeur', 'FEMALE'),
    ('C1-M5', 'Nom membre 5', 'Prénom membre 5', '1999-08-21', '2026-01-01 00:00:00'::timestamp, 'Lot UV 80 Ambato.', 'c1m5@fed-agri.mg', '0371555555', 'Riziculteur', 'MALE'),
    ('C1-M6', 'Nom membre 6', 'Prénom membre 6', '1998-08-22', '2026-01-01 00:00:00'::timestamp, 'Lot UV 6 Ambato.', 'c1m6@fed-agri.mg', '0371666666', 'Riziculteur', 'FEMALE'),
    ('C1-M7', 'Nom membre 7', 'Prénom membre 7', '1998-01-31', '2026-01-01 00:00:00'::timestamp, 'Lot UV 7 Ambato.', 'c1m7@fed-agri.mg', '0371777777', 'Riziculteur', 'MALE'),
    ('C1-M8', 'Nom membre 8', 'Prénom membre 8', '1975-08-20', '2026-01-01 00:00:00'::timestamp, 'Lot UV 8 Ambato.', 'c1m8@fed-agri.mg', '0371888888', 'Riziculteur', 'MALE');

-- Collectivité 2 members
INSERT INTO member (id, first_name, last_name, birth_date, enrolment_date, address, email, phone_number, profession, gender)
VALUES
    ('C2-M1', 'Nom membre 9', 'Prénom membre 9', '1980-02-01', '2026-01-01 00:00:00'::timestamp, 'Lot II V M Ambato.', 'c2m1@fed-agri.mg', '0342111111', 'Riziculteur', 'MALE'),
    ('C2-M2', 'Nom membre 10', 'Prénom membre 10', '1982-03-05', '2026-01-01 00:00:00'::timestamp, 'Lot II F Ambato.', 'c2m2@fed-agri.mg', '0322222222', 'Agriculteur', 'MALE'),
    ('C2-M3', 'Nom membre 11', 'Prénom membre 11', '1992-03-10', '2026-01-01 00:00:00'::timestamp, 'Lot II J Ambato.', 'c2m3@fed-agri.mg', '0332333333', 'Collecteur', 'MALE'),
    ('C2-M4', 'Nom membre 12', 'Prénom membre 12', '1988-05-22', '2026-01-01 00:00:00'::timestamp, 'Lot A K 50 Ambato.', 'c2m4@fed-agri.mg', '0382444444', 'Distributeur', 'FEMALE'),
    ('C2-M5', 'Nom membre 13', 'Prénom membre 13', '1999-08-21', '2026-01-01 00:00:00'::timestamp, 'Lot UV 80 Ambato.', 'c2m5@fed-agri.mg', '0372555555', 'Riziculteur', 'MALE'),
    ('C2-M6', 'Nom membre 14', 'Prénom membre 14', '1998-08-22', '2026-01-01 00:00:00'::timestamp, 'Lot UV 6 Ambato.', 'c2m6@fed-agri.mg', '0372666666', 'Riziculteur', 'FEMALE'),
    ('C2-M7', 'Nom membre 15', 'Prénom membre 15', '1998-01-31', '2026-01-01 00:00:00'::timestamp, 'Lot UV 7 Ambato.', 'c2m7@fed-agri.mg', '0372777777', 'Riziculteur', 'MALE'),
    ('C2-M8', 'Nom membre 16', 'Prénom membre 16', '1975-08-20', '2026-01-01 00:00:00'::timestamp, 'Lot UV 8 Ambato.', 'c2m8@fed-agri.mg', '0372888888', 'Riziculteur', 'MALE');

-- Collectivité 3 members
INSERT INTO member (id, first_name, last_name, birth_date, enrolment_date, address, email, phone_number, profession, gender)
VALUES
    ('C3-M1', 'Nom membre 17', 'Prénom frère 17', '1988-01-02', '2026-01-01 00:00:00'::timestamp, 'Lot 33 J Antsirabe', 'c3m1@fed-agri.mg', '0343111111', 'Apiculteur', 'MALE'),
    ('C3-M2', 'Nom membre 18', 'Prénom frère 18', '1982-03-05', '2026-01-01 00:00:00'::timestamp, 'Lot 2 J Antsirabe', 'c3m2@fed-agri.mg', '0333222222', 'Agriculteur', 'MALE'),
    ('C3-M3', 'Nom membre 19', 'Prénom frère 19', '1992-03-12', '2026-01-01 00:00:00'::timestamp, 'Lot 8 KM Antsirabe', 'c3m3@fed-agri.mg', '0333333333', 'Collecteur', 'MALE'),
    ('C3-M4', 'Nom membre 20', 'Prénom frère 20', '1988-05-10', '2026-01-01 00:00:00'::timestamp, 'Lot A K 50 Antsirabe', 'c3m4@fed-agri.mg', '0383444444', 'Distributeur', 'FEMALE'),
    ('C3-M5', 'Nom membre 21', 'Prénom frère 21', '1999-08-11', '2026-01-01 00:00:00'::timestamp, 'Lot UV 80 Antsirabe', 'c3m5@fed-agri.mg', '0373555555', 'Apiculteur', 'MALE'),
    ('C3-M6', 'Nom membre 22', 'Prénom frère 22', '1998-08-09', '2026-01-01 00:00:00'::timestamp, 'Lot UV 6 Antsirabe', 'c3m6@fed-agri.mg', '0373666666', 'Apiculteur', 'FEMALE'),
    ('C3-M7', 'Nom membre 23', 'Prénom frère 23', '1998-01-13', '2026-01-01 00:00:00'::timestamp, 'Lot UV 7 Antsirabe', 'c3m7@fed-agri.mg', '0373777777', 'Apiculteur', 'MALE'),
    ('C3-M8', 'Nom membre 24', 'Prénom frère 24', '1975-08-02', '2026-01-01 00:00:00'::timestamp, 'Lot UV 8 Antsirabe', 'c3m8@fed-agri.mg', '0373888888', 'Apiculteur', 'MALE');

-- ============================================================
-- MEMBER_COLLECTIVITY
-- ============================================================

INSERT INTO member_collectivity (id, id_member, id_collectivity, occupation, start_date)
VALUES
    ('mc-col1-1', 'C1-M1', 'col-1', 'PRESIDENT', '2026-01-01 00:00:00'::timestamp),
    ('mc-col1-2', 'C1-M2', 'col-1', 'VICE_PRESIDENT', '2026-01-01 00:00:00'::timestamp),
    ('mc-col1-3', 'C1-M3', 'col-1', 'SECRETARY', '2026-01-01 00:00:00'::timestamp),
    ('mc-col1-4', 'C1-M4', 'col-1', 'TREASURER', '2026-01-01 00:00:00'::timestamp),
    ('mc-col1-5', 'C1-M5', 'col-1', 'SENIOR', '2026-01-01 00:00:00'::timestamp),
    ('mc-col1-6', 'C1-M6', 'col-1', 'SENIOR', '2026-01-01 00:00:00'::timestamp),
    ('mc-col1-7', 'C1-M7', 'col-1', 'SENIOR', '2026-01-01 00:00:00'::timestamp),
    ('mc-col1-8', 'C1-M8', 'col-1', 'SENIOR', '2026-01-01 00:00:00'::timestamp),
    ('mc-col2-1', 'C2-M1', 'col-2', 'PRESIDENT', '2026-01-01 00:00:00'::timestamp),
    ('mc-col2-2', 'C2-M2', 'col-2', 'VICE_PRESIDENT', '2026-01-01 00:00:00'::timestamp),
    ('mc-col2-3', 'C2-M3', 'col-2', 'SECRETARY', '2026-01-01 00:00:00'::timestamp),
    ('mc-col2-4', 'C2-M4', 'col-2', 'TREASURER', '2026-01-01 00:00:00'::timestamp),
    ('mc-col2-5', 'C2-M5', 'col-2', 'SENIOR', '2026-01-01 00:00:00'::timestamp),
    ('mc-col2-6', 'C2-M6', 'col-2', 'SENIOR', '2026-01-01 00:00:00'::timestamp),
    ('mc-col2-7', 'C2-M7', 'col-2', 'SENIOR', '2026-01-01 00:00:00'::timestamp),
    ('mc-col2-8', 'C2-M8', 'col-2', 'SENIOR', '2026-01-01 00:00:00'::timestamp),
    ('mc-col3-1', 'C3-M1', 'col-3', 'PRESIDENT', '2026-01-01 00:00:00'::timestamp),
    ('mc-col3-2', 'C3-M2', 'col-3', 'VICE_PRESIDENT', '2026-01-01 00:00:00'::timestamp),
    ('mc-col3-3', 'C3-M3', 'col-3', 'SECRETARY', '2026-01-01 00:00:00'::timestamp),
    ('mc-col3-4', 'C3-M4', 'col-3', 'TREASURER', '2026-01-01 00:00:00'::timestamp),
    ('mc-col3-5', 'C3-M5', 'col-3', 'SENIOR', '2026-01-01 00:00:00'::timestamp),
    ('mc-col3-6', 'C3-M6', 'col-3', 'SENIOR', '2026-01-01 00:00:00'::timestamp),
    ('mc-col3-7', 'C3-M7', 'col-3', 'SENIOR', '2026-01-01 00:00:00'::timestamp),
    ('mc-col3-8', 'C3-M8', 'col-3', 'SENIOR', '2026-01-01 00:00:00'::timestamp);

-- ============================================================
-- MEMBER_REFEREE
-- ============================================================

INSERT INTO member_referee (id, id_candidate, id_referee, id_collectivity, relationship, created_at)
VALUES
    ('ref-C1-M3-1', 'C1-M3', 'C1-M1', 'col-1', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C1-M3-2', 'C1-M3', 'C1-M2', 'col-1', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C1-M4-1', 'C1-M4', 'C1-M1', 'col-1', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C1-M4-2', 'C1-M4', 'C1-M2', 'col-1', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C1-M5-1', 'C1-M5', 'C1-M1', 'col-1', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C1-M5-2', 'C1-M5', 'C1-M2', 'col-1', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C1-M6-1', 'C1-M6', 'C1-M1', 'col-1', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C1-M6-2', 'C1-M6', 'C1-M2', 'col-1', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C1-M7-1', 'C1-M7', 'C1-M1', 'col-1', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C1-M7-2', 'C1-M7', 'C1-M2', 'col-1', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C1-M8-1', 'C1-M8', 'C1-M6', 'col-1', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C1-M8-2', 'C1-M8', 'C1-M7', 'col-1', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C2-M3-1', 'C2-M3', 'C2-M1', 'col-2', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C2-M3-2', 'C2-M3', 'C2-M2', 'col-2', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C2-M4-1', 'C2-M4', 'C2-M1', 'col-2', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C2-M4-2', 'C2-M4', 'C2-M2', 'col-2', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C2-M5-1', 'C2-M5', 'C2-M1', 'col-2', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C2-M5-2', 'C2-M5', 'C2-M2', 'col-2', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C2-M6-1', 'C2-M6', 'C2-M1', 'col-2', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C2-M6-2', 'C2-M6', 'C2-M2', 'col-2', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C2-M7-1', 'C2-M7', 'C2-M1', 'col-2', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C2-M7-2', 'C2-M7', 'C2-M2', 'col-2', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C2-M8-1', 'C2-M8', 'C2-M6', 'col-2', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C2-M8-2', 'C2-M8', 'C2-M7', 'col-2', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C3-M3-1', 'C3-M3', 'C3-M1', 'col-3', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C3-M3-2', 'C3-M3', 'C3-M2', 'col-3', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C3-M4-1', 'C3-M4', 'C3-M1', 'col-3', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C3-M4-2', 'C3-M4', 'C3-M2', 'col-3', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C3-M5-1', 'C3-M5', 'C3-M1', 'col-3', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C3-M5-2', 'C3-M5', 'C3-M2', 'col-3', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C3-M6-1', 'C3-M6', 'C3-M1', 'col-3', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C3-M6-2', 'C3-M6', 'C3-M2', 'col-3', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C3-M7-1', 'C3-M7', 'C3-M1', 'col-3', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C3-M7-2', 'C3-M7', 'C3-M2', 'col-3', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C3-M8-1', 'C3-M8', 'C3-M1', 'col-3', 'collegues', '2026-01-01 00:00:00'::timestamp),
    ('ref-C3-M8-2', 'C3-M8', 'C3-M2', 'col-3', 'collegues', '2026-01-01 00:00:00'::timestamp);

-- ============================================================
-- COTISATION_PLAN
-- ============================================================

INSERT INTO cotisation_plan (id, id_collectivity, label, frequency, amount, eligible_from, is_active)
VALUES
    ('cot-1', 'col-1', 'Cotisation annuelle', 'ANNUALLY', 100000.00, '2026-01-01'::date, true),
    ('cot-2', 'col-2', 'Cotisation annuelle', 'ANNUALLY', 100000.00, '2026-01-01'::date, true),
    ('cot-3', 'col-3', 'Cotisation annuelle', 'ANNUALLY', 50000.00, '2026-01-01'::date, true);

-- ============================================================
-- ACCOUNTS
-- ============================================================

INSERT INTO account (id, id_collectivity, id_federation)
VALUES
    ('acc-col1-1', 'col-1', NULL),
    ('acc-col1-2', 'col-1', NULL),
    ('acc-col2-1', 'col-2', NULL),
    ('acc-col2-2', 'col-2', NULL),
    ('acc-col3-1', 'col-3', NULL);

INSERT INTO cash_account (id, id_account)
VALUES
    ('cash-col1-1', 'acc-col1-1'),
    ('cash-col2-1', 'acc-col2-1'),
    ('cash-col3-1', 'acc-col3-1');

INSERT INTO mobile_money_account (id, id_account, holder_name, service_name, phone_number)
VALUES
    ('mobile-col1-1', 'acc-col1-2', 'Mpanorina', 'ORANGE_MONEY', '0370489612'),
    ('mobile-col2-1', 'acc-col2-2', 'Dobo voalohany', 'ORANGE_MONEY', '0320489612');

-- ============================================================
-- TRANSACTIONS
-- ============================================================

INSERT INTO transaction (id, id_member, id_collectivity, id_cotisation_plan, id_account, transaction_type, amount, transaction_date, payment_mode, description)
VALUES
    ('tx-col1-1', 'C1-M1', 'col-1', 'cot-1', 'acc-col1-1', 'IN', 100000.00, '2026-01-01 00:00:00'::timestamp, 'CASH', 'Cotisation annuelle'),
    ('tx-col1-2', 'C1-M2', 'col-1', 'cot-1', 'acc-col1-1', 'IN', 100000.00, '2026-01-01 00:00:00'::timestamp, 'CASH', 'Cotisation annuelle'),
    ('tx-col1-3', 'C1-M3', 'col-1', 'cot-1', 'acc-col1-1', 'IN', 100000.00, '2026-01-01 00:00:00'::timestamp, 'CASH', 'Cotisation annuelle'),
    ('tx-col1-4', 'C1-M4', 'col-1', 'cot-1', 'acc-col1-1', 'IN', 100000.00, '2026-01-01 00:00:00'::timestamp, 'CASH', 'Cotisation annuelle'),
    ('tx-col1-5', 'C1-M5', 'col-1', 'cot-1', 'acc-col1-1', 'IN', 100000.00, '2026-01-01 00:00:00'::timestamp, 'CASH', 'Cotisation annuelle'),
    ('tx-col1-6', 'C1-M6', 'col-1', 'cot-1', 'acc-col1-1', 'IN', 100000.00, '2026-01-01 00:00:00'::timestamp, 'CASH', 'Cotisation annuelle'),
    ('tx-col1-7', 'C1-M7', 'col-1', 'cot-1', 'acc-col1-1', 'IN', 60000.00, '2026-01-01 00:00:00'::timestamp, 'CASH', 'Cotisation annuelle'),
    ('tx-col1-8', 'C1-M8', 'col-1', 'cot-1', 'acc-col1-1', 'IN', 90000.00, '2026-01-01 00:00:00'::timestamp, 'CASH', 'Cotisation annuelle'),
    ('tx-col2-1', 'C2-M1', 'col-2', 'cot-2', 'acc-col2-1', 'IN', 60000.00, '2026-01-01 00:00:00'::timestamp, 'CASH', 'Cotisation annuelle'),
    ('tx-col2-2', 'C2-M2', 'col-2', 'cot-2', 'acc-col2-1', 'IN', 90000.00, '2026-01-01 00:00:00'::timestamp, 'CASH', 'Cotisation annuelle'),
    ('tx-col2-3', 'C2-M3', 'col-2', 'cot-2', 'acc-col2-1', 'IN', 100000.00, '2026-01-01 00:00:00'::timestamp, 'CASH', 'Cotisation annuelle'),
    ('tx-col2-4', 'C2-M4', 'col-2', 'cot-2', 'acc-col2-1', 'IN', 100000.00, '2026-01-01 00:00:00'::timestamp, 'CASH', 'Cotisation annuelle'),
    ('tx-col2-5', 'C2-M5', 'col-2', 'cot-2', 'acc-col2-1', 'IN', 100000.00, '2026-01-01 00:00:00'::timestamp, 'CASH', 'Cotisation annuelle'),
    ('tx-col2-6', 'C2-M6', 'col-2', 'cot-2', 'acc-col2-1', 'IN', 100000.00, '2026-01-01 00:00:00'::timestamp, 'CASH', 'Cotisation annuelle'),
    ('tx-col2-7', 'C2-M7', 'col-2', 'cot-2', 'acc-col2-2', 'IN', 40000.00, '2026-01-01 00:00:00'::timestamp, 'MOBILE_BANKING', 'Cotisation annuelle'),
    ('tx-col2-8', 'C2-M8', 'col-2', 'cot-2', 'acc-col2-2', 'IN', 60000.00, '2026-01-01 00:00:00'::timestamp, 'MOBILE_BANKING', 'Cotisation annuelle');