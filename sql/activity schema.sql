-- ============================================
-- ACTIVITIES DATA
-- ============================================

-- Collectivity 1 Activities
INSERT INTO activity (id, id_collectivity, label, activity_type, executive_date, week_ordinal, day_of_week) VALUES
                                                                                                                ('act-1', 'col-1', 'AG1', 'MEETING', NULL, 1, 'SA'),    -- 1er samedi de chaque mois
                                                                                                                ('act-2', 'col-1', 'Formation de base', 'TRAINING', NULL, 2, 'SU');  -- 2è dimanche de chaque mois

INSERT INTO activity_member_occupation (id_activity, occupation) VALUES
                                                                     ('act-1', 'JUNIOR'), ('act-1', 'SENIOR'), ('act-1', 'SECRETARY'), ('act-1', 'TREASURER'), ('act-1', 'VICE_PRESIDENT'), ('act-1', 'PRESIDENT'),
                                                                     ('act-2', 'JUNIOR');

-- Collectivity 2 Activities
INSERT INTO activity (id, id_collectivity, label, activity_type, executive_date, week_ordinal, day_of_week) VALUES
                                                                                                                ('act-3', 'col-2', 'AG2', 'MEETING', NULL, 1, 'SU'),    -- 1er dimanche de chaque mois
                                                                                                                ('act-4', 'col-2', 'Formation de base', 'TRAINING', NULL, 3, 'SU'),  -- 3è dimanche de chaque mois
                                                                                                                ('act-5', 'col-2', 'Perfectionnement', 'OTHER', '2026-04-30', NULL, NULL);  -- Ponctuelle (spec says PUNCTUAL but enum is OTHER)

INSERT INTO activity_member_occupation (id_activity, occupation) VALUES
                                                                     ('act-3', 'JUNIOR'), ('act-3', 'SENIOR'), ('act-3', 'SECRETARY'), ('act-3', 'TREASURER'), ('act-3', 'VICE_PRESIDENT'), ('act-3', 'PRESIDENT'),
                                                                     ('act-4', 'JUNIOR'),
                                                                     ('act-5', 'SENIOR');

-- Collectivity 3 Activities
INSERT INTO activity (id, id_collectivity, label, activity_type, executive_date, week_ordinal, day_of_week) VALUES
                                                                                                                ('act-6', 'col-3', 'AG3', 'MEETING', NULL, 1, 'FR'),    -- 1er vendredi de chaque mois
                                                                                                                ('act-7', 'col-3', 'Formation de base', 'TRAINING', NULL, 4, 'WE');  -- 4è mercredi de chaque mois

INSERT INTO activity_member_occupation (id_activity, occupation) VALUES
                                                                     ('act-6', 'JUNIOR'), ('act-6', 'SENIOR'), ('act-6', 'SECRETARY'), ('act-6', 'TREASURER'), ('act-6', 'VICE_PRESIDENT'), ('act-6', 'PRESIDENT'),
                                                                     ('act-7', 'JUNIOR');

-- ============================================
-- ATTENDANCE DATA
-- ============================================

-- Col-1: act-1 AG1 - Mars 2026 (07/03/2026)
INSERT INTO activity_attendance (id, id_activity, id_member, attendance_status) VALUES
                                                                                    ('att-a1-1', 'act-1', 'C1-M1', 'ATTENDED'),
                                                                                    ('att-a1-2', 'act-1', 'C1-M2', 'ATTENDED'),
                                                                                    ('att-a1-3', 'act-1', 'C1-M3', 'ATTENDED'),
                                                                                    ('att-a1-4', 'act-1', 'C1-M4', 'ATTENDED'),
                                                                                    ('att-a1-5', 'act-1', 'C1-M5', 'ATTENDED'),
                                                                                    ('att-a1-6', 'act-1', 'C1-M6', 'ATTENDED'),
                                                                                    ('att-a1-7', 'act-1', 'C1-M7', 'MISSING'),
                                                                                    ('att-a1-8', 'act-1', 'C1-M8', 'MISSING');

-- Col-1: act-1 AG1 - Avril 2026 (04/04/2026)
INSERT INTO activity_attendance (id, id_activity, id_member, attendance_status) VALUES
                                                                                    ('att-a1-9', 'act-1', 'C1-M1', 'ATTENDED'),
                                                                                    ('att-a1-10', 'act-1', 'C1-M2', 'ATTENDED'),
                                                                                    ('att-a1-11', 'act-1', 'C1-M3', 'MISSING'),
                                                                                    ('att-a1-12', 'act-1', 'C1-M4', 'MISSING'),
                                                                                    ('att-a1-13', 'act-1', 'C1-M5', 'ATTENDED'),
                                                                                    ('att-a1-14', 'act-1', 'C1-M6', 'ATTENDED'),
                                                                                    ('att-a1-15', 'act-1', 'C1-M7', 'ATTENDED'),
                                                                                    ('att-a1-16', 'act-1', 'C1-M8', 'ATTENDED');

-- Col-2: act-3 AG2 - Mars 2026 (08/03/2026)
INSERT INTO activity_attendance (id, id_activity, id_member, attendance_status) VALUES
                                                                                    ('att-a3-1', 'act-3', 'C2-M1', 'ATTENDED'),
                                                                                    ('att-a3-2', 'act-3', 'C2-M2', 'ATTENDED'),
                                                                                    ('att-a3-3', 'act-3', 'C2-M3', 'MISSING'),
                                                                                    ('att-a3-4', 'act-3', 'C2-M4', 'MISSING'),
                                                                                    ('att-a3-5', 'act-3', 'C2-M5', 'ATTENDED'),
                                                                                    ('att-a3-6', 'act-3', 'C2-M6', 'ATTENDED'),
                                                                                    ('att-a3-7', 'act-3', 'C2-M7', 'ATTENDED'),
                                                                                    ('att-a3-8', 'act-3', 'C2-M8', 'ATTENDED');

-- Col-2: act-3 AG2 - Avril 2026 (05/04/2026)
INSERT INTO activity_attendance (id, id_activity, id_member, attendance_status) VALUES
                                                                                    ('att-a3-9', 'act-3', 'C2-M1', 'ATTENDED'),
                                                                                    ('att-a3-10', 'act-3', 'C2-M2', 'ATTENDED'),
                                                                                    ('att-a3-11', 'act-3', 'C2-M3', 'MISSING'),
                                                                                    ('att-a3-12', 'act-3', 'C2-M4', 'ATTENDED'),
                                                                                    ('att-a3-13', 'act-3', 'C2-M5', 'ATTENDED'),
                                                                                    ('att-a3-14', 'act-3', 'C2-M6', 'ATTENDED'),
                                                                                    ('att-a3-15', 'act-3', 'C2-M7', 'ATTENDED'),
                                                                                    ('att-a3-16', 'act-3', 'C2-M8', 'MISSING');

-- Col-2: act-5 Perfectionnement - 30/04/2026
INSERT INTO activity_attendance (id, id_activity, id_member, attendance_status) VALUES
                                                                                    ('att-a5-1', 'act-5', 'C2-M1', 'ATTENDED'),
                                                                                    ('att-a5-2', 'act-5', 'C2-M2', 'ATTENDED'),
                                                                                    ('att-a5-3', 'act-5', 'C2-M3', 'ATTENDED'),
                                                                                    ('att-a5-4', 'act-5', 'C2-M4', 'MISSING'),
                                                                                    ('att-a5-5', 'act-5', 'C2-M5', 'UNDEFINED'),
                                                                                    ('att-a5-6', 'act-5', 'C2-M6', 'UNDEFINED'),
                                                                                    ('att-a5-7', 'act-5', 'C2-M7', 'UNDEFINED'),
                                                                                    ('att-a5-8', 'act-5', 'C2-M8', 'UNDEFINED');

-- Col-3: act-6 AG3 - Mars 2026 (06/03/2026)
INSERT INTO activity_attendance (id, id_activity, id_member, attendance_status) VALUES
                                                                                    ('att-a6-1', 'act-6', 'C3-M1', 'ATTENDED'),
                                                                                    ('att-a6-2', 'act-6', 'C3-M2', 'ATTENDED'),
                                                                                    ('att-a6-3', 'act-6', 'C3-M3', 'ATTENDED'),
                                                                                    ('att-a6-4', 'act-6', 'C3-M4', 'ATTENDED'),
                                                                                    ('att-a6-5', 'act-6', 'C3-M5', 'ATTENDED'),
                                                                                    ('att-a6-6', 'act-6', 'C3-M6', 'ATTENDED'),
                                                                                    ('att-a6-7', 'act-6', 'C3-M7', 'MISSING'),
                                                                                    ('att-a6-8', 'act-6', 'C3-M8', 'MISSING');

-- Col-3: act-6 AG3 - Avril 2026 (03/04/2026)
INSERT INTO activity_attendance (id, id_activity, id_member, attendance_status) VALUES
                                                                                    ('att-a6-9', 'act-6', 'C3-M1', 'ATTENDED'),
                                                                                    ('att-a6-10', 'act-6', 'C3-M2', 'ATTENDED'),
                                                                                    ('att-a6-11', 'act-6', 'C3-M3', 'MISSING'),
                                                                                    ('att-a6-12', 'act-6', 'C3-M4', 'MISSING'),
                                                                                    ('att-a6-13', 'act-6', 'C3-M5', 'ATTENDED'),
                                                                                    ('att-a6-14', 'act-6', 'C3-M6', 'ATTENDED'),
                                                                                    ('att-a6-15', 'act-6', 'C3-M7', 'MISSING'),
                                                                                    ('att-a6-16', 'act-6', 'C3-M8', 'ATTENDED'),
-- External member attending col-3 activity
                                                                                    ('att-a6-17', 'act-6', 'C1-M1', 'ATTENDED');