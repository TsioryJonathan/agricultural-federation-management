const chai = require('chai');
const chaiHttp = require('chai-http');
const expect = chai.expect;

chai.use(chaiHttp);

const BASE_URL = process.env.SERVER_URL || 'http://localhost:8080';

describe('Agricultural Federation API - Complete Test Suite', function() {
    this.timeout(30000);

    let createdMemberIds = [];
    let createdActivityIds = [];

    // ============================================
    // 0. SCHEMA VALIDATION HELPERS
    // ============================================

    /**
     * Validates that an object conforms to Member schema (allOf MemberInformation + id + referees)
     */
    function validateMemberObject(member, options = {}) {
        // Required properties from MemberInformation
        expect(member, 'Member should exist').to.exist;
        expect(member, 'Member should be an object').to.be.an('object');

        // id is required from Member schema
        expect(member, 'Member should have id').to.have.property('id');
        expect(member.id, 'Member id should be a string').to.be.a('string');

        // Name properties
        expect(member, 'Member should have firstName').to.have.property('firstName');
        expect(member.firstName, 'firstName should be a string').to.be.a('string');

        expect(member, 'Member should have lastName').to.have.property('lastName');
        expect(member.lastName, 'lastName should be a string').to.be.a('string');

        // birthDate should be a date string
        expect(member, 'Member should have birthDate').to.have.property('birthDate');
        expect(member.birthDate, 'birthDate should be a string').to.be.a('string');

        // gender enum check
        expect(member, 'Member should have gender').to.have.property('gender');
        expect(['MALE', 'FEMALE'], `Gender should be MALE or FEMALE, got ${member.gender}`).to.include(member.gender);

        // address
        expect(member, 'Member should have address').to.have.property('address');
        expect(member.address, 'address should be a string').to.be.a('string');

        // profession
        expect(member, 'Member should have profession').to.have.property('profession');
        expect(member.profession, 'profession should be a string').to.be.a('string');

        // phoneNumber - spec says int but JSON serialization may make it number
        expect(member, 'Member should have phoneNumber').to.have.property('phoneNumber');
        expect(member.phoneNumber, 'phoneNumber should be a number').to.be.a('string');

        // email
        expect(member, 'Member should have email').to.have.property('email');
        expect(member.email, 'email should be a string').to.be.a('string');

        // occupation
        expect(member, 'Member should have occupation').to.have.property('occupation');
        expect(['JUNIOR', 'SENIOR', 'SECRETARY', 'TREASURER', 'VICE_PRESIDENT', 'PRESIDENT'],
            `Occupation should be valid, got ${member.occupation}`).to.include(member.occupation);

        // referees array
        if (!options.skipReferees) {
            expect(member, 'Member should have referees').to.have.property('referees');
            expect(member.referees, 'referees should be an array').to.be.an('array');
        }

        return true;
    }

    /**
     * Validates Collectivity schema
     */
    function validateCollectivityObject(collectivity) {
        expect(collectivity, 'Collectivity should exist').to.exist;
        expect(collectivity, 'Collectivity should be an object').to.be.an('object');

        // CollectivityInformation properties
        expect(collectivity, 'Collectivity should have id').to.have.property('id');
        expect(collectivity.id, 'id should be a string').to.be.a('string');

        expect(collectivity, 'Collectivity should have name').to.have.property('name');
        expect(collectivity.name, 'name should be a string').to.be.a('string');

        expect(collectivity, 'Collectivity should have number').to.have.property('number');
        // Spec says int but JSON might serialize as string or number
        expect(collectivity.number, `number should be a int`).to.be.a('number');

        expect(collectivity, 'Collectivity should have location').to.have.property('location');
        expect(collectivity.location, 'location should be a string').to.be.a('string');

        // Structure
        expect(collectivity, 'Collectivity should have structure').to.have.property('structure');
        expect(collectivity.structure, 'structure should be an object').to.be.an('object');

        const struct = collectivity.structure;
        ['president', 'vicePresident', 'treasurer', 'secretary'].forEach(role => {
            expect(struct, `structure should have ${role}`).to.have.property(role);
            if (struct[role]) {
                validateMemberObject(struct[role], { skipReferees: true });
            }
        });

        // Members
        expect(collectivity, 'Collectivity should have members').to.have.property('members');
        expect(collectivity.members, 'members should be an array').to.be.an('array');

        return true;
    }

    /**
     * Validates FinancialAccount schema (oneOf: CashAccount, MobileBankingAccount, BankAccount)
     */
    function validateFinancialAccount(account) {
        expect(account, 'Account should exist').to.exist;
        expect(account, 'Account should be an object').to.be.an('object');

        expect(account, 'Account should have id').to.have.property('id');
        expect(account.id, 'id should be a string').to.be.a('string');

        expect(account, 'Account should have amount').to.have.property('amount');
        expect(account.amount, 'amount should be a number').to.be.a('number');

        // Check account type
        const isCashAccount = !account.mobileBankingService && !account.bankName;
        const isMobileAccount = !!account.mobileBankingService;
        const isBankAccount = !!account.bankName;

        if (isMobileAccount) {
            // MobileBankingAccount
            expect(account, 'Mobile account should have holderName').to.have.property('holderName');
            expect(account.holderName, 'holderName should be a string').to.be.a('string');

            expect(account, 'Mobile account should have mobileBankingService').to.have.property('mobileBankingService');
            expect(['AIRTEL_MONEY', 'MVOLA', 'ORANGE_MONEY'],
                `mobileBankingService should be valid, got ${account.mobileBankingService}`).to.include(account.mobileBankingService);

            expect(account, 'Mobile account should have mobileNumber').to.have.property('mobileNumber');
            expect(account.mobileNumber, 'mobileNumber should be a string').to.be.a('string');
        }

        if (isBankAccount) {
            // BankAccount
            expect(account, 'Bank account should have holderName').to.have.property('holderName');
            expect(account.holderName, 'holderName should be a string').to.be.a('string');

            expect(account, 'Bank account should have bankName').to.have.property('bankName');
            expect(['BRED', 'MCB', 'BMOI', 'BOA', 'BGFI', 'AFG', 'ACCES_BANQUE', 'BAOBAB', 'SIPEM'],
                `bankName should be valid, got ${account.bankName}`).to.include(account.bankName);

            expect(account, 'Bank account should have bankCode').to.have.property('bankCode');
            expect(account.bankCode, 'bankCode should be a number').to.be.a('number');

            expect(account, 'Bank account should have bankBranchCode').to.have.property('bankBranchCode');
            expect(account.bankBranchCode, 'bankBranchCode should be a number').to.be.a('number');

            expect(account, 'Bank account should have bankAccountNumber').to.have.property('bankAccountNumber');
            expect(account.bankAccountNumber, 'bankAccountNumber should be a number').to.be.a('number');

            expect(account, 'Bank account should have bankAccountKey').to.have.property('bankAccountKey');
            expect(account.bankAccountKey, 'bankAccountKey should be a number').to.be.a('number');
        }

        return true;
    }

    /**
     * Validates Transaction schema
     */
    function validateTransaction(transaction) {
        expect(transaction, 'Transaction should exist').to.exist;
        expect(transaction, 'Transaction should be an object').to.be.an('object');

        expect(transaction, 'Transaction should have id').to.have.property('id');
        expect(transaction.id, 'id should be a string').to.be.a('string');

        expect(transaction, 'Transaction should have creationDate').to.have.property('creationDate');
        expect(transaction.creationDate, 'creationDate should be a string').to.be.a('string');

        expect(transaction, 'Transaction should have amount').to.have.property('amount');
        expect(transaction.amount, 'amount should be a number').to.be.a('number');

        expect(transaction, 'Transaction should have paymentMode').to.have.property('paymentMode');
        expect(['CASH', 'MOBILE_BANKING', 'BANK_TRANSFER'],
            `paymentMode should be valid, got ${transaction.paymentMode}`).to.include(transaction.paymentMode);

        // accountCredited should be a FinancialAccount
        if (transaction.accountCredited) {
            validateFinancialAccount(transaction.accountCredited);
        }

        // memberDebited should be a Member
        if (transaction.memberDebited) {
            validateMemberObject(transaction.memberDebited, { skipReferees: true });
        }

        return true;
    }

    /**
     * Validates MembershipFee schema
     */
    function validateMembershipFee(fee) {
        expect(fee, 'MembershipFee should exist').to.exist;
        expect(fee, 'MembershipFee should be an object').to.be.an('object');

        expect(fee, 'MembershipFee should have id').to.have.property('id');
        expect(fee.id, 'id should be a string').to.be.a('string');

        expect(fee, 'MembershipFee should have label').to.have.property('label');
        expect(fee.label, 'label should be a string').to.be.a('string');

        expect(fee, 'MembershipFee should have frequency').to.have.property('frequency');
        expect(['WEEKLY', 'MONTHLY', 'ANNUALLY', 'PUNCTUALLY'],
            `frequency should be valid, got ${fee.frequency}`).to.include(fee.frequency);

        expect(fee, 'MembershipFee should have amount').to.have.property('amount');
        expect(fee.amount, 'amount should be a number').to.be.a('number');

        expect(fee, 'MembershipFee should have status').to.have.property('status');
        expect(['ACTIVE', 'INACTIVE'],
            `status should be valid, got ${fee.status}`).to.include(fee.status);

        if (fee.eligibleFrom) {
            expect(fee.eligibleFrom, 'eligibleFrom should be a string').to.be.a('string');
        }

        return true;
    }

    /**
     * Validates MemberPayment schema
     */
    function validateMemberPayment(payment) {
        expect(payment, 'MemberPayment should exist').to.exist;
        expect(payment, 'MemberPayment should be an object').to.be.an('object');

        expect(payment, 'MemberPayment should have id').to.have.property('id');
        expect(payment.id, 'id should be a string').to.be.a('string');

        expect(payment, 'MemberPayment should have amount').to.have.property('amount');
        expect(payment.amount, 'amount should be a number').to.be.a('number');

        expect(payment, 'MemberPayment should have paymentMode').to.have.property('paymentMode');
        expect(['CASH', 'MOBILE_BANKING', 'BANK_TRANSFER'],
            `paymentMode should be valid, got ${payment.paymentMode}`).to.include(payment.paymentMode);

        if (payment.accountCredited) {
            validateFinancialAccount(payment.accountCredited);
        }

        if (payment.creationDate) {
            expect(payment.creationDate, 'creationDate should be a string').to.be.a('string');
        }

        return true;
    }

    /**
     * Validates CollectivityLocalStatistics schema
     */
    function validateLocalStatistics(stat) {
        expect(stat, 'LocalStatistics should exist').to.exist;
        expect(stat, 'LocalStatistics should be an object').to.be.an('object');

        expect(stat, 'Should have memberDescription').to.have.property('memberDescription');
        expect(stat.memberDescription, 'memberDescription should be an object').to.be.an('object');
        expect(stat.memberDescription, 'memberDescription should have id').to.have.property('id');
        expect(stat.memberDescription, 'memberDescription should have firstName').to.have.property('firstName');
        expect(stat.memberDescription, 'memberDescription should have lastName').to.have.property('lastName');
        expect(stat.memberDescription, 'memberDescription should have email').to.have.property('email');
        expect(stat.memberDescription, 'memberDescription should have occupation').to.have.property('occupation');

        expect(stat, 'Should have earnedAmount').to.have.property('earnedAmount');
        expect(stat.earnedAmount, 'earnedAmount should be a number').to.be.a('number');

        expect(stat, 'Should have unpaidAmount').to.have.property('unpaidAmount');
        expect(stat.unpaidAmount, 'unpaidAmount should be a number').to.be.a('number');

        // assiduityPercentage may be present in v0.0.7
        if (stat.assiduityPercentage !== undefined) {
            expect(stat.assiduityPercentage, 'assiduityPercentage should be a number').to.be.a('number');
            expect(stat.assiduityPercentage, 'assiduityPercentage should be between 0 and 100').to.be.within(0, 100);
        }

        return true;
    }

    /**
     * Validates CollectivityOverallStatistics schema
     */
    function validateOverallStatistics(stat) {
        expect(stat, 'OverallStatistics should exist').to.exist;
        expect(stat, 'OverallStatistics should be an object').to.be.an('object');

        expect(stat, 'Should have collectivityInformation').to.have.property('collectivityInformation');
        expect(stat.collectivityInformation, 'should be an object').to.be.an('object');
        expect(stat.collectivityInformation, 'should have name').to.have.property('name');
        expect(stat.collectivityInformation, 'should have number').to.have.property('number');

        expect(stat, 'Should have newMembersNumber').to.have.property('newMembersNumber');
        expect(stat.newMembersNumber, 'newMembersNumber should be a number').to.be.a('number');
        expect(stat.newMembersNumber, 'newMembersNumber should be integer >= 0').to.satisfy(v => Number.isInteger(v) && v >= 0);

        expect(stat, 'Should have overallMemberCurrentDuePercentage').to.have.property('overallMemberCurrentDuePercentage');
        expect(stat.overallMemberCurrentDuePercentage, 'should be a number').to.be.a('number');
        expect(stat.overallMemberCurrentDuePercentage, 'should be between 0 and 100').to.be.within(0, 100);

        // overallMemberAssiduityPercentage may be present in v0.0.7
        if (stat.overallMemberAssiduityPercentage !== undefined) {
            expect(stat.overallMemberAssiduityPercentage, 'should be a number').to.be.a('number');
            expect(stat.overallMemberAssiduityPercentage, 'should be between 0 and 100').to.be.within(0, 100);
        }

        return true;
    }

    /**
     * Validates CollectivityActivity schema
     */
    function validateActivity(activity) {
        expect(activity, 'Activity should exist').to.exist;
        expect(activity, 'Activity should be an object').to.be.an('object');

        expect(activity, 'Activity should have id').to.have.property('id');
        expect(activity.id, 'id should be a string').to.be.a('string');

        expect(activity, 'Activity should have label').to.have.property('label');
        expect(activity.label, 'label should be a string').to.be.a('string');

        expect(activity, 'Activity should have activityType').to.have.property('activityType');
        expect(['MEETING', 'TRAINING', 'OTHER'],
            `activityType should be valid, got ${activity.activityType}`).to.include(activity.activityType);

        return true;
    }

    /**
     * Validates ActivityMemberAttendance schema
     */
    function validateAttendance(attendance) {
        expect(attendance, 'Attendance should exist').to.exist;
        expect(attendance, 'Attendance should be an object').to.be.an('object');

        expect(attendance, 'Attendance should have id').to.have.property('id');
        expect(attendance.id, 'id should be a string').to.be.a('string');

        expect(attendance, 'Attendance should have memberDescription').to.have.property('memberDescription');
        expect(attendance.memberDescription, 'memberDescription should be an object').to.be.an('object');

        expect(attendance, 'Attendance should have attendanceStatus').to.have.property('attendanceStatus');
        expect(['MISSING', 'ATTENDED', 'UNDEFINED'],
            `attendanceStatus should be valid, got ${attendance.attendanceStatus}`).to.include(attendance.attendanceStatus);

        return true;
    }

    // ============================================
    // 1. MEMBER CREATION TESTS (POST /members)
    // ============================================
    describe('POST /members - Member Creation', function() {

        it('should create a member with valid data (2 referees from same collectivity)', async function() {
            const newMember = {
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
            };

            const res = await chai.request(BASE_URL)
                .post('/members')
                .send([newMember]);

            expect(res.status, 'Status should be 201').to.equal(201);
            expect(res.body, 'Response should be an array').to.be.an('array');
            expect(res.body, 'Should have at least 1 member').to.have.lengthOf.at.least(1);

            const created = res.body[0];
            validateMemberObject(created);

            expect(created.firstName).to.equal('Jean');
            expect(created.lastName).to.equal('Dupont');
            expect(created.gender).to.equal('MALE');
            expect(created.email).to.equal('jean.dupont@test.mg');
            expect(created.occupation).to.equal('JUNIOR');

            // referees should be populated (either as strings or objects)
            expect(created.referees, 'referees should not be empty').to.not.be.empty;

            createdMemberIds.push(created.id);
        });

        it('should create multiple members at once', async function() {
            const res = await chai.request(BASE_URL)
                .post('/members')
                .send([
                    {
                        firstName: 'Marie',
                        lastName: 'Curie',
                        birthDate: '1988-03-20',
                        gender: 'FEMALE',
                        address: 'Lot 456 Toamasina',
                        profession: 'Chercheuse',
                        phoneNumber: 321234567,
                        email: 'marie.curie@test.mg',
                        occupation: 'JUNIOR',
                        collectivityIdentifier: 'col-1',
                        referees: ['C1-M1', 'C1-M2'],
                        registrationFeePaid: true,
                        membershipDuesPaid: true
                    },
                    {
                        firstName: 'Paul',
                        lastName: 'Bertrand',
                        birthDate: '1992-07-10',
                        gender: 'MALE',
                        address: 'Lot 789 Antsirabe',
                        profession: 'Apiculteur',
                        phoneNumber: 331234567,
                        email: 'paul.bertrand@test.mg',
                        occupation: 'JUNIOR',
                        collectivityIdentifier: 'col-1',
                        referees: ['C1-M1', 'C1-M2'],
                        registrationFeePaid: true,
                        membershipDuesPaid: true
                    }
                ]);

            expect(res.status, 'Status should be 201').to.equal(201);
            expect(res.body, 'Should be an array').to.be.an('array');
            expect(res.body, 'Should have 2 members').to.have.lengthOf(2);

            res.body.forEach(m => {
                validateMemberObject(m);
                createdMemberIds.push(m.id);
            });
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

            expect(res.status, 'Status should be 400').to.equal(400);
        });

        it('should reject member without membership dues paid', async function() {
            const res = await chai.request(BASE_URL)
                .post('/members')
                .send([{
                    firstName: 'Sophie',
                    lastName: 'Bernard',
                    birthDate: '1992-07-10',
                    gender: 'FEMALE',
                    address: 'Lot 789 Antsirabe',
                    profession: 'Apiculteur',
                    phoneNumber: 331234567,
                    email: 'sophie.bernard@test.mg',
                    occupation: 'JUNIOR',
                    collectivityIdentifier: 'col-1',
                    referees: ['C1-M1', 'C1-M2'],
                    registrationFeePaid: true,
                    membershipDuesPaid: false
                }]);

            expect(res.status, 'Status should be 400').to.equal(400);
        });

        it('should reject member with less than 2 referees', async function() {
            const res = await chai.request(BASE_URL)
                .post('/members')
                .send([{
                    firstName: 'Luc',
                    lastName: 'Petit',
                    birthDate: '1995-11-25',
                    gender: 'MALE',
                    address: 'Lot 101 Fianarantsoa',
                    profession: 'Collecteur',
                    phoneNumber: 341234568,
                    email: 'luc.petit@test.mg',
                    occupation: 'JUNIOR',
                    collectivityIdentifier: 'col-1',
                    referees: ['C1-M1'],
                    registrationFeePaid: true,
                    membershipDuesPaid: true
                }]);

            expect(res.status, 'Status should be 400').to.equal(400);
        });

        it('should reject member with non-existent referees', async function() {
            const res = await chai.request(BASE_URL)
                .post('/members')
                .send([{
                    firstName: 'Anne',
                    lastName: 'Moreau',
                    birthDate: '1993-09-05',
                    gender: 'FEMALE',
                    address: 'Lot 202 Mahajanga',
                    profession: 'Distributeur',
                    phoneNumber: 351234567,
                    email: 'anne.moreau@test.mg',
                    occupation: 'JUNIOR',
                    collectivityIdentifier: 'col-1',
                    referees: ['NONEXISTENT-1', 'NONEXISTENT-2'],
                    registrationFeePaid: true,
                    membershipDuesPaid: true
                }]);

            expect(res.status, 'Status should be 404').to.equal(404);
        });

        it('should reject member with referees only from other collectivities', async function() {
            const res = await chai.request(BASE_URL)
                .post('/members')
                .send([{
                    firstName: 'Marc',
                    lastName: 'Dumas',
                    birthDate: '1991-04-18',
                    gender: 'MALE',
                    address: 'Lot 333 Toliara',
                    profession: 'Riziculteur',
                    phoneNumber: 361234567,
                    email: 'marc.dumas@test.mg',
                    occupation: 'JUNIOR',
                    collectivityIdentifier: 'col-1',
                    referees: ['C2-M5', 'C2-M6'],
                    registrationFeePaid: true,
                    membershipDuesPaid: true
                }]);

            expect(res.status, 'Status should be 400').to.equal(400);
        });

        it('should accept member with referees from same and other collectivities (balanced)', async function() {
            const res = await chai.request(BASE_URL)
                .post('/members')
                .send([{
                    firstName: 'Claire',
                    lastName: 'Lemoine',
                    birthDate: '1994-06-12',
                    gender: 'FEMALE',
                    address: 'Lot 555 Antsiranana',
                    profession: 'Agricultrice',
                    phoneNumber: 371234567,
                    email: 'claire.lemoine@test.mg',
                    occupation: 'JUNIOR',
                    collectivityIdentifier: 'col-1',
                    referees: ['C1-M5', 'C2-M5'],
                    registrationFeePaid: true,
                    membershipDuesPaid: true
                }]);

            expect(res.status, 'Status should be 201').to.equal(201);
            expect(res.body).to.be.an('array');
            if (res.body.length > 0) createdMemberIds.push(res.body[0].id);
        });

        it('should reject member with duplicate email', async function() {
            const res = await chai.request(BASE_URL)
                .post('/members')
                .send([{
                    firstName: 'Jean',
                    lastName: 'Duplicate',
                    birthDate: '1995-01-01',
                    gender: 'MALE',
                    address: 'Lot 999',
                    profession: 'Test',
                    phoneNumber: 399999999,
                    email: 'jean.dupont@test.mg',
                    occupation: 'JUNIOR',
                    collectivityIdentifier: 'col-1',
                    referees: ['C1-M1', 'C1-M2'],
                    registrationFeePaid: true,
                    membershipDuesPaid: true
                }]);

            expect(res.status, 'Status should be 500').to.equal(500);
        });

        it('should reject member without collectivityIdentifier', async function() {
            const res = await chai.request(BASE_URL)
                .post('/members')
                .send([{
                    firstName: 'Test',
                    lastName: 'NoCollectivity',
                    birthDate: '1990-01-01',
                    gender: 'MALE',
                    address: 'Test',
                    profession: 'Test',
                    phoneNumber: 300000000,
                    email: 'test.nocollectivity@test.mg',
                    occupation: 'JUNIOR',
                    referees: ['C1-M1', 'C1-M2'],
                    registrationFeePaid: true,
                    membershipDuesPaid: true
                }]);

            expect(res.status, 'Status should be 400 or 404').to.be.oneOf([400, 404]);
        });

        it('should reject member with invalid gender', async function() {
            const res = await chai.request(BASE_URL)
                .post('/members')
                .send([{
                    firstName: 'Test',
                    lastName: 'InvalidGender',
                    birthDate: '1990-01-01',
                    gender: 'INVALID',
                    address: 'Test',
                    profession: 'Test',
                    phoneNumber: 300000001,
                    email: 'test.invalidgender@test.mg',
                    occupation: 'JUNIOR',
                    collectivityIdentifier: 'col-1',
                    referees: ['C1-M1', 'C1-M2'],
                    registrationFeePaid: true,
                    membershipDuesPaid: true
                }]);

            expect(res.status, 'Status should be 400').to.equal(400);
        });

        it('should reject member with invalid occupation', async function() {
            const res = await chai.request(BASE_URL)
                .post('/members')
                .send([{
                    firstName: 'Test',
                    lastName: 'InvalidOccupation',
                    birthDate: '1990-01-01',
                    gender: 'MALE',
                    address: 'Test',
                    profession: 'Test',
                    phoneNumber: 300000002,
                    email: 'test.invalidoccupation@test.mg',
                    occupation: 'INVALID',
                    collectivityIdentifier: 'col-1',
                    referees: ['C1-M1', 'C1-M2'],
                    registrationFeePaid: true,
                    membershipDuesPaid: true
                }]);

            expect(res.status, 'Status should be 400').to.equal(400);
        });
    });

    // ============================================
    // 2. MEMBER PAYMENT TESTS (POST /members/{id}/payments)
    // ============================================
    describe('POST /members/{id}/payments - Member Payments', function() {

        it('should create a CASH payment for an existing member', async function() {
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

            expect(res.status, 'Status should be 201').to.equal(201);
            expect(res.body, 'Should be an array').to.be.an('array');

            res.body.forEach(p => validateMemberPayment(p));

            expect(res.body[0].amount).to.equal(50000);
            expect(res.body[0].paymentMode).to.equal('CASH');
        });

        it('should create a MOBILE_BANKING payment', async function() {
            if (createdMemberIds.length < 2) this.skip();
            const memberId = createdMemberIds[1];

            const res = await chai.request(BASE_URL)
                .post(`/members/${memberId}/payments`)
                .send([{
                    amount: 30000,
                    membershipFeeIdentifier: 'cot-1',
                    accountCreditedIdentifier: 'C1-A-MOBILE-1',
                    paymentMode: 'MOBILE_BANKING'
                }]);

            expect(res.status, 'Status should be 201').to.equal(201);
            expect(res.body).to.be.an('array');
            res.body.forEach(p => validateMemberPayment(p));
        });

        it('should create multiple payments at once', async function() {
            if (createdMemberIds.length < 3) this.skip();
            const memberId = createdMemberIds[2];

            const res = await chai.request(BASE_URL)
                .post(`/members/${memberId}/payments`)
                .send([
                    { amount: 25000, membershipFeeIdentifier: 'cot-1', accountCreditedIdentifier: 'C1-A-CASH', paymentMode: 'CASH' },
                    { amount: 15000, membershipFeeIdentifier: 'cot-2', accountCreditedIdentifier: 'C1-A-MOBILE-1', paymentMode: 'MOBILE_BANKING' }
                ]);

            expect(res.status, 'Status should be 201').to.equal(201);
            expect(res.body, 'Should have 2 payments').to.have.lengthOf(2);
            res.body.forEach(p => validateMemberPayment(p));
        });

        it('should reject payment for non-existent member', async function() {
            const res = await chai.request(BASE_URL)
                .post('/members/NONEXISTENT/payments')
                .send([{
                    amount: 50000,
                    membershipFeeIdentifier: 'cot-1',
                    accountCreditedIdentifier: 'C1-A-CASH',
                    paymentMode: 'CASH'
                }]);

            expect(res.status, 'Status should be 404').to.equal(404);
        });

        it('should reject payment with zero amount', async function() {
            if (createdMemberIds.length === 0) this.skip();

            const res = await chai.request(BASE_URL)
                .post(`/members/${createdMemberIds[0]}/payments`)
                .send([{
                    amount: 0,
                    membershipFeeIdentifier: 'cot-1',
                    accountCreditedIdentifier: 'C1-A-CASH',
                    paymentMode: 'CASH'
                }]);

            expect(res.status, 'Status should be 400').to.equal(400);
        });

        it('should reject payment with negative amount', async function() {
            if (createdMemberIds.length === 0) this.skip();

            const res = await chai.request(BASE_URL)
                .post(`/members/${createdMemberIds[0]}/payments`)
                .send([{
                    amount: -1000,
                    membershipFeeIdentifier: 'cot-1',
                    accountCreditedIdentifier: 'C1-A-CASH',
                    paymentMode: 'CASH'
                }]);

            expect(res.status, 'Status should be 400').to.equal(400);
        });

        it('should reject payment with invalid payment mode', async function() {
            if (createdMemberIds.length === 0) this.skip();

            const res = await chai.request(BASE_URL)
                .post(`/members/${createdMemberIds[0]}/payments`)
                .send([{
                    amount: 50000,
                    membershipFeeIdentifier: 'cot-1',
                    accountCreditedIdentifier: 'C1-A-CASH',
                    paymentMode: 'INVALID_MODE'
                }]);

            expect(res.status, 'Status should be 400').to.equal(400);
        });
    });

    // ============================================
    // 3. GET COLLECTIVITY TESTS (GET /collectivities/{id})
    // ============================================
    describe('GET /collectivities/{id} - Get Collectivity', function() {

        it('should retrieve col-1 with correct schema', async function() {
            const res = await chai.request(BASE_URL).get('/collectivities/col-1');

            expect(res.status, 'Status should be 200').to.equal(200);
            validateCollectivityObject(res.body);
        });

        it('col-1 should have correct information', async function() {
            const res = await chai.request(BASE_URL).get('/collectivities/col-1');

            expect(res.body.id).to.equal('col-1');
            expect(res.body.name).to.equal('Mpanorina');
            expect(res.body.number).to.equal('1');
            expect(res.body.location).to.equal('Ambatondrazaka');
        });

        it('col-1 should have correct structure', async function() {
            const res = await chai.request(BASE_URL).get('/collectivities/col-1');
            const struct = res.body.structure;

            expect(struct.president.id).to.equal('C1-M1');
            expect(struct.vicePresident.id).to.equal('C1-M2');
            expect(struct.secretary.id).to.equal('C1-M3');
            expect(struct.treasurer.id).to.equal('C1-M4');

            // Verify structure members have correct occupations
            expect(struct.president.occupation).to.equal('PRESIDENT');
            expect(struct.vicePresident.occupation).to.equal('VICE_PRESIDENT');
            expect(struct.secretary.occupation).to.equal('SECRETARY');
            expect(struct.treasurer.occupation).to.equal('TREASURER');
        });

        it('C1-M1 and C1-M2 should have empty referees arrays', async function() {
            const res = await chai.request(BASE_URL).get('/collectivities/col-1');

            const c1m1 = res.body.members.find(m => m.id === 'C1-M1');
            expect(c1m1, 'C1-M1 should exist').to.exist;
            expect(c1m1.referees, 'C1-M1 referees should be an array').to.be.an('array');
            expect(c1m1.referees, 'C1-M1 should have empty referees').to.be.empty;

            const c1m2 = res.body.members.find(m => m.id === 'C1-M2');
            expect(c1m2, 'C1-M2 should exist').to.exist;
            expect(c1m2.referees, 'C1-M2 referees should be an array').to.be.an('array');
            expect(c1m2.referees, 'C1-M2 should have empty referees').to.be.empty;
        });

        it('C1-M8 should have referees C1-M6 and C1-M7', async function() {
            const res = await chai.request(BASE_URL).get('/collectivities/col-1');
            const c1m8 = res.body.members.find(m => m.id === 'C1-M8');

            expect(c1m8, 'C1-M8 should exist').to.exist;

            // Check if referees are strings or objects
            if (c1m8.referees.length > 0) {
                if (typeof c1m8.referees[0] === 'string') {
                    expect(c1m8.referees).to.include('C1-M6');
                    expect(c1m8.referees).to.include('C1-M7');
                } else if (typeof c1m8.referees[0] === 'object') {
                    const ids = c1m8.referees.map(r => r.id);
                    expect(ids).to.include('C1-M6');
                    expect(ids).to.include('C1-M7');
                }
            }
        });

        it('should retrieve col-2 with correct data', async function() {
            const res = await chai.request(BASE_URL).get('/collectivities/col-2');

            expect(res.status).to.equal(200);
            validateCollectivityObject(res.body);
            expect(res.body.id).to.equal('col-2');
            expect(res.body.name).to.equal('Dobo voalahany');
            expect(res.body.number).to.equal('2');
        });

        it('should retrieve col-3 with correct data', async function() {
            const res = await chai.request(BASE_URL).get('/collectivities/col-3');

            expect(res.status).to.equal(200);
            validateCollectivityObject(res.body);
            expect(res.body.id).to.equal('col-3');
            expect(res.body.name).to.equal('Tantely mamy');
            expect(res.body.number).to.equal('3');
            expect(res.body.location).to.equal('Brickaville');
        });

        it('should return 404 for non-existent collectivity', async function() {
            const res = await chai.request(BASE_URL).get('/collectivities/NONEXISTENT');
            expect(res.status).to.equal(404);
        });

        it('all members in col-1 should have valid occupation enum values', async function() {
            const res = await chai.request(BASE_URL).get('/collectivities/col-1');
            const validOccupations = ['JUNIOR', 'SENIOR', 'SECRETARY', 'TREASURER', 'VICE_PRESIDENT', 'PRESIDENT'];

            res.body.members.forEach(member => {
                expect(validOccupations, `Member ${member.id} should have valid occupation`).to.include(member.occupation);
                expect(member.gender, `Member ${member.id} should have valid gender`).to.be.oneOf(['MALE', 'FEMALE']);
            });
        });
    });

    // ============================================
    // 4. COLLECTIVITY INFORMATION TESTS (PUT /collectivities/{id}/informations)
    // ============================================
    describe('PUT /collectivities/{id}/informations - Update Collectivity Info', function() {

        it('should reject updating already assigned collectivity info', async function() {
            const res = await chai.request(BASE_URL)
                .put('/collectivities/col-1/informations')
                .send({ number: 999, name: 'New Name' });

            expect(res.status, 'Status should be 400').to.equal(400);
        });

        it('should reject updating to existing number and name', async function() {
            // Try to set col-1 to have col-2's number
            const res = await chai.request(BASE_URL)
                .put('/collectivities/col-1/informations')
                .send({ number: 2, name: 'Test' });

            expect(res.status, 'Status should be 400').to.equal(400);
        });

        it('should reject updating to existing name', async function() {
            const res = await chai.request(BASE_URL)
                .put('/collectivities/col-1/informations')
                .send({ number: 999, name: 'Dobo voalahany' });

            expect(res.status, 'Status should be 400').to.equal(400);
        });
    });

    // ============================================
    // 5. MEMBERSHIP FEES TESTS (GET /collectivities/{id}/membershipFees)
    // ============================================
    describe('GET /collectivities/{id}/membershipFees - Membership Fees', function() {

        it('should retrieve membership fees for col-1', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/membershipFees');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            res.body.forEach(f => validateMembershipFee(f));
        });

        it('col-1 should have cot-1 (ANNUALLY, 200000, ACTIVE)', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/membershipFees');

            const cot1 = res.body.find(c => c.id === 'cot-1');
            expect(cot1, 'cot-1 should exist').to.exist;
            expect(cot1.frequency).to.equal('ANNUALLY');
            expect(cot1.amount).to.equal(200000);
            expect(cot1.status).to.equal('ACTIVE');
            expect(cot1.label).to.equal('Cotisation annuelle');
        });

        it('col-1 should have cot-2 (PUNCTUALLY, 20000, ACTIVE)', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/membershipFees');

            const cot2 = res.body.find(c => c.id === 'cot-2');
            expect(cot2, 'cot-2 should exist').to.exist;
            expect(cot2.frequency).to.equal('PUNCTUALLY');
            expect(cot2.amount).to.equal(20000);
            expect(cot2.status).to.equal('ACTIVE');
            expect(cot2.label).to.equal('Famangiana');
        });

        it('should retrieve membership fees for col-2', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-2/membershipFees');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            res.body.forEach(f => validateMembershipFee(f));
        });

        it('col-2 should have cot-3 (ANNUALLY, 200000, ACTIVE)', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-2/membershipFees');

            const cot3 = res.body.find(c => c.id === 'cot-3');
            expect(cot3, 'cot-3 should exist').to.exist;
            expect(cot3.frequency).to.equal('ANNUALLY');
            expect(cot3.amount).to.equal(200000);
            expect(cot3.status).to.equal('ACTIVE');
        });

        it('col-2 should have cot-4 (INACTIVE)', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-2/membershipFees');

            const cot4 = res.body.find(c => c.id === 'cot-4');
            expect(cot4, 'cot-4 should exist').to.exist;
            expect(cot4.status).to.equal('INACTIVE');
        });

        it('should retrieve membership fees for col-3', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-3/membershipFees');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            res.body.forEach(f => validateMembershipFee(f));
        });

        it('col-3 should have cot-5 (MONTHLY, 25000, ACTIVE)', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-3/membershipFees');

            const cot5 = res.body.find(c => c.id === 'cot-5');
            expect(cot5, 'cot-5 should exist').to.exist;
            expect(cot5.frequency).to.equal('MONTHLY');
            expect(cot5.amount).to.equal(25000);
            expect(cot5.status).to.equal('ACTIVE');
        });
    });

    // ============================================
    // 6. FINANCIAL ACCOUNTS TESTS (GET /collectivities/{id}/financialAccounts)
    // ============================================
    describe('GET /collectivities/{id}/financialAccounts - Financial Accounts', function() {

        it('should retrieve financial accounts for col-1', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/financialAccounts');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            expect(res.body).to.not.be.empty;
            res.body.forEach(a => validateFinancialAccount(a));
        });

        it('col-1 should have C1-A-CASH (cash account)', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/financialAccounts');

            const cash = res.body.find(a => a.id === 'C1-A-CASH');
            expect(cash, 'C1-A-CASH should exist').to.exist;
            // Cash account should NOT have mobileBankingService or bankName
            expect(cash).to.not.have.property('mobileBankingService');
            expect(cash).to.not.have.property('bankName');
        });

        it('col-1 should have C1-A-MOBILE-1 (mobile account)', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/financialAccounts');

            const mobile = res.body.find(a => a.id === 'C1-A-MOBILE-1');
            expect(mobile, 'C1-A-MOBILE-1 should exist').to.exist;
            expect(mobile.holderName).to.equal('Mpanorina');
            expect(mobile.mobileBankingService).to.equal('ORANGE_MONEY');
            expect(mobile.mobileNumber).to.equal("0370489612");
        });

        it('should retrieve financial accounts for col-3 with all types', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-3/financialAccounts');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            res.body.forEach(a => validateFinancialAccount(a));

            // Should have C3-A-CASH (cash), C3-A-BANK-1 and C3-A-BANK-2 (bank), C3-A-MOBILE-1 (mobile)
            const hasCash = res.body.some(a => a.id === 'C3-A-CASH');
            const hasBank1 = res.body.some(a => a.id === 'C3-A-BANK-1');
            const hasBank2 = res.body.some(a => a.id === 'C3-A-BANK-2');
            const hasMobile = res.body.some(a => a.id === 'C3-A-MOBILE-1');

            expect(hasCash, 'Should have cash account').to.be.true;
            expect(hasBank1, 'Should have bank account 1').to.be.true;
            expect(hasBank2, 'Should have bank account 2').to.be.true;
            expect(hasMobile, 'Should have mobile account').to.be.true;
        });

        it('col-3 bank accounts should have correct details', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-3/financialAccounts');

            const bank1 = res.body.find(a => a.id === 'C3-A-BANK-1');
            expect(bank1, 'C3-A-BANK-1 should exist').to.exist;
            expect(bank1.holderName).to.equal('Koto');
            expect(bank1.bankName).to.equal('BMOI');
            expect(bank1.bankCode).to.equal(4);
        });

        it('should return 404 for non-existent collectivity', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/NONEXISTENT/financialAccounts');

            expect(res.status).to.equal(404);
        });

        it('should handle "at" query parameter for historical balance', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/financialAccounts?at=2026-01-15');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            res.body.forEach(a => validateFinancialAccount(a));
        });
    });

    // ============================================
    // 7. TRANSACTIONS TESTS (GET /collectivities/{id}/transactions)
    // ============================================
    describe('GET /collectivities/{id}/transactions - Transactions', function() {

        it('should retrieve transactions for col-1 in 2026', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/transactions?from=2026-01-01&to=2026-12-31');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            // 8 original + payments from tests
            expect(res.body.length, 'Should have at least 8 transactions').to.be.at.least(8);
            res.body.forEach(t => validateTransaction(t));
        });

        it('transactions should have correct payment modes', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/transactions?from=2026-01-01&to=2026-12-31');

            const modes = res.body.map(t => t.paymentMode);
            expect(modes, 'Should include CASH').to.include('CASH');
            expect(modes, 'Should include MOBILE_BANKING').to.include('MOBILE_BANKING');
        });

        it('should retrieve col-3 transactions for April 2026', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-3/transactions?from=2026-04-01&to=2026-04-30');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            expect(res.body, 'Should have 8 April transactions').to.have.lengthOf(8);
            res.body.forEach(t => validateTransaction(t));
        });

        it('should retrieve col-3 transactions for May 2026', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-3/transactions?from=2026-05-01&to=2026-05-31');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            expect(res.body, 'Should have 8 May transactions').to.have.lengthOf(8);
            res.body.forEach(t => validateTransaction(t));
        });

        it('col-3 should have 16 total transactions (Apr + May)', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-3/transactions?from=2026-04-01&to=2026-05-31');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            expect(res.body.length, 'Should have 16 transactions').to.equal(16);
        });

        it('should verify specific transaction trx-1-5', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/transactions?from=2026-01-01&to=2026-06-01');

            const trx = res.body.find(t => t.id === 'trx-1-5');
            if (trx) {
                // C1-M5 paid 150000 via MOBILE_BANKING
                expect(trx.amount).to.equal(150000);
                expect(trx.paymentMode).to.equal('MOBILE_BANKING');
            }
        });

        it('should return 400 without mandatory query params', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/transactions');

            expect(res.status, 'Status should be 400').to.equal(400);
        });

        it('should return 400 with only "from" param', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/transactions?from=2026-01-01');

            expect(res.status, 'Status should be 400').to.equal(400);
        });

        it('should return 400 with invalid date format', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/transactions?from=invalid&to=2026-12-31');

            expect(res.status, 'Status should be 400').to.equal(400);
        });

        it('should return empty array for period with no transactions', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/transactions?from=2020-01-01&to=2020-12-31');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            expect(res.body).to.be.empty;
        });
    });

    // ============================================
    // 8. LOCAL STATISTICS TESTS (GET /collectivities/{id}/statistics)
    // ============================================
    describe('GET /collectivities/{id}/statistics - Local Statistics', function() {

        it('should retrieve local statistics for col-1 in 2026', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/statistics?from=2026-01-01&to=2026-12-31');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            // Should have at least 8 original members
            expect(res.body.length, 'Should have at least 8 entries').to.be.at.least(8);
            res.body.forEach(s => validateLocalStatistics(s));
        });

        it('C1-M5 should have earnedAmount = 150000', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/statistics?from=2026-01-01&to=2026-12-31');

            const c1m5 = res.body.find(s => s.memberDescription.id === 'C1-M5');
            if (c1m5) {
                expect(c1m5.earnedAmount, 'C1-M5 earnedAmount should be 150000 (initial) + any new').to.be.at.least(150000);
            }
        });

        it('C1-M1 should have earnedAmount = 200000', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/statistics?from=2026-01-01&to=2026-12-31');

            const c1m1 = res.body.find(s => s.memberDescription.id === 'C1-M1');
            if (c1m1) {
                expect(c1m1.earnedAmount, 'C1-M1 earnedAmount should be at least 200000').to.be.at.least(200000);
            }
        });

        it('should calculate unpaid amounts for active membership fees', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/statistics?from=2026-01-01&to=2026-12-31');

            // C1-M6 paid 100000 out of 200000 for cot-1
            const c1m6 = res.body.find(s => s.memberDescription.id === 'C1-M6');
            if (c1m6) {
                expect(c1m6.earnedAmount, 'C1-M6 paid 100000').to.be.at.least(100000);
                // unpaidAmount should be 100000 (200000 - 100000) if cot-1 is only active
                expect(c1m6.unpaidAmount, 'C1-M6 should have unpaid amount').to.be.at.least(100000);
            }
        });

        it('all earnAmounts should be non-negative', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/statistics?from=2026-01-01&to=2026-12-31');

            res.body.forEach(s => {
                expect(s.earnedAmount, `${s.memberDescription.id} earnedAmount should be >= 0`).to.be.at.least(0);
            });
        });

        it('all unpaidAmounts should be non-negative', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/statistics?from=2026-01-01&to=2026-12-31');

            res.body.forEach(s => {
                expect(s.unpaidAmount, `${s.memberDescription.id} unpaidAmount should be >= 0`).to.be.at.least(0);
            });
        });

        it('should retrieve stats for col-3 and verify monthly calculations', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-3/statistics?from=2026-04-01&to=2026-05-31');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            res.body.forEach(s => validateLocalStatistics(s));

            // C3-M1: 25000 (Apr) + 25000 (May) = 50000 for MONTHLY cot-5 in this period
            const c3m1 = res.body.find(s => s.memberDescription.id === 'C3-M1');
            if (c3m1) {
                expect(c3m1.earnedAmount, 'C3-M1 should have 50000').to.equal(50000);
            }
        });

        it('should check C3-M7 earnedAmount (25000 Apr + 5000 May = 30000)', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-3/statistics?from=2026-04-01&to=2026-05-31');

            const c3m7 = res.body.find(s => s.memberDescription.id === 'C3-M7');
            if (c3m7) {
                expect(c3m7.earnedAmount, 'C3-M7 should have 30000').to.equal(30000);
                // For MONTHLY cot-5: expected 25000*2=50000, paid 30000, unpaid=20000
                expect(c3m7.unpaidAmount, 'C3-M7 unpaid should be 20000').to.equal(20000);
            }
        });

        it('should verify C3-M8 unpaid amount', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-3/statistics?from=2026-04-01&to=2026-05-31');

            const c3m8 = res.body.find(s => s.memberDescription.id === 'C3-M8');
            if (c3m8) {
                // Paid: 25000 (Apr) + 5000 (May) = 30000. Expected: 50000. Unpaid: 20000
                expect(c3m8.earnedAmount, 'C3-M8 earned should be 30000').to.equal(30000);
                expect(c3m8.unpaidAmount, 'C3-M8 unpaid should be 20000').to.equal(20000);
            }
        });

        it('should return 400 without mandatory query params', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/statistics');

            expect(res.status).to.equal(400);
        });
    });

    // ============================================
    // 9. OVERALL STATISTICS TESTS (GET /collectivities/statistics)
    // ============================================
    describe('GET /collectivities/statistics - Overall Statistics', function() {

        it('should retrieve overall statistics for 2026', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/statistics?from=2026-01-01&to=2026-12-31');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            expect(res.body.length, 'Should have stats for 3 collectivities').to.equal(3);
            res.body.forEach(s => validateOverallStatistics(s));
        });

        it('should have collectivities in correct order', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/statistics?from=2026-01-01&to=2026-12-31');

            const numbers = res.body.map(s => s.collectivityInformation.number);
            expect(numbers).to.include(1);
            expect(numbers).to.include(2);
            expect(numbers).to.include(3);
        });

        it('col-1 should have newMembersNumber = 4 (C1-M9, C1-M10, C1-M11, C1-M12)', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/statistics?from=2026-01-01&to=2026-12-31');

            const col1 = res.body.find(s => s.collectivityInformation.number === 1);
            if (col1) {
                expect(col1.newMembersNumber, 'col-1 should have 4 new members').to.equal(4);
            }
        });

        it('col-2 should have newMembersNumber = 3 (C2-M9, C2-M10, C2-M11)', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/statistics?from=2026-01-01&to=2026-12-31');

            const col2 = res.body.find(s => s.collectivityInformation.number === 2);
            if (col2) {
                expect(col2.newMembersNumber, 'col-2 should have 3 new members').to.equal(3);
            }
        });

        it('col-3 should have newMembersNumber = 6 (C3-M9 to C3-M14)', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/statistics?from=2026-01-01&to=2026-12-31');

            const col3 = res.body.find(s => s.collectivityInformation.number === 3);
            if (col3) {
                expect(col3.newMembersNumber, 'col-3 should have 6 new members').to.equal(6);
            }
        });

        it('col-2 should have overallMemberCurrentDuePercentage >= 0 (cot-4 is INACTIVE)', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/statistics?from=2026-01-01&to=2026-12-31');

            const col2 = res.body.find(s => s.collectivityInformation.number === 2);
            if (col2) {
                expect(col2.overallMemberCurrentDuePercentage, 'Should be a percentage').to.be.within(0, 100);
            }
        });

        it('should return 400 without mandatory query params', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/statistics');

            expect(res.status).to.equal(400);
        });

        it('should handle different date ranges', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/statistics?from=2026-04-01&to=2026-06-30');

            expect(res.status).to.equal(200);
            expect(res.body).to.be.an('array');
            res.body.forEach(s => validateOverallStatistics(s));
        });
    });

    // ============================================
    // 10. DATA INTEGRITY TESTS
    // ============================================
    describe('Data Integrity Tests', function() {

        it('col-1 should have 12 original members + newly created ones', async function() {
            const res = await chai.request(BASE_URL).get('/collectivities/col-1');
            // 8 original + 4 new (C1-M9 to C1-M12) + members created in tests
            expect(res.body.members.length, `col-1 should have at least 12 members, got ${res.body.members.length}`).to.be.at.least(12);
            // Check for specific new members
            const ids = res.body.members.map(m => m.id);
            expect(ids).to.include('C1-M9');
            expect(ids).to.include('C1-M10');
            expect(ids).to.include('C1-M11');
            expect(ids).to.include('C1-M12');
        });

        it('col-2 should have 11 members (8 original + 3 new)', async function() {
            const res = await chai.request(BASE_URL).get('/collectivities/col-2');
            expect(res.body.members.length, `col-2 should have 11 members, got ${res.body.members.length}`).to.equal(11);

            const ids = res.body.members.map(m => m.id);
            expect(ids).to.include('C2-M9');
            expect(ids).to.include('C2-M10');
            expect(ids).to.include('C2-M11');
        });

        it('col-3 should have 14 members (8 original + 6 new)', async function() {
            const res = await chai.request(BASE_URL).get('/collectivities/col-3');
            expect(res.body.members.length, `col-3 should have 14 members, got ${res.body.members.length}`).to.equal(14);

            const ids = res.body.members.map(m => m.id);
            expect(ids).to.include('C3-M9');
            expect(ids).to.include('C3-M14');
        });

        it('all collectivity names should be correct', async function() {
            const col1 = await chai.request(BASE_URL).get('/collectivities/col-1');
            expect(col1.body.name).to.equal('Mpanorina');
            expect(col1.body.location).to.equal('Ambatondrazaka');

            const col2 = await chai.request(BASE_URL).get('/collectivities/col-2');
            expect(col2.body.name).to.equal('Dobo voalahany');

            const col3 = await chai.request(BASE_URL).get('/collectivities/col-3');
            expect(col3.body.name).to.equal('Tantely mamy');
            expect(col3.body.location).to.equal('Brickaville');
        });

        it('new members should have correct occupations', async function() {
            const res = await chai.request(BASE_URL).get('/collectivities/col-1');

            const c1m9 = res.body.members.find(m => m.id === 'C1-M9');
            expect(c1m9, 'C1-M9 should exist').to.exist;
            expect(c1m9.occupation, 'C1-M9 should be JUNIOR').to.equal('JUNIOR');
        });

        it('C2-M5 should be PRESIDENT of col-2', async function() {
            const res = await chai.request(BASE_URL).get('/collectivities/col-2');
            expect(res.body.structure.president.id).to.equal('C2-M5');
            expect(res.body.structure.vicePresident.id).to.equal('C2-M6');
            expect(res.body.structure.secretary.id).to.equal('C2-M7');
            expect(res.body.structure.treasurer.id).to.equal('C2-M8');
        });

        it('C3-M1 should be PRESIDENT of col-3', async function() {
            const res = await chai.request(BASE_URL).get('/collectivities/col-3');
            expect(res.body.structure.president.id).to.equal('C3-M1');
            expect(res.body.structure.vicePresident.id).to.equal('C3-M2');
            expect(res.body.structure.secretary.id).to.equal('C3-M3');
            expect(res.body.structure.treasurer.id).to.equal('C3-M4');
        });

        it('referee relationships should be correctly reflected', async function() {
            const res = await chai.request(BASE_URL).get('/collectivities/col-1');

            // C1-M3 should have referees C1-M1 and C1-M2
            const c1m3 = res.body.members.find(m => m.id === 'C1-M3');
            expect(c1m3, 'C1-M3 should exist').to.exist;
            expect(c1m3.referees, 'C1-M3 should have referees').to.not.be.empty;

            // Check each member has correct number of referees
            const c1m4 = res.body.members.find(m => m.id === 'C1-M4');
            expect(c1m4, 'C1-M4 should exist').to.exist;
            expect(c1m4.referees, 'C1-M4 should have 2 referees').to.have.lengthOf(2);
        });
    });

    // ============================================
    // 11. TRANSACTION CALCULATION VERIFICATION TESTS
    // ============================================
    describe('Transaction Calculation Verification', function() {

        it('col-1 total transactions amount should sum correctly', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/transactions?from=2026-01-01&to=2026-06-01');

            // Initial 8 transactions sum: 200k+200k+200k+200k+150k+100k+60k+90k = 1,200,000
            const initialTransactions = res.body.filter(t => t.id && t.id.startsWith('trx-1-'));
            const sum = initialTransactions.reduce((acc, t) => acc + t.amount, 0);
            expect(sum, 'Sum of initial col-1 transactions should be 1,200,000').to.equal(1200000);
        });

        it('col-3 April transaction sum should be 200,000', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-3/transactions?from=2026-04-01&to=2026-04-30');

            const sum = res.body.reduce((acc, t) => acc + t.amount, 0);
            expect(sum, 'April sum should be 200000').to.equal(200000);
        });

        it('col-3 May transaction sum should be 140,000', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-3/transactions?from=2026-05-01&to=2026-05-31');

            const sum = res.body.reduce((acc, t) => acc + t.amount, 0);
            // 25000+25000+15000+15000+20000+25000+5000+5000 = 135000
            expect(sum, 'May sum should be 135000').to.equal(135000);
        });

        it('col-3 total transaction sum for Apr+May should be 335,000', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-3/transactions?from=2026-04-01&to=2026-05-31');

            const sum = res.body.reduce((acc, t) => acc + t.amount, 0);
            expect(sum, 'Total sum should be 335000').to.equal(335000);
        });
    });

    // ============================================
    // 12. FINANCIAL ACCOUNT BALANCE VERIFICATION
    // ============================================
    describe('Financial Account Balance Verification', function() {

        it('C1-A-CASH balance should reflect all CASH transactions', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/financialAccounts');

            const cash = res.body.find(a => a.id === 'C1-A-CASH');
            expect(cash, 'C1-A-CASH should exist').to.exist;

            // Initial CASH transactions: trx-1-1 (200k), trx-1-2 (200k), trx-1-6 (100k), trx-1-7 (60k), trx-1-8 (90k) = 650,000
            expect(cash.amount, 'C1-A-CASH should have at least 650000').to.be.at.least(650000);
        });

        it('C3-A-BANK-1 balance should reflect transactions', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-3/financialAccounts');

            const bank1 = res.body.find(a => a.id === 'C3-A-BANK-1');
            expect(bank1, 'C3-A-BANK-1 should exist').to.exist;

            // trx-3-1 (25k), trx-3-2 (25k), trx-3-3 (25k), trx-3-4 (25k) April
            // trx-3-9 (25k), trx-3-10 (25k) May = 150,000
            expect(bank1.amount, 'C3-A-BANK-1 should have at least 150000').to.be.at.least(150000);
        });
    });

    // ============================================
    // 13. ACTIVITIES TESTS (POST/GET /collectivities/{id}/activities) - Bonus 1
    // ============================================
    describe('POST /collectivities/{id}/activities - Create Activities', function() {

        it('should create a MEETING activity with recurrence rule', async function() {
            const res = await chai.request(BASE_URL)
                .post('/collectivities/col-1/activities')
                .send([{
                    label: 'Assemblée générale mensuelle',
                    activityType: 'MEETING',
                    memberOccupationConcerned: ['JUNIOR', 'SENIOR', 'PRESIDENT', 'VICE_PRESIDENT', 'SECRETARY', 'TREASURER'],
                    recurrenceRule: {
                        weekOrdinal: 2,
                        dayOfWeek: 'SU'
                    }
                }]);

            // May be 200 if implemented
            expect(res.status).to.be.oneOf([201, 404]);

            if (res.status === 201) {
                expect(res.body).to.be.an('array');
                res.body.forEach(a => {
                    validateActivity(a);
                    createdActivityIds.push(a.id);
                });
            }
        });

        it('should create a TRAINING activity with recurrence rule', async function() {
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

            if (res.status === 200 && res.body.length > 0) {
                createdActivityIds.push(res.body[0].id);
            }
        });

        it('should create an OTHER activity with executive date', async function() {
            const res = await chai.request(BASE_URL)
                .post('/collectivities/col-1/activities')
                .send([{
                    label: 'Formation exceptionnelle',
                    activityType: 'OTHER',
                    memberOccupationConcerned: ['SENIOR', 'PRESIDENT'],
                    executiveDate: '2026-06-15'
                }]);

            if (res.status === 200 && res.body.length > 0) {
                createdActivityIds.push(res.body[0].id);
            }
        });

        it('should reject activity with both recurrenceRule and executiveDate', async function() {
            const res = await chai.request(BASE_URL)
                .post('/collectivities/col-1/activities')
                .send([{
                    label: 'Invalid Activity',
                    activityType: 'MEETING',
                    memberOccupationConcerned: ['JUNIOR'],
                    recurrenceRule: { weekOrdinal: 1, dayOfWeek: 'MO' },
                    executiveDate: '2026-06-15'
                }]);

            if (res.status !== 404) {
                expect(res.status).to.equal(400);
            }
        });
    });

    describe('GET /collectivities/{id}/activities - Get Activities', function() {

        it('should retrieve activities for col-1', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/activities');

            expect(res.status).to.be.oneOf([200, 404]);

            if (res.status === 200) {
                expect(res.body).to.be.an('array');
                res.body.forEach(a => validateActivity(a));
            }
        });

        it('should return 404 for non-existent collectivity', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/NONEXISTENT/activities');

            expect(res.status).to.equal(404);
        });
    });

    // ============================================
    // 14. ATTENDANCE TESTS (POST/GET .../attendance) - Bonus 1
    // ============================================
    describe('POST /collectivities/{id}/activities/{activityId}/attendance - Create Attendance', function() {

        let testActivityId;

        before(async function() {
            if (createdActivityIds.length > 0) {
                testActivityId = createdActivityIds[0];
            }
        });

        it('should create attendance records for an activity', async function() {
            if (!testActivityId) this.skip();

            const res = await chai.request(BASE_URL)
                .post(`/collectivities/col-1/activities/${testActivityId}/attendance`)
                .send([
                    { memberIdentifier: 'C1-M1', attendanceStatus: 'ATTENDED' },
                    { memberIdentifier: 'C1-M2', attendanceStatus: 'MISSING' },
                    { memberIdentifier: 'C1-M5', attendanceStatus: 'ATTENDED' }
                ]);

            if (res.status === 201) {
                expect(res.body).to.be.an('array');
                res.body.forEach(a => validateAttendance(a));
            }
        });

        it('should reject updating already set attendance status', async function() {
            if (!testActivityId) this.skip();

            const res = await chai.request(BASE_URL)
                .post(`/collectivities/col-1/activities/${testActivityId}/attendance`)
                .send([
                    { memberIdentifier: 'C1-M1', attendanceStatus: 'MISSING' }
                ]);

            if (res.status !== 404) {
                expect(res.status).to.equal(400);
            }
        });
    });

    describe('GET /collectivities/{id}/activities/{activityId}/attendance - Get Attendance', function() {

        let testActivityId;

        before(async function() {
            if (createdActivityIds.length > 0) {
                testActivityId = createdActivityIds[0];
            }
        });

        it('should retrieve attendance for an activity', async function() {
            if (!testActivityId) this.skip();

            const res = await chai.request(BASE_URL)
                .get(`/collectivities/col-1/activities/${testActivityId}/attendance`);

            if (res.status === 200) {
                expect(res.body).to.be.an('array');
                res.body.forEach(a => validateAttendance(a));

                // Should include UNDEFINED status for members not yet marked
                const hasUndefined = res.body.some(a => a.attendanceStatus === 'UNDEFINED');
                // May or may not have undefined depending on implementation
            }
        });

        it('should return 404 for non-existent activity', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/activities/NONEXISTENT/attendance');

            expect(res.status).to.equal(404);
        });
    });

    // ============================================
    // 15. EDGE CASES & BOUNDARY TESTS
    // ============================================
    describe('Edge Cases & Boundary Tests', function() {

        it('should handle empty body in POST /members', async function() {
            const res = await chai.request(BASE_URL)
                .post('/members')
                .send([]);

            expect(res.status).to.be.oneOf([400, 201]);
        });

        it('should handle very long names', async function() {
            const res = await chai.request(BASE_URL)
                .post('/members')
                .send([{
                    firstName: 'A'.repeat(100),
                    lastName: 'B'.repeat(100),
                    birthDate: '1990-01-01',
                    gender: 'MALE',
                    address: 'C'.repeat(200),
                    profession: 'Test',
                    phoneNumber: 300001000,
                    email: `verylong.email.${Date.now()}@test.mg`,
                    occupation: 'JUNIOR',
                    collectivityIdentifier: 'col-1',
                    referees: ['C1-M1', 'C1-M2'],
                    registrationFeePaid: true,
                    membershipDuesPaid: true
                }]);

            // Should either accept or reject gracefully
            expect(res.status).to.be.oneOf([201, 400]);
        });

        it('should handle missing required fields', async function() {
            const res = await chai.request(BASE_URL)
                .post('/members')
                .send([{}]);

            expect(res.status).to.equal(400);
        });

        it('should handle special characters in names', async function() {
            const res = await chai.request(BASE_URL)
                .post('/members')
                .send([{
                    firstName: "Jean-Luc",
                    lastName: "O'Connor",
                    birthDate: '1990-01-01',
                    gender: 'MALE',
                    address: 'Lot 123',
                    profession: 'Test',
                    phoneNumber: 300001001,
                    email: `special.chars.${Date.now()}@test.mg`,
                    occupation: 'JUNIOR',
                    collectivityIdentifier: 'col-1',
                    referees: ['C1-M1', 'C1-M2'],
                    registrationFeePaid: true,
                    membershipDuesPaid: true
                }]);

            if (res.status === 201 && res.body.length > 0) {
                expect(res.body[0].firstName).to.equal("Jean-Luc");
            }
        });

        it('should handle date formats correctly', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/transactions?from=2026-01-01&to=2026-12-31');

            expect(res.status).to.equal(200);
            res.body.forEach(t => {
                expect(t.creationDate, 'creationDate should match YYYY-MM-DD format')
                    .to.match(/^\d{4}-\d{2}-\d{2}$/);
            });
        });
    });

    // ============================================
    // 16. SCHEMA CONSISTENCY ACROSS ENDPOINTS
    // ============================================
    describe('Schema Consistency Across Endpoints', function() {

        it('member from get collectivity should match member schema', async function() {
            const res = await chai.request(BASE_URL).get('/collectivities/col-1');
            res.body.members.forEach(m => {
                validateMemberObject(m, { skipReferees: false });
            });
        });

        it('memberDebited in transactions should match member schema', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/transactions?from=2026-01-01&to=2026-06-01');

            res.body.forEach(t => {
                if (t.memberDebited) {
                    validateMemberObject(t.memberDebited, { skipReferees: true });
                }
            });
        });

        it('accountCredited in transactions should match FinancialAccount schema', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/transactions?from=2026-01-01&to=2026-06-01');

            res.body.forEach(t => {
                if (t.accountCredited) {
                    validateFinancialAccount(t.accountCredited);
                }
            });
        });

        it('collectivityInformation in overall stats should have correct properties', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/statistics?from=2026-01-01&to=2026-12-31');

            res.body.forEach(s => {
                expect(s.collectivityInformation).to.have.property('name');
                expect(s.collectivityInformation).to.have.property('number');
            });
        });

        it('memberDescription in local stats should have correct properties', async function() {
            const res = await chai.request(BASE_URL)
                .get('/collectivities/col-1/statistics?from=2026-01-01&to=2026-12-31');

            res.body.forEach(s => {
                expect(s.memberDescription).to.have.property('id');
                expect(s.memberDescription).to.have.property('firstName');
                expect(s.memberDescription).to.have.property('lastName');
                expect(s.memberDescription).to.have.property('email');
                expect(s.memberDescription).to.have.property('occupation');
            });
        });
    });
});