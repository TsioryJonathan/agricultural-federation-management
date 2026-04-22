-- ============================================================
-- INSERT TEST DATA FOR TRANSACTIONS
-- ============================================================

-- 1. First, create accounts for Collectivity #1
INSERT INTO account (id, id_collectivity, id_federation) VALUES
                                                             (1, 1, NULL),  -- Cash account
                                                             (2, 1, NULL),  -- Bank account (BMOI)
                                                             (3, 1, NULL);  -- Mobile Money account (Orange Money)

-- 2. Create cash account
INSERT INTO cash_account (id_account) VALUES (1);

-- 3. Create bank account (BMOI)
INSERT INTO bank_account (id_account, holder_name, bank_name, bank_code, branch_code, account_number, rib_key)
VALUES (2, 'Collectivité Antananarivo', 'BMOI', '00015', '00001', '12345678901', '12');

-- 4. Create mobile money account (Orange Money)
INSERT INTO mobile_money_account (id_account, holder_name, service_name, phone_number)
VALUES (3, 'Rabe Pierre', 'ORANGE_MONEY', '0341234567');

-- ============================================================
-- INSERT COTISATION PLANS
-- ============================================================

-- Annual cotisation plan for 2026
INSERT INTO cotisation_plan (id_collectivity, label, frequency, amount, eligible_from, is_active)
VALUES (1, 'Cotisation Annuelle 2026', 'ANNUALLY', 200000.00, '2026-01-01', true);

-- Monthly cotisation plan
INSERT INTO cotisation_plan (id_collectivity, label, frequency, amount, eligible_from, is_active)
VALUES (1, 'Cotisation Mensuelle', 'MONTHLY', 20000.00, '2026-01-01', true);

-- Punctual cotisation for special event
INSERT INTO cotisation_plan (id_collectivity, label, frequency, amount, eligible_from, is_active)
VALUES (1, 'Cotisation Événement Spécial', 'PUNCTUALLY', 50000.00, '2026-04-01', true);

-- ============================================================
-- INSERT TRANSACTIONS (IN - Member Payments)
-- ============================================================

-- Member 1 (Rasoa - President) - Registration fee (50,000 Ar) via Mobile Money
INSERT INTO transaction (id_member, id_collectivity, id_cotisation_plan, id_account, transaction_type, amount, transaction_date, payment_mode, description)
VALUES (1, 1, NULL, 3, 'IN', 50000.00, '2026-01-15 10:30:00', 'MOBILE_BANKING', 'Frais d''adhésion - Rasoa');

-- Member 1 (Rasoa) - Annual cotisation 2026 via Bank Transfer
INSERT INTO transaction (id_member, id_collectivity, id_cotisation_plan, id_account, transaction_type, amount, transaction_date, payment_mode, description)
VALUES (1, 1, 1, 2, 'IN', 200000.00, '2026-01-15 10:35:00', 'BANK_TRANSFER', 'Cotisation annuelle 2026 - Rasoa');

-- Member 2 (Jean - Vice President) - Registration fee via Cash
INSERT INTO transaction (id_member, id_collectivity, id_cotisation_plan, id_account, transaction_type, amount, transaction_date, payment_mode, description)
VALUES (2, 1, NULL, 1, 'IN', 50000.00, '2026-01-20 14:15:00', 'CASH', 'Frais d''adhésion - Jean');

-- Member 2 (Jean) - Annual cotisation 2026 via Mobile Money
INSERT INTO transaction (id_member, id_collectivity, id_cotisation_plan, id_account, transaction_type, amount, transaction_date, payment_mode, description)
VALUES (2, 1, 1, 3, 'IN', 200000.00, '2026-01-20 14:20:00', 'MOBILE_BANKING', 'Cotisation annuelle 2026 - Jean');

-- Member 3 (Marie - Treasurer) - Registration fee via Mobile Money
INSERT INTO transaction (id_member, id_collectivity, id_cotisation_plan, id_account, transaction_type, amount, transaction_date, payment_mode, description)
VALUES (3, 1, NULL, 3, 'IN', 50000.00, '2026-02-05 09:45:00', 'MOBILE_BANKING', 'Frais d''adhésion - Marie');

-- Member 3 (Marie) - Annual cotisation 2026 via Bank Transfer
INSERT INTO transaction (id_member, id_collectivity, id_cotisation_plan, id_account, transaction_type, amount, transaction_date, payment_mode, description)
VALUES (3, 1, 1, 2, 'IN', 200000.00, '2026-02-05 09:50:00', 'BANK_TRANSFER', 'Cotisation annuelle 2026 - Marie');

-- Member 3 (Marie) - Monthly cotisation March 2026 via Cash
INSERT INTO transaction (id_member, id_collectivity, id_cotisation_plan, id_account, transaction_type, amount, transaction_date, payment_mode, description)
VALUES (3, 1, 2, 1, 'IN', 20000.00, '2026-03-01 11:00:00', 'CASH', 'Cotisation mensuelle Mars 2026 - Marie');

-- Member 4 (Pierre - Secretary) - Registration fee via Bank Transfer
INSERT INTO transaction (id_member, id_collectivity, id_cotisation_plan, id_account, transaction_type, amount, transaction_date, payment_mode, description)
VALUES (4, 1, NULL, 2, 'IN', 50000.00, '2026-02-10 16:30:00', 'BANK_TRANSFER', 'Frais d''adhésion - Pierre');

-- Member 4 (Pierre) - Annual cotisation 2026 via Mobile Money
INSERT INTO transaction (id_member, id_collectivity, id_cotisation_plan, id_account, transaction_type, amount, transaction_date, payment_mode, description)
VALUES (4, 1, 1, 3, 'IN', 200000.00, '2026-02-10 16:35:00', 'MOBILE_BANKING', 'Cotisation annuelle 2026 - Pierre');

-- Member 5 (Sophie - Senior) - Registration fee via Cash
INSERT INTO transaction (id_member, id_collectivity, id_cotisation_plan, id_account, transaction_type, amount, transaction_date, payment_mode, description)
VALUES (5, 1, NULL, 1, 'IN', 50000.00, '2026-03-15 13:20:00', 'CASH', 'Frais d''adhésion - Sophie');

-- Member 5 (Sophie) - Annual cotisation 2026 via Mobile Money
INSERT INTO transaction (id_member, id_collectivity, id_cotisation_plan, id_account, transaction_type, amount, transaction_date, payment_mode, description)
VALUES (5, 1, 1, 3, 'IN', 200000.00, '2026-03-15 13:25:00', 'MOBILE_BANKING', 'Cotisation annuelle 2026 - Sophie');

-- Member 5 (Sophie) - Punctual cotisation for special event via Mobile Money
INSERT INTO transaction (id_member, id_collectivity, id_cotisation_plan, id_account, transaction_type, amount, transaction_date, payment_mode, description)
VALUES (5, 1, 3, 3, 'IN', 50000.00, '2026-04-01 10:00:00', 'MOBILE_BANKING', 'Cotisation événement spécial - Sophie');

-- Member 6 (Marie 2) - Registration fee via Mobile Money (recent)
INSERT INTO transaction (id_member, id_collectivity, id_cotisation_plan, id_account, transaction_type, amount, transaction_date, payment_mode, description)
VALUES (6, 1, NULL, 3, 'IN', 50000.00, '2026-04-10 15:00:00', 'MOBILE_BANKING', 'Frais d''adhésion - Marie');

-- Member 6 (Marie 2) - Annual cotisation 2026 via Mobile Money (recent)
INSERT INTO transaction (id_member, id_collectivity, id_cotisation_plan, id_account, transaction_type, amount, transaction_date, payment_mode, description)
VALUES (6, 1, 1, 3, 'IN', 200000.00, '2026-04-10 15:05:00', 'MOBILE_BANKING', 'Cotisation annuelle 2026 - Marie');

-- ============================================================
-- INSERT TRANSACTIONS (OUT - Expenses)
-- ============================================================

-- Expense: Office supplies from cash
INSERT INTO transaction (id_member, id_collectivity, id_cotisation_plan, id_account, transaction_type, amount, transaction_date, payment_mode, description)
VALUES (3, 1, NULL, 1, 'OUT', 15000.00, '2026-02-01 09:00:00', NULL, 'Achat fournitures de bureau');

-- Expense: Meeting refreshments from cash
INSERT INTO transaction (id_member, id_collectivity, id_cotisation_plan, id_account, transaction_type, amount, transaction_date, payment_mode, description)
VALUES (3, 1, NULL, 1, 'OUT', 25000.00, '2026-03-05 14:00:00', NULL, 'Rafraîchissements réunion mensuelle');

-- Expense: Bank transfer fees from bank account
INSERT INTO transaction (id_member, id_collectivity, id_cotisation_plan, id_account, transaction_type, amount, transaction_date, payment_mode, description)
VALUES (3, 1, NULL, 2, 'OUT', 5000.00, '2026-03-01 10:00:00', NULL, 'Frais de virement bancaire');