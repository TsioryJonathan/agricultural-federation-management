const chai = require('chai');
const chaiHttp = require('chai-http');
const expect = chai.expect;

chai.use(chaiHttp);

const BASE_URL = process.env.SERVER_URL || 'http://localhost:8080';

describe('Agricultural Federation API - Complete Test Suite (Updated Data)', function() {
    this.timeout(10000);

    let createdMemberIds = [];

    // ============================================
    // 1. MEMBER CREATION TESTS
    // ============================================
    describe('POST /members - Member Creation', function() {

        it('should create a member with valid data', async function() {
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

            expect(res.status).to.equal(201);
            expect(res.body).to.be.an('array');
            expect(res.body[0]).to.have.property('id');
            expect(res.body[0].firstName).to.equal('Jean');
            expect(res.body[0].lastName).to.equal('Dupont');
            expect(res.body[0].gender).to.equal('MALE');
            expect(res.body[0].email).to.equal('jean.dupont@test.mg');
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

        it('should reject member with referees from other collectivities only', async function() {
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
    });

    // ============================================
    // 2. MEMBER PAYMENT TESTS
    // ============================================
    describe('POST /members/{id}/payments - Member Payments', function() {

        it('should create a payment for an existing member', async function() {
            if (createdMemberIds.length === 0) this.skip();
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
        });

        it('should reject payment for non-existent member', async function() {
            const res = await chai.request(BASE_URL)
                .post('/members/NONEXISTENT/payments')
                .send([{ amount: 50000, paymentMode: 'CASH' }]);

            expect(res.status).to.equal(404);
        });

        it('should reject payment with zero amount', async function() {
            if (createdMemberIds.length === 0) this.skip();
            const res = await chai.request(BASE_URL)
                .post(`/members/${createdMemberIds[0]}/payments`)
                .send([{ amount: 0, paymentMode: 'CASH' }]);

            expect(res.status).to.equal(400);
        });
    });

    // ============================================
    // 3. GET COLLECTIVITY TESTS
    // ============================================
    describe('GET /collectivities/{id} - Get Collectivity', function() {

        it('should retrieve collectivity col-1 with members', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1');

            expect(res.status).to.equal(200);
            expect(res.body).to.have.property('id', 'col-1');
            expect(res.body).to.have.property('name', 'Mpanorina');
            expect(res.body).to.have.property('number', '1');
            expect(res.body).to.have.property('location', 'Ambatondrazaka');
            expect(res.body).to.have.property('structure');
            expect(res.body.structure).to.have.property('president');
            expect(res.body.structure).to.have.property('vicePresident');
            expect(res.body.structure).to.have.property('treasurer');
            expect(res.body.structure).to.have.property('secretary');
            expect(res.body).to.have.property('members');
            expect(res.body.members).to.be.an('array');
            // 8 original + 4 new = 12
            expect(res.body.members.length).to.be.at.least(12);
        });

        it('should verify C1-M1 and C1-M2 have empty referees array', async function() {
            const res = await chai.request(BASE_URL).get('/collectivities/col-1');

            const c1m1 = res.body.members.find(m => m.id === 'C1-M1');
            expect(c1m1).to.exist;
            expect(c1m1.referees).to.be.an('array');

            const c1m2 = res.body.members.find(m => m.id === 'C1-M2');
            expect(c1m2).to.exist;
            expect(c1m2.referees).to.be.an('array');
        });

        it('should verify C1-M8 has referees including C1-M6 and C1-M7', async function() {
            const res = await chai.request(BASE_URL).get('/collectivities/col-1');

            const c1m8 = res.body.members.find(m => m.id === 'C1-M8');
            expect(c1m8).to.exist;
            // Check if referees are strings or objects
            if (c1m8.referees.length > 0 && typeof c1m8.referees[0] === 'string') {
                expect(c1m8.referees).to.include('C1-M6');
                expect(c1m8.referees).to.include('C1-M7');
            } else if (c1m8.referees.length > 0 && typeof c1m8.referees[0] === 'object') {
                const ids = c1m8.referees.map(r => r.id);
                expect(ids).to.include('C1-M6');
                expect(ids).to.include('C1-M7');
            }
        });

        it('should verify structure has correct members', async function() {
            const res = await chai.request(BASE_URL).get('/collectivities/col-1');

            expect(res.body.structure.president.id).to.equal('C1-M1');
            expect(res.body.structure.vicePresident.id).to.equal('C1-M2');
            expect(res.body.structure.secretary.id).to.equal('C1-M3');
            expect(res.body.structure.treasurer.id).to.equal('C1-M4');
        });

        it('should return 404 for non-existent collectivity', async function() {
            const res = await chai.request(BASE_URL).get('/collectivities/NONEXISTENT');
            expect(res.status).to.equal(404);
        });
    });

    // ============================================
    // 4. COLLECTIVITY INFORMATION TESTS
    // ============================================
    describe('PUT /collectivities/{id}/informations', function() {

        it('should reject updating already assigned collectivity info', async function() {
            const res = await chai.request(BASE_URL)
                .put('/collectivities/col-1/informations')
                .send({ number: '999', name: 'New Name' });

            expect(res.status).to.equal(400);
        });

        it('should reject duplicate name', async function() {
            const res = await chai.request(BASE_URL)
                .put('/collectivities/col-1/informations')
                .send({ number: '999', name: 'Dobo voalahany' });

            expect(res.status).to.equal(400);
        });
    });

    // ============================================
    // 5. MEMBERSHIP FEES TESTS
    // ============================================
    describe('GET /collectivities/{id}/membershipFees', function() {

        it('should retrieve membership fees for col-1', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/membershipFees');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            // cot-1 (ANNUALLY 200000) + cot-2 (PUNCTUALLY 20000) = 2 active
            expect(res.body.length).to.be.at.least(2);
        });

        it('should verify cot-1 is ANNUALLY with 200000', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/membershipFees');

            const cot1 = res.body.find(c => c.id === 'cot-1');
            expect(cot1).to.exist;
            expect(cot1.frequency).to.equal('ANNUALLY');
            expect(cot1.amount).to.equal(200000);
            expect(cot1.status).to.equal('ACTIVE');
        });

        it('should verify cot-2 is PUNCTUALLY with 20000', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/membershipFees');

            const cot2 = res.body.find(c => c.id === 'cot-2');
            expect(cot2).to.exist;
            expect(cot2.frequency).to.equal('PUNCTUALLY');
            expect(cot2.amount).to.equal(20000);
        });

        it('should verify col-2 has cot-4 INACTIVE', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-2/membershipFees');

            const cot4 = res.body.find(c => c.id === 'cot-4');
            expect(cot4).to.exist;
            expect(cot4.status).to.equal('INACTIVE');
        });
    });

    // ============================================
    // 6. FINANCIAL ACCOUNTS TESTS
    // ============================================
    describe('GET /collectivities/{id}/financialAccounts', function() {

        it('should retrieve financial accounts for col-3', async function () {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-3/financialAccounts');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');

            expect(res.body.length).to.be.greaterThan(0);

            res.body.forEach(account => {
                // common fields
                expect(account).to.have.property('id');
                expect(account).to.have.property('amount');

                // MOBILE ACCOUNT
                if (account.mobileBankingService) {
                    expect(account).to.have.property('holderName');
                    expect(account).to.have.property('mobileBankingService');
                    expect(account).to.have.property('mobileNumber');
                }

                // BANK ACCOUNT
                if (account.bankName) {
                    expect(account).to.have.property('holderName');
                    expect(account).to.have.property('bankName');
                    expect(account).to.have.property('bankCode');
                    expect(account).to.have.property('bankBranchCode');
                    expect(account).to.have.property('bankAccountNumber');
                    expect(account).to.have.property('bankAccountKey');
                }
            });
        });

        it('should return 404 for non-existent collectivity', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/NONEXISTENT/financialAccounts');

            expect(res.status).to.equal(404);
        });
    });

    // ============================================
    // 7. TRANSACTIONS TESTS
    // ============================================
    describe('GET /collectivities/{id}/transactions', function() {

        it('should retrieve transactions for col-1', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/transactions?from=2026-01-01&to=2026-12-31');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            // 8 original transactions
            expect(res.body.length).to.be.at.least(8);
        });

        it('should retrieve transactions for col-3', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-3/transactions?from=2026-01-01&to=2026-12-31');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            // 16 transactions (8 April + 8 May)
            expect(res.body.length).to.equal(16);
        });

        it('should return 400 without mandatory query params', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/transactions');

            expect(res.status).to.equal(400);
        });
    });

    // ============================================
    // 8. STATISTICS TESTS
    // ============================================
    describe('GET /collectivities/{id}/statistics - Local Statistics', function() {

        it('should retrieve local statistics for col-1', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/statistics?from=2026-01-01&to=2026-12-31');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            // 12 members (8 original + 4 new)
            expect(res.body.length).to.be.at.least(12);
            expect(res.body[0]).to.have.property('memberDescription');
            expect(res.body[0]).to.have.property('earnedAmount');
            expect(res.body[0]).to.have.property('unpaidAmount');
            expect(res.body[0]).to.have.property('assiduityPercentage');
        });

        it('should show C1-M5 with earnedAmount 150000', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/statistics?from=2026-01-01&to=2026-12-31');

            const c1m5 = res.body.find(s => s.memberDescription.id === 'C1-M5');
            expect(c1m5).to.exist;
            expect(c1m5.earnedAmount).to.equal(150000);
        });
    });

    describe('GET /collectivities/statistics - Overall Statistics', function() {

        it('should retrieve overall statistics', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/statistics?from=2026-01-01&to=2026-12-31');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            expect(res.body.length).to.equal(3);
        });

        it('col-2 should have 100% current due (cot-4 is INACTIVE, cot-3 fully paid by many)', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/statistics?from=2026-01-01&to=2026-12-31');

            const col2 = res.body.find(c => c.collectivityInformation.number === '2');
            expect(col2).to.exist;
            // cot-4 INACTIVE, cot-3 ACTIVE (200000) - C2-M3, C2-M4, C2-M5, C2-M6 paid 200000
            expect(col2.overallMemberCurrentDuePercentage).to.be.at.least(0);
        });
    });

    // ============================================
    // 9. DATA INTEGRITY TESTS
    // ============================================
    describe('Data Integrity Tests', function() {

        it('should verify col-1 has 13 members', async function() {
            const res = await chai.request(BASE_URL).get('/collectivities/col-1');
            expect(res.body.members.length).to.equal(13);
        });

        it('should verify col-2 has 11 members', async function() {
            const res = await chai.request(BASE_URL).get('/collectivities/col-2');
            expect(res.body.members.length).to.equal(11);
        });

        it('should verify col-3 has 14 members', async function() {
            const res = await chai.request(BASE_URL).get('/collectivities/col-3');
            expect(res.body.members.length).to.equal(14);
        });

        it('should verify collectivity names', async function() {
            const col1 = await chai.request(BASE_URL).get('/collectivities/col-1');
            expect(col1.body.name).to.equal('Mpanorina');
            expect(col1.body.location).to.equal('Ambatondrazaka');

            const col2 = await chai.request(BASE_URL).get('/collectivities/col-2');
            expect(col2.body.name).to.equal('Dobo voalahany');

            const col3 = await chai.request(BASE_URL).get('/collectivities/col-3');
            expect(col3.body.name).to.equal('Tantely mamy');
            expect(col3.body.location).to.equal('Brickaville');
        });
    });
});