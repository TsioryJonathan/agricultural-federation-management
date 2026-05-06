const chai = require('chai');
const chaiHttp = require('chai-http');
const expect = chai.expect;

chai.use(chaiHttp);

const BASE_URL = process.env.SERVER_URL || 'http://localhost:8080';

describe('Agricultural Federation API - Complete Test Suite', function() {
    this.timeout(10000);

    let createdMemberIds = [];
    let createdCollectivityId = null;
    let createdActivityId = null;
    let createdMembershipFeeId = null;

    // ============================================
    // 1. MEMBER CREATION TESTS
    // ============================================
    describe('POST /members - Member Creation', function() {

        it('should create a member with valid data, 2 referees, registration paid, membership dues paid', async function() {
            const res = await chai.request(BASE_URL)
                .post('/members')
                .send([{
                    firstName: 'Jean',
                    lastName: 'Dupont',
                    birthDate: '1990-05-15',
                    gender: 'MALE',
                    address: 'Lot 123 Antananarivo',
                    profession: 'Agriculteur',
                    phoneNumber: 341234567,
                    email: 'jean.dupont@test.mg',
                    occupation: 'JUNIOR',
                    collectivityIdentifier: 'col-1',
                    referees: ['C1-M1', 'C1-M2'],
                    registrationFeePaid: true,
                    membershipDuesPaid: true
                }]);
            console.log(res.error)

            expect(res.status).to.equal(201);
            expect(res.body).to.be.an('array');
            expect(res.body[0]).to.have.property('id');
            expect(res.body[0].firstName).to.equal('Jean');
            expect(res.body[0].lastName).to.equal('Dupont');
            expect(res.body[0].gender).to.equal('MALE');
            expect(res.body[0].email).to.equal('jean.dupont@test.mg');
            expect(res.body[0].referees).to.be.an('array');
            expect(res.body[0].referees.length).to.be.at.least(2);
            createdMemberIds.push(res.body[0].id);
        });

        it('should reject member without registration fee paid', async function() {
            const res = await chai.request(BASE_URL)
                .post('/members')
                .send([{
                    firstName: 'Pierre',
                    lastName: 'Martin',
                    birthDate: '1988-03-20',
                    gender: 'MALE',
                    address: 'Lot 456 Toamasina',
                    profession: 'Riziculteur',
                    phoneNumber: 321234567,
                    email: 'pierre.martin@test.mg',
                    occupation: 'JUNIOR',
                    collectivityIdentifier: 'col-1',
                    referees: ['C1-M1', 'C1-M2'],
                    registrationFeePaid: false,
                    membershipDuesPaid: true
                }]);

            expect(res.status).to.equal(400);
            expect(res.text).to.include('Registration fee');
        });

        it('should reject member without membership dues paid', async function() {
            const res = await chai.request(BASE_URL)
                .post('/members')
                .send([{
                    firstName: 'Marie',
                    lastName: 'Bernard',
                    birthDate: '1992-07-10',
                    gender: 'FEMALE',
                    address: 'Lot 789 Antsirabe',
                    profession: 'Apiculteur',
                    phoneNumber: 331234567,
                    email: 'marie.bernard@test.mg',
                    occupation: 'JUNIOR',
                    collectivityIdentifier: 'col-1',
                    referees: ['C1-M1', 'C1-M2'],
                    registrationFeePaid: true,
                    membershipDuesPaid: false
                }]);

            expect(res.status).to.equal(400);
            expect(res.text).to.include('Membership dues');
        });

        it('should reject member with less than 2 referees', async function() {
            const res = await chai.request(BASE_URL)
                .post('/members')
                .send([{
                    firstName: 'Paul',
                    lastName: 'Petit',
                    birthDate: '1995-11-25',
                    gender: 'MALE',
                    address: 'Lot 101 Fianarantsoa',
                    profession: 'Collecteur',
                    phoneNumber: 341234568,
                    email: 'paul.petit@test.mg',
                    occupation: 'JUNIOR',
                    collectivityIdentifier: 'col-1',
                    referees: ['C1-M1'],
                    registrationFeePaid: true,
                    membershipDuesPaid: true
                }]);

            expect(res.status).to.equal(400);
        });

        it('should reject member with non-existent referees', async function() {
            const res = await chai.request(BASE_URL)
                .post('/members')
                .send([{
                    firstName: 'Sophie',
                    lastName: 'Moreau',
                    birthDate: '1993-09-05',
                    gender: 'FEMALE',
                    address: 'Lot 202 Mahajanga',
                    profession: 'Distributeur',
                    phoneNumber: 351234567,
                    email: 'sophie.moreau@test.mg',
                    occupation: 'JUNIOR',
                    collectivityIdentifier: 'col-1',
                    referees: ['NONEXISTENT-1', 'NONEXISTENT-2'],
                    registrationFeePaid: true,
                    membershipDuesPaid: true
                }]);

            expect(res.status).to.equal(404);
        });

        it('should reject member with referees from other collectivities only (spec rule violation)', async function() {
            const res = await chai.request(BASE_URL)
                .post('/members')
                .send([{
                    firstName: 'Luc',
                    lastName: 'Dumas',
                    birthDate: '1991-04-18',
                    gender: 'MALE',
                    address: 'Lot 333 Toliara',
                    profession: 'Riziculteur',
                    phoneNumber: 361234567,
                    email: 'luc.dumas@test.mg',
                    occupation: 'JUNIOR',
                    collectivityIdentifier: 'col-1',
                    referees: ['C2-M5', 'C2-M6'],
                    registrationFeePaid: true,
                    membershipDuesPaid: true
                }]);
            expect(res.status).to.equal(400);
        });

        it('should reject member with missing required fields', async function() {
            const res = await chai.request(BASE_URL)
                .post('/members')
                .send([{
                    firstName: 'Test',
                    lastName: 'Incomplet',
                    collectivityIdentifier: 'col-1',
                    referees: ['C1-M1', 'C1-M2'],
                    registrationFeePaid: true,
                    membershipDuesPaid: true
                }]);

            expect(res.status).to.equal(400);
        });
    });

    // ============================================
    // 2. MEMBER PAYMENT TESTS
    // ============================================
    describe('POST /members/{id}/payments - Member Payments', function() {

        it('should create a payment for an existing member', async function() {
            const memberId = createdMemberIds[0];
            const res = await chai.request(BASE_URL)
                .post(`/members/${memberId}/payments`)
                .send([{
                    amount: 50000,
                    membershipFeeIdentifier: 'cot-1',
                    accountCreditedIdentifier: 'C1-A-CASH',
                    paymentMode: 'CASH'
                }]);

            expect(res.status).to.equal(201);
            expect(res.body).to.be.an('array');
            expect(res.body[0]).to.have.property('id');
            expect(res.body[0]).to.have.property('amount');
            expect(res.body[0].amount).to.equal(50000);
            expect(res.body[0].paymentMode).to.equal('CASH');
            expect(res.body[0]).to.have.property('creationDate');
        });

        it('should reject payment for non-existent member', async function() {
            const res = await chai.request(BASE_URL)
                .post('/members/NONEXISTENT/payments')
                .send([{
                    amount: 50000,
                    paymentMode: 'CASH'
                }]);

            expect(res.status).to.equal(404);
        });

        it('should reject payment with negative amount', async function() {
            const memberId = createdMemberIds[0];
            const res = await chai.request(BASE_URL)
                .post(`/members/${memberId}/payments`)
                .send([{
                    amount: -1000,
                    paymentMode: 'CASH'
                }]);

            expect(res.status).to.equal(400);
        });

        it('should reject payment with zero amount', async function() {
            const memberId = createdMemberIds[0];
            const res = await chai.request(BASE_URL)
                .post(`/members/${memberId}/payments`)
                .send([{
                    amount: 0,
                    paymentMode: 'CASH'
                }]);

            expect(res.status).to.equal(400);
        });

        it('should reject payment with invalid payment mode', async function() {
            const memberId = createdMemberIds[0];
            const res = await chai.request(BASE_URL)
                .post(`/members/${memberId}/payments`)
                .send([{
                    amount: 50000,
                    paymentMode: 'INVALID_MODE'
                }]);

            expect(res.status).to.equal(400);
        });
    });

    // ============================================
    // 3. COLLECTIVITY CREATION TESTS
    // ============================================
    describe('POST /collectivities - Collectivity Creation', function() {

        it('should reject collectivity without federation approval', async function() {
            const res = await chai.request(BASE_URL)
                .post('/collectivities')
                .send([{
                    location: 'Antananarivo',
                    members: ['C1-M1', 'C1-M2', 'C1-M3', 'C1-M4', 'C1-M5', 'C1-M6', 'C1-M7', 'C1-M8',
                        createdMemberIds[0] || 'C1-M1', 'C3-M1'],
                    federationApproval: false,
                    structure: {
                        president: 'C1-M1',
                        vicePresident: 'C1-M2',
                        treasurer: 'C1-M4',
                        secretary: 'C1-M3'
                    }
                }]);

            expect(res.status).to.equal(400);
            expect(res.text).to.include('federation approval');
        });

        it('should reject collectivity with less than 10 members', async function() {
            const res = await chai.request(BASE_URL)
                .post('/collectivities')
                .send([{
                    location: 'Antananarivo',
                    members: ['C1-M1', 'C1-M2', 'C1-M3'],
                    federationApproval: true,
                    structure: {
                        president: 'C1-M1',
                        vicePresident: 'C1-M2',
                        treasurer: 'C1-M1',
                        secretary: 'C1-M1'
                    }
                }]);

            expect(res.status).to.equal(400);
        });

        it('should reject collectivity with duplicate roles', async function() {
            const res = await chai.request(BASE_URL)
                .post('/collectivities')
                .send([{
                    location: 'Antananarivo',
                    members: ['C1-M1', 'C1-M2', 'C1-M3', 'C1-M4', 'C1-M5', 'C1-M6', 'C1-M7', 'C1-M8',
                        createdMemberIds[0] || 'C1-M1', 'C3-M1'],
                    federationApproval: true,
                    structure: {
                        president: 'C1-M1',
                        vicePresident: 'C1-M1',
                        treasurer: 'C1-M1',
                        secretary: 'C1-M1'
                    }
                }]);

            expect(res.status).to.equal(400);
        });

        it('should reject collectivity with non-member in structure', async function() {
            const res = await chai.request(BASE_URL)
                .post('/collectivities')
                .send([{
                    location: 'Antananarivo',
                    members: ['C1-M1', 'C1-M2', 'C1-M3', 'C1-M4', 'C1-M5', 'C1-M6', 'C1-M7', 'C1-M8',
                        'C1-M1', 'C1-M1'],
                    federationApproval: true,
                    structure: {
                        president: 'C3-M1',
                        vicePresident: 'C1-M2',
                        treasurer: 'C1-M4',
                        secretary: 'C1-M3'
                    }
                }]);

            expect(res.status).to.equal(400);
        });
    });

    // ============================================
    // 4. GET COLLECTIVITY TESTS
    // ============================================
    describe('GET /collectivities/{id} - Get Collectivity', function() {

        it('should retrieve collectivity col-1 with members', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1');

            expect(res.status).to.equal(200);
            expect(res.body).to.have.property('id', 'col-1');
            expect(res.body).to.have.property('name');
            expect(res.body).to.have.property('number');
            expect(res.body).to.have.property('location', 'Ambatondrazaka');
            expect(res.body).to.have.property('structure');
            expect(res.body.structure).to.have.property('president');
            expect(res.body.structure).to.have.property('vicePresident');
            expect(res.body.structure).to.have.property('treasurer');
            expect(res.body.structure).to.have.property('secretary');
            expect(res.body).to.have.property('members');
            expect(res.body.members).to.be.an('array');
            expect(res.body.members.length).to.be.at.least(8);
        });

        it('should return 404 for non-existent collectivity', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/NONEXISTENT');

            expect(res.status).to.equal(404);
        });
    });

    // ============================================
    // 5. COLLECTIVITY INFORMATION TESTS
    // ============================================
    describe('PUT /collectivities/{id}/informations - Assign Information', function() {

        it('should retrieve a collectivity that already has name and number', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1');

            expect(res.status).to.equal(200);
            expect(res.body).to.have.property('name');
            expect(res.body).to.have.property('number');
            expect(res.body.name).to.not.be.empty;
            expect(res.body.number).to.not.be.empty;
        });

        it('should reject updating already assigned collectivity info', async function() {
            const res = await chai.request(BASE_URL)
                .put('/collectivities/col-1/informations')
                .send({
                    number: '999',
                    name: 'New Name'
                });

            expect(res.status).to.equal(400);
        });

        it('should reject empty name', async function() {
            const res = await chai.request(BASE_URL)
                .put('/collectivities/col-1/informations')
                .send({
                    number: '123',
                    name: ''
                });

            expect(res.status).to.equal(400);
        });

        it('should reject empty number', async function() {
            const res = await chai.request(BASE_URL)
                .put('/collectivities/col-1/informations')
                .send({
                    number: '',
                    name: 'Test'
                });

            expect(res.status).to.equal(400);
        });

        it('should reject duplicate name', async function() {
            const res = await chai.request(BASE_URL)
                .put('/collectivities/col-1/informations')
                .send({
                    number: '999',
                    name: 'Dobo voalahany'
                });

            expect(res.status).to.equal(400);
        });

        it('should reject duplicate number', async function() {
            const res = await chai.request(BASE_URL)
                .put('/collectivities/col-1/informations')
                .send({
                    number: '2',
                    name: 'Unique Name Test'
                });

            expect(res.status).to.equal(400);
        });
    });

    // ============================================
    // 6. MEMBERSHIP FEES TESTS
    // ============================================
    describe('GET /collectivities/{id}/membershipFees - Get Membership Fees', function() {

        it('should retrieve membership fees for col-1', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/membershipFees');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            expect(res.body.length).to.be.at.least(1);
            expect(res.body[0]).to.have.property('id');
            expect(res.body[0]).to.have.property('label');
            expect(res.body[0]).to.have.property('amount');
            expect(res.body[0]).to.have.property('frequency');
            expect(res.body[0]).to.have.property('status');
            expect(res.body[0]).to.have.property('eligibleFrom');
        });

        it('should return 404 for non-existent collectivity', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/NONEXISTENT/membershipFees');

            expect(res.status).to.equal(404);
        });
    });

    describe('POST /collectivities/{id}/membershipFees - Create Membership Fees', function() {

        it('should create a membership fee for col-1', async function() {
            const res = await chai.request(BASE_URL)
                .post('/collectivities/col-1/membershipFees')
                .send([{
                    label: 'Cotisation test',
                    frequency: 'MONTHLY',
                    amount: 25000,
                    eligibleFrom: '2026-05-01'
                }]);
            console.log(res.error)
            expect(res.status).to.equal(201);
            expect(res.body).to.be.an('array');
            expect(res.body[0]).to.have.property('id');
            expect(res.body[0].label).to.equal('Cotisation test');
            expect(res.body[0].frequency).to.equal('MONTHLY');
            expect(res.body[0].amount).to.equal(25000);
            expect(res.body[0].status).to.equal('ACTIVE');
            createdMembershipFeeId = res.body[0].id;
        });

        it('should reject membership fee with negative amount', async function() {
            const res = await chai.request(BASE_URL)
                .post('/collectivities/col-1/membershipFees')
                .send([{
                    label: 'Invalid Fee',
                    frequency: 'MONTHLY',
                    amount: -1000,
                    eligibleFrom: '2026-05-01'
                }]);
        expect(res.status).to.equal(400);
        });

        it('should reject membership fee without frequency', async function() {
            const res = await chai.request(BASE_URL)
                .post('/collectivities/col-1/membershipFees')
                .send([{
                    label: 'Invalid Fee',
                    amount: 10000,
                    eligibleFrom: '2026-05-01'
                }]);

            expect(res.status).to.equal(400);
        });

        it('should reject membership fee with invalid frequency', async function() {
            const res = await chai.request(BASE_URL)
                .post('/collectivities/col-1/membershipFees')
                .send([{
                    label: 'Invalid Fee',
                    frequency: 'DAILY',
                    amount: 10000,
                    eligibleFrom: '2026-05-01'
                }]);

            expect(res.status).to.equal(400);
        });
    });

    // ============================================
    // 7. FINANCIAL ACCOUNTS TESTS
    // ============================================
    describe('GET /collectivities/{id}/financialAccounts - Financial Accounts', function() {

        it('should retrieve financial accounts for col-1', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/financialAccounts');

            expect(res.status).to.equal(200);
            expect(res.body).to.have.property('accounts');
            expect(res.body.accounts).to.be.an('array');
            expect(res.body.accounts.length).to.be.at.least(1);
            expect(res.body).to.have.property('amount');
            expect(res.body).to.have.property('id');
            expect(res.body.id).to.equal('col-1');
        });

        it('should retrieve financial accounts at specific date', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/financialAccounts?at=2026-01-02');

            expect(res.status).to.equal(200);
            expect(res.body).to.have.property('accounts');
            expect(res.body.accounts).to.be.an('array');
            expect(res.body).to.have.property('amount');
            expect(res.body).to.have.property('id');
        });

        it('should return 404 for non-existent collectivity', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/NONEXISTENT/financialAccounts');

            expect(res.status).to.equal(404);
        });
    });

    // ============================================
    // 8. TRANSACTIONS TESTS
    // ============================================
    describe('GET /collectivities/{id}/transactions - Transactions', function() {

        it('should retrieve transactions for col-1 in date range', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/transactions?from=2026-01-01&to=2026-12-31');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            expect(res.body.length).to.be.at.least(1);
            expect(res.body[0]).to.have.property('id');
            expect(res.body[0]).to.have.property('amount');
            expect(res.body[0]).to.have.property('creationDate');
            expect(res.body[0]).to.have.property('paymentMode');
            expect(res.body[0]).to.have.property('accountCredited');
            expect(res.body[0]).to.have.property('memberDebited');
        });

        it('should return empty list for col-3 (no payments in PDF)', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-3/transactions?from=2026-01-01&to=2026-12-31');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            expect(res.body.length).to.equal(0);
        });

        it('should return 400 without mandatory query params', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/transactions');

            expect(res.status).to.equal(400);
        });

        it('should return 400 when from is after to', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/transactions?from=2026-12-31&to=2026-01-01');

            expect(res.status).to.equal(400);
        });

        it('should return 404 for non-existent collectivity', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/NONEXISTENT/transactions?from=2026-01-01&to=2026-12-31');

            expect(res.status).to.equal(404);
        });
    });

    // ============================================
    // 9. STATISTICS TESTS
    // ============================================
    describe('GET /collectivities/{id}/statistics - Local Statistics', function() {

        it('should retrieve local statistics for col-1', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/statistics?from=2026-01-01&to=2026-12-31');
            console.log(res.error)
            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            expect(res.body.length).to.be.at.least(1);
            expect(res.body[0]).to.have.property('memberDescription');
            expect(res.body[0].memberDescription).to.have.property('id');
            expect(res.body[0].memberDescription).to.have.property('firstName');
            expect(res.body[0].memberDescription).to.have.property('lastName');
            expect(res.body[0].memberDescription).to.have.property('email');
            expect(res.body[0]).to.have.property('earnedAmount');
            expect(res.body[0]).to.have.property('unpaidAmount');
            expect(res.body[0]).to.have.property('assiduityPercentage');
        });

        it('should return empty local statistics for date range with no transactions', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/statistics?from=2020-01-01&to=2020-12-31');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            res.body.forEach(stat => {
                expect(stat.earnedAmount).to.equal(0);
            });
        });

        it('should return 400 without mandatory query params', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/statistics');

            expect(res.status).to.equal(400);
        });

        it('should return 404 for non-existent collectivity', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivites/NONEXISTENT/statistics?from=2026-01-01&to=2026-12-31');

            expect(res.status).to.equal(404);
        });
    });

    describe('GET /collectivites/statistics - Overall Statistics', function() {

        it('should retrieve overall statistics', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/statistics?from=2026-01-01&to=2026-12-31');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            expect(res.body.length).to.be.at.least(1);
            expect(res.body[0]).to.have.property('collectivityInformation');
            expect(res.body[0].collectivityInformation).to.have.property('name');
            expect(res.body[0].collectivityInformation).to.have.property('number');
            expect(res.body[0]).to.have.property('newMembersNumber');
            expect(res.body[0]).to.have.property('overallMemberCurrentDuePercentage');
            expect(res.body[0]).to.have.property('overallMemberAssiduityPercentage');
        });

        it('should return 400 without mandatory query params', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/statistics');

            expect(res.status).to.equal(400);
        });

        it('should return 400 when from is after to', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/statistics?from=2026-12-31&to=2026-01-01');

            expect(res.status).to.equal(400);
        });
    });

    // ============================================
    // 10. ACTIVITIES TESTS
    // ============================================
    describe('POST /collectivities/{id}/activities - Create Activities', function() {

        it('should create an activity with executive date', async function() {
            const res = await chai.request(BASE_URL)
                .post('/collectivities/col-1/activities')
                .send([{
                    label: 'Assemblée générale mensuelle',
                    activityType: 'MEETING',
                    memberOccupationConcerned: ['PRESIDENT', 'VICE_PRESIDENT', 'SECRETARY', 'TREASURER', 'SENIOR', 'JUNIOR'],
                    executiveDate: '2026-06-15'
                }]);

            expect(res.status).to.equal(201);
            expect(res.body).to.be.an('array');
            expect(res.body[0]).to.have.property('id');
            expect(res.body[0].label).to.equal('Assemblée générale mensuelle');
            expect(res.body[0].activityType).to.equal('MEETING');
            expect(res.body[0].executiveDate).to.equal('2026-06-15');
            createdActivityId = res.body[0].id;
        });

        it('should create an activity with recurrence rule', async function() {
            const res = await chai.request(BASE_URL)
                .post('/collectivities/col-1/activities')
                .send([{
                    label: 'Formation obligatoire juniors',
                    activityType: 'TRAINING',
                    memberOccupationConcerned: ['JUNIOR'],
                    recurrenceRule: {
                        weekOrdinal: 4,
                        dayOfWeek: 'SA'
                    }
                }]);

            expect(res.status).to.equal(201);
            expect(res.body).to.be.an('array');
            expect(res.body[0]).to.have.property('id');
            expect(res.body[0].label).to.equal('Formation obligatoire juniors');
            expect(res.body[0].activityType).to.equal('TRAINING');
            expect(res.body[0].recurrenceRule).to.have.property('weekOrdinal', 4);
            expect(res.body[0].recurrenceRule).to.have.property('dayOfWeek', 'SA');
        });

        it('should reject activity with both recurrence rule and executive date', async function() {
            const res = await chai.request(BASE_URL)
                .post('/collectivities/col-1/activities')
                .send([{
                    label: 'Invalid Activity',
                    activityType: 'MEETING',
                    memberOccupationConcerned: ['SENIOR'],
                    recurrenceRule: {
                        weekOrdinal: 2,
                        dayOfWeek: 'SU'
                    },
                    executiveDate: '2026-06-15'
                }]);

            expect(res.status).to.equal(400);
        });

        it('should reject activity with neither recurrence rule nor executive date', async function() {
            const res = await chai.request(BASE_URL)
                .post('/collectivities/col-1/activities')
                .send([{
                    label: 'Invalid Activity',
                    activityType: 'MEETING',
                    memberOccupationConcerned: ['SENIOR']
                }]);

            expect(res.status).to.equal(400);
        });

        it('should return 404 for non-existent collectivity', async function() {
            const res = await chai.request(BASE_URL)
                .post('/collectivities/NONEXISTENT/activities')
                .send([{
                    label: 'Test Activity',
                    activityType: 'MEETING',
                    executiveDate: '2026-06-15'
                }]);

            expect(res.status).to.equal(404);
        });
    });

    describe('GET /collectivities/{id}/activities - Get Activities', function() {

        it('should retrieve activities for col-1', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/activities');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            expect(res.body.length).to.be.at.least(1);
            expect(res.body[0]).to.have.property('id');
            expect(res.body[0]).to.have.property('label');
            expect(res.body[0]).to.have.property('activityType');
        });

        it('should return 404 for non-existent collectivity', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/NONEXISTENT/activities');

            expect(res.status).to.equal(404);
        });
    });

    // ============================================
    // 11. ATTENDANCE TESTS
    // ============================================
    describe('POST /collectivities/{id}/activities/{activityId}/attendance - Create Attendance', function() {

        it('should record attendance for an activity', async function() {
            if (!createdActivityId) {
                this.skip();
                return;
            }

            const res = await chai.request(BASE_URL)
                .post(`/collectivities/col-1/activities/${createdActivityId}/attendance`)
                .send([
                    { memberIdentifier: 'C1-M1', attendanceStatus: 'ATTENDED' },
                    { memberIdentifier: 'C1-M2', attendanceStatus: 'ATTENDED' },
                    { memberIdentifier: 'C1-M3', attendanceStatus: 'MISSING' }
                ]);

            console.log(res.error)
            expect(res.status).to.equal(201);
            expect(res.body).to.be.an('array');
            expect(res.body.length).to.equal(3);
            expect(res.body[0]).to.have.property('id');
            expect(res.body[0]).to.have.property('memberDescription');
            expect(res.body[0]).to.have.property('attendanceStatus');
        });

        it('should reject attendance for non-existent member', async function() {
            if (!createdActivityId) {
                this.skip();
                return;
            }

            const res = await chai.request(BASE_URL)
                .post(`/collectivities/col-1/activities/${createdActivityId}/attendance`)
                .send([
                    { memberIdentifier: 'NONEXISTENT', attendanceStatus: 'ATTENDED' }
                ]);

            expect(res.status).to.equal(404);
        });

        it('should reject modifying already confirmed attendance', async function() {
            if (!createdActivityId) {
                this.skip();
                return;
            }

            // Try to modify C1-M1 from ATTENDED to MISSING
            const res = await chai.request(BASE_URL)
                .post(`/collectivities/col-1/activities/${createdActivityId}/attendance`)
                .send([
                    { memberIdentifier: 'C1-M1', attendanceStatus: 'MISSING' }
                ]);
            console.log(res.error)
            expect(res.status).to.equal(400);
        });

        it('should return 404 for non-existent activity', async function() {
            const res = await chai.request(BASE_URL)
                .post('/collectivities/col-1/activities/NONEXISTENT/attendance')
                .send([
                    { memberIdentifier: 'C1-M1', attendanceStatus: 'ATTENDED' }
                ]);

            expect(res.status).to.equal(404);
        });
    });

    describe('GET /collectivities/{id}/activities/{activityId}/attendance - Get Attendance', function() {

        it('should retrieve attendance for an activity', async function() {
            if (!createdActivityId) {
                this.skip();
                return;
            }

            const res = await chai.request(BASE_URL)
                .get(`/collectivities/col-1/activities/${createdActivityId}/attendance`);

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            res.body.forEach(attendance => {
                expect(attendance).to.have.property('id');
                expect(attendance).to.have.property('memberDescription');
                expect(attendance).to.have.property('attendanceStatus');
                expect(['ATTENDED', 'MISSING', 'UNDEFINED']).to.include(attendance.attendanceStatus);
            });
        });

        it('should return 404 for non-existent activity', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/activities/NONEXISTENT/attendance');

            expect(res.status).to.equal(404);
        });
    });

    // ============================================
    // 12. DATA INTEGRITY TESTS
    // ============================================
    describe('Data Integrity Tests', function() {

        it('should verify col-1 has 8 members from PDF data', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1');

            expect(res.status).to.equal(200);
            expect(res.body.members).to.be.an('array');
            expect(res.body.members.length).to.be.at.least(8);
        });

        it('should verify col-2 has members from PDF data', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-2');

            expect(res.status).to.equal(200);
            expect(res.body.members).to.be.an('array');
            expect(res.body.members.length).to.be.at.least(8);
        });

        it('should verify col-3 has members from PDF data', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-3');

            expect(res.status).to.equal(200);
            expect(res.body.members).to.be.an('array');
            expect(res.body.members.length).to.be.at.least(8);
        });

        it('should verify member C1-M1 exists and has correct data', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1');

            const member = res.body.members.find(m => m.id === 'C1-M1');
            expect(member).to.exist;
            expect(member.firstName).to.equal('Nom membre 1');
            expect(member.lastName).to.equal('Prénom membre 1');
            expect(member.gender).to.equal('MALE');
            expect(member.email).to.equal('member.1@fed-agri.mg');
            expect(member.profession).to.equal('Riziculteur');
        });

        it('should verify member C1-M3 has 2 referees from PDF', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1');

            const member = res.body.members.find(m => m.id === 'C1-M3');
            expect(member).to.exist;
            expect(member.referees).to.be.an('array');
            expect(member.referees.length).to.equal(2);
            const refereeIds = member.referees.map(r => r.id);
            expect(refereeIds).to.include('C1-M1');
            expect(refereeIds).to.include('C1-M2');
        });

        it('should verify member C1-M8 has referees C1-M6 and C1-M7', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1');

            const member = res.body.members.find(m => m.id === 'C1-M8');
            expect(member).to.exist;
            expect(member.referees).to.be.an('array');
            expect(member.referees.length).to.equal(2);
            const refereeIds = member.referees.map(r => r.id);
            expect(refereeIds).to.include('C1-M6');
            expect(refereeIds).to.include('C1-M7');
        });

        it('should verify col-1 structure has correct members', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1');

            expect(res.body.structure.president.id).to.equal('C1-M1');
            expect(res.body.structure.vicePresident.id).to.equal('C1-M2');
            expect(res.body.structure.secretary.id).to.equal('C1-M3');
            expect(res.body.structure.treasurer.id).to.equal('C1-M4');
        });

        it('should verify col-1 transactions match PDF data', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/transactions?from=2026-01-01&to=2026-12-31');

            expect(res.status).to.equal(200);
            const txC1M7 = res.body.find(t => t.memberDebited.id === 'C1-M7');
            expect(txC1M7).to.exist;
            expect(txC1M7.amount).to.equal(60000);

            const txC1M8 = res.body.find(t => t.memberDebited.id === 'C1-M8');
            expect(txC1M8).to.exist;
            expect(txC1M8.amount).to.equal(90000);
        });

        it('should verify col-3 has no transactions (as per PDF)', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-3/transactions?from=2026-01-01&to=2026-12-31');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            expect(res.body.length).to.equal(0);
        });

        it('should verify cotisation amounts match PDF', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/membershipFees');

            const cot1 = res.body.find(c => c.id === 'cot-1');
            expect(cot1).to.exist;
            expect(cot1.amount).to.equal(100000);
            expect(cot1.frequency).to.equal('ANNUALLY');
            expect(cot1.status).to.equal('ACTIVE');
        });

        it('should verify col-3 cotisation is 50000 as per PDF', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-3/membershipFees');

            const cot3 = res.body.find(c => c.id === 'cot-3');
            expect(cot3).to.exist;
            expect(cot3.amount).to.equal(50000);
        });

        it('should verify collectivity names match PDF', async function() {
            const col1 = await chai.request(BASE_URL).get('/collectivities/col-1');
            expect(col1.body.name).to.equal('Mpanorina');
            expect(col1.body.location).to.equal('Ambatondrazaka');

            const col2 = await chai.request(BASE_URL).get('/collectivities/col-2');
            expect(col2.body.name).to.equal('Dobo voalahany');
            expect(col2.body.location).to.equal('Ambatondrazaka');

            const col3 = await chai.request(BASE_URL).get('/collectivities/col-3');
            expect(col3.body.name).to.equal('Tantely mamy');
            expect(col3.body.location).to.equal('Brickaville');
        });

        it('should verify all members in collectivity have non-null referees', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1');

            res.body.members.forEach(member => {
                expect(member.referees, `Member ${member.id} should have referees`).to.not.be.null;
                expect(member.referees, `Member ${member.id} should have referees array`).to.be.an('array');
            });
        });
    });
});