-- Seeds for testing (use with schema v0.0.1)

-- Federation
INSERT INTO federation (id, cotisations_percentage) VALUES (1, 10.00) ON CONFLICT (id) DO NOTHING;

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

-- Senior Members (enrolled 180+ days ago, but not superuser - need collectivity to sponsor)
INSERT INTO member (id, first_name, last_name, birth_date, enrolment_date, address, email, phone_number, profession, gender)
VALUES 
(3, 'Marie', 'Randriamanantena', '1988-07-22', CURRENT_TIMESTAMP - INTERVAL '185 days', 'Lot VA 12, Antananarivo', 'marie.randria@gmail.com', '+261329876543', 'Enseignante', 'FEMALE'),
(4, 'Pierre', 'Ratsimba', '1990-01-10', CURRENT_TIMESTAMP - INTERVAL '195 days', 'Lot III B 78, Antananarivo', 'pierre.ratsimba@gmail.com', '+261334512389', 'Commercant', 'MALE'),
(5, 'Sophie', 'Rakotovao', '1992-05-18', CURRENT_TIMESTAMP - INTERVAL '180 days', 'Lot I C 23, Antananarivo', 'sophie.rakotovao@gmail.com', '+261345678912', 'Agricultrice', 'FEMALE')
ON CONFLICT (id) DO NOTHING;