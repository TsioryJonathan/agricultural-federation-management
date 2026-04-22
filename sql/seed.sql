-- Seeds for testing (use with schema v0.0.3)

-- Federation
INSERT INTO federation (id, cotisation_percentage) VALUES (1, 10.00) ON CONFLICT (id) DO NOTHING;

-- Superuser 1 (can sponsor anyone without being in a collectivity)
INSERT INTO member (id, first_name, last_name, birth_date, enrolment_date, address, email, phone_number, profession, gender, superuser)
VALUES (
    1,
    'Rasoa',
    'Andrianaivo',
    '1980-05-15',
    CURRENT_TIMESTAMP - INTERVAL '200 days',
    'Lot I B 10, Analakely, Antananarivo',
    'andrianaivo.rasoa@gmail.com',
    '+261341000001',
    'President de la Federation',
    'MALE',
    true
) ON CONFLICT (id) DO NOTHING;

-- Superuser 2 (can sponsor anyone without being in a collectivity)
INSERT INTO member (id, first_name, last_name, birth_date, enrolment_date, address, email, phone_number, profession, gender, superuser)
VALUES (
    2,
    'Jean',
    'Rabenrasoa',
    '1985-03-15',
    CURRENT_TIMESTAMP - INTERVAL '200 days',
    'Lot II B 45, Antananarivo',
    'jean.rabenrasoa@gmail.com',
    '+261341234567',
    'Agriculteur',
    'MALE',
    true
) ON CONFLICT (id) DO NOTHING;

-- Senior Members (enrolled 180+ days ago, can be in collectivity structure)
INSERT INTO member (id, first_name, last_name, birth_date, enrolment_date, address, email, phone_number, profession, gender)
VALUES
(3, 'Marie', 'Randriamanantena', '1988-07-22', CURRENT_TIMESTAMP - INTERVAL '185 days', 'Lot VA 12, Antananarivo', 'marie.randria@gmail.com', '+261329876543', 'Enseignante', 'FEMALE'),
(4, 'Pierre', 'Ratsimba', '1990-01-10', CURRENT_TIMESTAMP - INTERVAL '195 days', 'Lot III B 78, Antananarivo', 'pierre.ratsimba@gmail.com', '+261334512389', 'Commercant', 'MALE'),
(5, 'Sophie', 'Rakotovao', '1992-05-18', CURRENT_TIMESTAMP - INTERVAL '200 days', 'Lot I C 23, Antananarivo', 'sophie.rakotovao@gmail.com', '+261345678912', 'Agricultrice', 'FEMALE')
ON CONFLICT (id) DO NOTHING;

-- Junior Members (enrolled < 180 days ago, cannot be in collectivity structure)
INSERT INTO member (id, first_name, last_name, birth_date, enrolment_date, address, email, phone_number, profession, gender)
VALUES
(6, 'Lucas', 'Ramanantsoa', '1995-03-01', CURRENT_TIMESTAMP - INTERVAL '90 days', 'Lot IV A 34, Antananarivo', 'lucas.ramanantsoa@gmail.com', '+261331234567', 'Etudiant', 'MALE'),
(7, 'Emma', 'Rakotozafy', '1998-08-15', CURRENT_TIMESTAMP - INTERVAL '60 days', 'Lot V B 56, Antananarivo', 'emma.rakotozafy@gmail.com', '+261338765432', 'Agricultrice', 'FEMALE'),
(8, 'Thomas', 'Rakotoniaina', '1993-11-22', CURRENT_TIMESTAMP - INTERVAL '120 days', 'Lot VI A 78, Antananarivo', 'thomas.rakotoniaina@gmail.com', '+261337654321', 'Technicien', 'MALE'),
(9, 'Julie', 'Rasoa', '1996-04-05', CURRENT_TIMESTAMP - INTERVAL '45 days', 'Lot VII B 12, Antananarivo', 'julie.rasoa@gmail.com', '+261336543210', 'Secretaire', 'FEMALE'),
(10, 'Marc', 'Ratsiantsoa', '1991-09-18', CURRENT_TIMESTAMP - INTERVAL '150 days', 'Lot VIII A 45, Antananarivo', 'marc.ratsiantsoa@gmail.com', '+261335432109', 'Chauffeur', 'MALE'),
(11, 'Camille', 'Andrianaivo', '1994-12-30', CURRENT_TIMESTAMP - INTERVAL '30 days', 'Lot IX B 67, Antananarivo', 'camille.andrianaivo@gmail.com', '+261334321098', 'Agricultrice', 'FEMALE'),
(12, 'Paul', 'Rakotoarivelo', '1997-06-12', CURRENT_TIMESTAMP - INTERVAL '160 days', 'Lot X A 89, Antananarivo', 'paul.rakotoarivelo@gmail.com', '+261333210987', 'Artisan', 'MALE'),
(13, 'Anna', 'Rasoloarimanana', '1999-01-25', CURRENT_TIMESTAMP - INTERVAL '15 days', 'Lot XI B 23, Antananarivo', 'anna.rasoloarimanana@gmail.com', '+261332109876', 'Etudiante', 'FEMALE')
ON CONFLICT (id) DO NOTHING;

-- First Collectivity in First Federation (Antananarivo)
INSERT INTO collectivity (id, number, name, speciality, creation_datetime, federation_approval, authorization_date, id_federation, location)
VALUES (
    1,
    'COL-ANTANANARIVO',
    'Collectivite d''Antananarivo',
    'Agriculture',
    CURRENT_TIMESTAMP,
    true,
    CURRENT_TIMESTAMP,
    1,
    'Antananarivo'
) ON CONFLICT (id) DO NOTHING;

-- Link members to collectivity (structure: President, Vice President, Treasurer, Secretary + Seniors + Juniors)
DELETE FROM member_collectivity WHERE id_collectivity = 1;

INSERT INTO member_collectivity (id_member, id_collectivity, occupation, start_date)
VALUES
(1, 1, 'PRESIDENT', CURRENT_TIMESTAMP),
(2, 1, 'VICE_PRESIDENT', CURRENT_TIMESTAMP),
(3, 1, 'TREASURER', CURRENT_TIMESTAMP),
(4, 1, 'SECRETARY', CURRENT_TIMESTAMP),
(5, 1, 'SENIOR', CURRENT_TIMESTAMP),
(6, 1, 'JUNIOR', CURRENT_TIMESTAMP),
(7, 1, 'JUNIOR', CURRENT_TIMESTAMP),
(8, 1, 'SENIOR', CURRENT_TIMESTAMP),
(9, 1, 'JUNIOR', CURRENT_TIMESTAMP),
(10, 1, 'SENIOR', CURRENT_TIMESTAMP),
(11, 1, 'JUNIOR', CURRENT_TIMESTAMP),
(12, 1, 'SENIOR', CURRENT_TIMESTAMP),
(13, 1, 'JUNIOR', CURRENT_TIMESTAMP);