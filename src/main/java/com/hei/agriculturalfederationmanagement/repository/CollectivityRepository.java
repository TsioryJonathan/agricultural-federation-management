package com.hei.agriculturalfederationmanagement.repository;

import com.hei.agriculturalfederationmanagement.entity.*;
import com.hei.agriculturalfederationmanagement.entity.enums.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@AllArgsConstructor
public class CollectivityRepository {
    private final Connection connection;

    public Collectivity save(Collectivity collectivity, List<String> memberIds,
                                                    String presidentId, String vicePresidentId,
                                                    String treasurerId, String secretaryId) {
        String insertCollectivitySql = """
        insert into collectivity (number, name, speciality,federation_approval, authorization_date, location, id_federation, creation_datetime)
        values (?, ?, ?, ?, ?, ?, 1, now())
        returning id
    """;

        String insertMemberSql = """
            insert into member_collectivity (id_member, id_collectivity, occupation, start_date)
            values (?, ?, ?::collectivity_occupation, ?)
        """;

        try {
            connection.setAutoCommit(false);

            String collectivityId;
            try (PreparedStatement stmt = connection.prepareStatement(insertCollectivitySql)) {
                stmt.setString(1, collectivity.getNumber());
                stmt.setString(2, collectivity.getName());
                stmt.setString(3, collectivity.getSpeciality());
                stmt.setBoolean(4, collectivity.isFederationApproval());
                stmt.setTimestamp(5, collectivity.getAuthorizationDate() != null ?
                        Timestamp.from(collectivity.getAuthorizationDate()) : null);
                stmt.setString(6, collectivity.getLocation());

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    collectivityId = rs.getString("id");
                } else {
                    throw new SQLException("Failed to insert collectivity, no ID returned");
                }
            }

            try (PreparedStatement memberStmt = connection.prepareStatement(insertMemberSql)) {
                Timestamp now = Timestamp.from(Instant.now());

                for (String memberId : memberIds) {
                    String occupation = determineOccupation(memberId, presidentId, vicePresidentId,
                            treasurerId, secretaryId);

                    memberStmt.setString(1, memberId);
                    memberStmt.setString(2, collectivityId);
                    memberStmt.setObject(3, occupation, Types.OTHER);
                    memberStmt.setTimestamp(4, now);
                    memberStmt.addBatch();
                }
                memberStmt.executeBatch();
            }

            connection.commit();

            return findById(collectivityId);

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new RuntimeException("Failed to rollback transaction", rollbackEx);
            }
            throw new RuntimeException("Failed to save collectivity with members", e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.err.println("Failed to reset auto-commit");
            }
        }
    }

    private String determineOccupation(String memberId, String presidentId, String vicePresidentId,
                                       String treasurerId, String secretaryId) {
        if (memberId.equals(presidentId)) return "PRESIDENT";
        if (memberId.equals(vicePresidentId)) return "VICE_PRESIDENT";
        if (memberId.equals(treasurerId)) return "TREASURER";
        if (memberId.equals(secretaryId)) return "SECRETARY";

        if (hasMinimumSeniority(memberId)) {
            return "SENIOR";
        }
        return "JUNIOR";
    }

    private boolean hasMinimumSeniority(String memberId) {
        String sql = """
            select enrolment_date from member where id = ?
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, memberId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Timestamp enrolmentDate = rs.getTimestamp("enrolment_date");
                long monthsBetween = ChronoUnit.MONTHS.between(
                        enrolmentDate.toLocalDateTime(), LocalDateTime.now());
                return monthsBetween >= 6;
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check member seniority", e);
        }
    }

    public Collectivity findById(String id) {
        String collectivitySql = """
            select id, number, name, speciality, creation_datetime, 
                   federation_approval, authorization_date, location
            from collectivity
            where id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(collectivitySql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Collectivity collectivity = Collectivity.builder()
                        .id(rs.getString("id"))
                        .number(rs.getString("number"))
                        .name(rs.getString("name"))
                        .speciality(rs.getString("speciality"))
                        .creationDatetime(rs.getTimestamp("creation_datetime").toInstant())
                        .federationApproval(rs.getBoolean("federation_approval"))
                        .authorizationDate(rs.getTimestamp("authorization_date") != null ?
                                rs.getTimestamp("authorization_date").toInstant() : null)
                        .location(rs.getString("location"))
                        .build();

                fetchMembersAndStructure(collectivity);

                return collectivity;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find collectivity " + e.getMessage());
        }
    }

    public Optional<Collectivity> findByIdOptional(String id) {
        String collectivitySql = """
        select id, number, name, speciality, creation_datetime,
               federation_approval, authorization_date, location
        from collectivity
        where id = ?
    """;

        try (PreparedStatement stmt = connection.prepareStatement(collectivitySql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Collectivity collectivity = Collectivity.builder()
                        .id(rs.getString("id"))
                        .number(rs.getString("number"))
                        .name(rs.getString("name"))
                        .speciality(rs.getString("speciality"))
                        .creationDatetime(rs.getTimestamp("creation_datetime").toInstant())
                        .federationApproval(rs.getBoolean("federation_approval"))
                        .authorizationDate(rs.getTimestamp("authorization_date") != null ?
                                rs.getTimestamp("authorization_date").toInstant() : null)
                        .location(rs.getString("location"))
                        .build();

                fetchMembersAndStructure(collectivity);

                return Optional.of(collectivity);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find collectivity", e);
        }
    }

    private void fetchMembersAndStructure(Collectivity collectivity) {
        String sql = """
        select
            m.id, m.first_name, m.last_name, m.birth_date, m.enrolment_date,
            m.address, m.email, m.phone_number, m.profession, m.gender,
            mc.occupation
        from member_collectivity mc
        join member m on mc.id_member = m.id
        where mc.id_collectivity = ? AND mc.end_date is null
    """;

        List<Member> members = new ArrayList<>();
        Structure structure = Structure.builder().build();
        Map<String, Member> memberCache = new HashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, collectivity.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String memberId = rs.getString("id");

                Member member = memberCache.computeIfAbsent(memberId, id -> {
                    try {
                        return Member.builder()
                                .id(id)
                                .firstName(rs.getString("first_name"))
                                .lastName(rs.getString("last_name"))
                                .birthDate(rs.getDate("birth_date").toLocalDate())
                                .enrolmentDate(rs.getTimestamp("enrolment_date").toInstant())
                                .address(rs.getString("address"))
                                .email(rs.getString("email"))
                                .phoneNumber(rs.getString("phone_number"))
                                .profession(rs.getString("profession"))
                                .gender(Gender.valueOf(rs.getString("gender")))
                                .referees(new ArrayList<>())
                                .build();
                    } catch (SQLException e) {
                        throw new RuntimeException("Failed to map member", e);
                    }
                });

                members.add(member);

                String occupation = rs.getString("occupation");
                switch (CollectivityOccupation.valueOf(occupation)) {
                    case PRESIDENT -> structure.setPresident(member);
                    case VICE_PRESIDENT -> structure.setVicePresident(member);
                    case TREASURER -> structure.setTreasurer(member);
                    case SECRETARY -> structure.setSecretary(member);
                }
            }

            loadRefereesForMembers(members);

            collectivity.setMembers(members);
            collectivity.setStructure(structure);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch members and structure", e);
        }
    }

    private void loadRefereesForMembers(List<Member> members) {
        if (members == null || members.isEmpty()) {
            return;
        }

        Map<String, Member> memberMap = members.stream()
                .collect(Collectors.toMap(Member::getId, m -> m));

        List<String> memberIds = members.stream()
                .map(Member::getId)
                .toList();

        String placeholders = memberIds.stream()
                .map(id -> "?")
                .collect(Collectors.joining(","));

        String sql = """
        select
            mr.id_candidate,
            mr.id_referee,
            m.first_name,
            m.last_name,
            m.email,
            m.phone_number,
            m.gender
        from member_referee mr
        join member m ON mr.id_referee = m.id
        where mr.id_candidate in (%s)
    """.formatted(placeholders);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < memberIds.size(); i++) {
                stmt.setString(i + 1, memberIds.get(i));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String candidateId = rs.getString("id_candidate");
                String refereeId = rs.getString("id_referee");

                Member candidate = memberMap.get(candidateId);
                if (candidate != null) {
                    Member referee = Member.builder()
                            .id(refereeId)
                            .firstName(rs.getString("first_name"))
                            .lastName(rs.getString("last_name"))
                            .email(rs.getString("email"))
                            .phoneNumber(rs.getString("phone_number"))
                            .gender(Gender.valueOf(rs.getString("gender")))
                            .build();

                    candidate.getReferees().add(referee);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load referees for members", e);
        }
    }


    public List<Collectivity> saveAll(List<Collectivity> collectivities,
                                                 List<List<String>> memberIdsList,
                                                 List<String> presidentIds,
                                                 List<String> vicePresidentIds,
                                                 List<String> treasurerIds,
                                                 List<String> secretaryIds) {
        List<Collectivity> savedCollectivities = new ArrayList<>();

        for (int i = 0; i < collectivities.size(); i++) {
            Collectivity saved = save(
                    collectivities.get(i),
                    memberIdsList.get(i),
                    presidentIds.get(i),
                    vicePresidentIds.get(i),
                    treasurerIds.get(i),
                    secretaryIds.get(i)
            );
            savedCollectivities.add(saved);
        }

        return savedCollectivities;
    }

    public boolean existsByNumber(String number) {
        if (number == null) return false;
        String sql = "select count(id) from collectivity where number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, number);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check number existence", e);
        }
    }

    public boolean existsByName(String name) {
        if (name == null) return false;
        String sql = "select count(id) from collectivity where name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check name existence", e);
        }
    }

    public void assignIdentity(String id, String number, String name) {
        String updateSql = "update collectivity set number = ?, name = ? where id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
            stmt.setString(1, number);
            stmt.setString(2, name);
            stmt.setString(3, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to assign identity", e);
        }
    }


    public List<Transaction> findTransactionsByCollectivityIdAndDateRange(
            String collectivityId,
            Instant from,
            Instant to) {

        Map<String, Account> accountMap = loadAccountsWithAllTransactions(collectivityId);

        String transactionSql = """
        select
            t.id,
            t.amount,
            t.transaction_date,
            t.payment_mode,
            t.id_account,
            t.id_member,
            m.first_name,
            m.last_name,
            m.birth_date,
            m.enrolment_date,
            m.address,
            m.email,
            m.phone_number as member_phone,
            m.profession,
            m.gender
        from transaction t
        join member m on t.id_member = m.id
        where t.id_collectivity = ?
          and t.transaction_type = 'IN'
          and t.transaction_date >= ?
          and t.transaction_date < ?
        order by t.transaction_date desc
    """;

        List<Transaction> transactions = new ArrayList<>();
        Map<String, Member> memberCache = new HashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(transactionSql)) {
            stmt.setString(1, collectivityId);
            stmt.setTimestamp(2, Timestamp.from(from));
            stmt.setTimestamp(3, Timestamp.from(to));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Transaction transaction = Transaction.builder()
                        .id(rs.getString("id"))
                        .amount(rs.getBigDecimal("amount").doubleValue())
                        .transactionDate(rs.getTimestamp("transaction_date").toInstant())
                        .paymentMode(rs.getString("payment_mode") != null ?
                                PaymentMode.valueOf(rs.getString("payment_mode")) : null)
                        .build();

                Account account = accountMap.get(rs.getString("id_account"));
                transaction.setAccount(account);

                String memberId = rs.getString("id_member");
                Member member = memberCache.computeIfAbsent(memberId, id -> {
                    try {
                        return Member.builder()
                                .id(id)
                                .firstName(rs.getString("first_name"))
                                .lastName(rs.getString("last_name"))
                                .birthDate(rs.getDate("birth_date").toLocalDate())
                                .enrolmentDate(rs.getTimestamp("enrolment_date").toInstant())
                                .address(rs.getString("address"))
                                .email(rs.getString("email"))
                                .phoneNumber(rs.getString("member_phone"))
                                .profession(rs.getString("profession"))
                                .gender(Gender.valueOf(rs.getString("gender")))
                                .referees(new ArrayList<>())
                                .build();
                    } catch (SQLException e) {
                        throw new RuntimeException("Failed to map member", e);
                    }
                });

                transaction.setMember(member);
                transactions.add(transaction);
            }

            if (!memberCache.isEmpty()) {
                List<Member> members = new ArrayList<>(memberCache.values());
                loadRefereesForMembers(members);
            }

            return transactions;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find transactions", e);
        }
    }

    public Map<String, Account> loadAccountsWithAllTransactions(String collectivityId) {
        return loadAccountsWithTransactions(collectivityId, null);
    }

    public Map<String, Account> loadAccountsWithTransactionsAt(String collectivityId, Instant at) {
        return loadAccountsWithTransactions(collectivityId, at);
    }

    private Map<String, Account> loadAccountsWithTransactions(String collectivityId, Instant at) {
        String baseSql = """
        select
            a.id as account_id,
            a.id_collectivity,
            a.id_federation,
            ca.id as cash_account_id,
            ba.id as bank_account_id,
            ba.holder_name as bank_holder_name,
            ba.bank_name,
            ba.bank_code,
            ba.branch_code,
            ba.account_number,
            ba.rib_key,
            ma.id as mobile_account_id,
            ma.holder_name as mobile_holder_name,
            ma.service_name,
            ma.phone_number,
            t.id as transaction_id,
            t.amount as transaction_amount,
            t.transaction_type,
            t.transaction_date,
            t.payment_mode,
            t.description
        from account a
        left join cash_account ca on a.id = ca.id_account
        left join bank_account ba on a.id = ba.id_account
        left join mobile_money_account ma on a.id = ma.id_account
        left join transaction t on a.id = t.id_account
        """;

        String whereClause = at != null 
            ? "where a.id_collectivity = ? and t.transaction_date <= ?"
            : "where a.id_collectivity = ?";

        String sql = baseSql + " " + whereClause + " order by a.id, t.transaction_date";

        Map<String, Account> accountMap = new HashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            if (at != null) {
                stmt.setTimestamp(2, Timestamp.from(at));
            }
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String accountId = rs.getString("account_id");

                Account account = accountMap.computeIfAbsent(accountId, id -> {
                    try {
                        Account newAccount = Account.builder()
                                .id(id)
                                .collectivity(findById(rs.getString("id_collectivity")))
                                .transactions(new ArrayList<>())
                                .build();

                        if (rs.getObject("cash_account_id") != null) {
                            CashAccount cashAccount = CashAccount.builder()
                                    .id(rs.getString("cash_account_id"))
                                    .account(newAccount)
                                    .build();
                            newAccount.setCashAccount(cashAccount);
                        } else if (rs.getObject("bank_account_id") != null) {
                            BankAccount bankAccount = BankAccount.builder()
                                    .id(rs.getString("bank_account_id"))
                                    .account(newAccount)
                                    .holderName(rs.getString("bank_holder_name"))
                                    .bankName(BankName.valueOf(rs.getString("bank_name")))
                                    .bankCode(rs.getString("bank_code"))
                                    .branchCode(rs.getString("branch_code"))
                                    .accountNumber(rs.getString("account_number"))
                                    .ribKey(rs.getString("rib_key"))
                                    .build();
                            newAccount.setBankAccount(bankAccount);
                        } else if (rs.getObject("mobile_account_id") != null) {
                            MobileMoneyAccount mobileAccount = MobileMoneyAccount.builder()
                                    .id(rs.getString("mobile_account_id"))
                                    .account(newAccount)
                                    .holderName(rs.getString("mobile_holder_name"))
                                    .serviceName(MobileMoneyService.valueOf(rs.getString("service_name")))
                                    .phoneNumber(rs.getString("phone_number"))
                                    .build();
                            newAccount.setMobileMoneyAccount(mobileAccount);
                        }

                        return newAccount;
                    } catch (SQLException e) {
                        throw new RuntimeException("Failed to build account", e);
                    }
                });

                String transactionId = rs.getString("transaction_id");
                if (transactionId != null) {
                    Transaction transaction = Transaction.builder()
                            .id(transactionId)
                            .amount(rs.getBigDecimal("transaction_amount").doubleValue())
                            .transactionType(TransactionType.valueOf(rs.getString("transaction_type")))
                            .transactionDate(rs.getTimestamp("transaction_date").toInstant())
                            .paymentMode(rs.getString("payment_mode") != null ?
                                    PaymentMode.valueOf(rs.getString("payment_mode")) : null)
                            .description(rs.getString("description"))
                            .build();

                    account.getTransactions().add(transaction);
                }
            }

            return accountMap;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load accounts with transactions", e);
        }
    }

    public Account findAccountById(String accountId) {
        String sql = """
        SELECT 
            a.id, a.id_collectivity, a.id_federation,
            ca.id as cash_account_id,
            ba.id as bank_account_id, ba.holder_name as bank_holder_name, ba.bank_name,
            ba.bank_code, ba.branch_code, ba.account_number, ba.rib_key,
            ma.id as mobile_account_id, ma.holder_name as mobile_holder_name,
            ma.service_name, ma.phone_number
        FROM account a
        LEFT JOIN cash_account ca ON a.id = ca.id_account
        LEFT JOIN bank_account ba ON a.id = ba.id_account
        LEFT JOIN mobile_money_account ma ON a.id = ma.id_account
        WHERE a.id = ?
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, accountId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return buildAccountFromResultSet(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find account", e);
        }
    }


    private Account buildAccountFromResultSet(ResultSet rs) throws SQLException {
        String accountId = rs.getString("id");
        String collectivityId = rs.getString("id_collectivity");
        String federationId = rs.getString("id_federation");

        Account account = Account.builder()
                .id(accountId)
                .collectivity(findById(collectivityId))
                .transactions(new ArrayList<>())
                .build();

        if (rs.getObject("cash_account_id") != null) {
            CashAccount cashAccount = CashAccount.builder()
                    .id(rs.getString("cash_account_id"))
                    .account(account)
                    .build();
            account.setCashAccount(cashAccount);

        } else if (rs.getObject("bank_account_id") != null) {
            BankAccount bankAccount = BankAccount.builder()
                    .id(rs.getString("bank_account_id"))
                    .account(account)
                    .holderName(rs.getString("bank_holder_name"))
                    .bankName(BankName.valueOf(rs.getString("bank_name")))
                    .bankCode(rs.getString("bank_code"))
                    .branchCode(rs.getString("branch_code"))
                    .accountNumber(rs.getString("account_number"))
                    .ribKey(rs.getString("rib_key"))
                    .build();
            account.setBankAccount(bankAccount);

        } else if (rs.getObject("mobile_account_id") != null) {
            MobileMoneyAccount mobileAccount = MobileMoneyAccount.builder()
                    .id(rs.getString("mobile_account_id"))
                    .account(account)
                    .holderName(rs.getString("mobile_holder_name"))
                    .serviceName(MobileMoneyService.valueOf(rs.getString("service_name")))
                    .phoneNumber(rs.getString("phone_number"))
                    .build();
            account.setMobileMoneyAccount(mobileAccount);
        }

        loadTransactionsForAccount(account);

        return account;
    }

    private void loadTransactionsForAccount(Account account) {
        String sql = """
        select
            id, amount, transaction_type, transaction_date, 
            payment_mode, description, id_member, id_collectivity
        from transaction
        where id_account = ?
        order by transaction_date
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, account.getId());
            ResultSet rs = stmt.executeQuery();

            List<Transaction> transactions = new ArrayList<>();
            while (rs.next()) {
                Transaction transaction = Transaction.builder()
                        .id(rs.getString("id"))
                        .amount(rs.getBigDecimal("amount").doubleValue())
                        .transactionType(TransactionType.valueOf(rs.getString("transaction_type")))
                        .transactionDate(rs.getTimestamp("transaction_date").toInstant())
                        .paymentMode(rs.getString("payment_mode") != null ?
                                PaymentMode.valueOf(rs.getString("payment_mode")) : null)
                        .description(rs.getString("description"))
                        .collectivity(findById(rs.getString("id_collectivity")))
                        .account(account)
                        .build();
                transactions.add(transaction);
            }

            account.setTransactions(transactions);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load transactions for account", e);
        }
    }

    public Collectivity findByMembershipFeeId(String membershipFeeId) {
        String sql = """
        select c.id, c.number, c.name, c.speciality, c.creation_datetime,
               c.federation_approval, c.authorization_date, c.location
        from collectivity c
        join cotisation_plan cp on c.id = cp.id_collectivity
        where cp.id = ?
    """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, membershipFeeId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Collectivity collectivity = Collectivity.builder()
                        .id(rs.getString("id"))
                        .number(rs.getString("number"))
                        .name(rs.getString("name"))
                        .speciality(rs.getString("speciality"))
                        .creationDatetime(rs.getTimestamp("creation_datetime").toInstant())
                        .federationApproval(rs.getBoolean("federation_approval"))
                        .authorizationDate(rs.getTimestamp("authorization_date") != null ?
                                rs.getTimestamp("authorization_date").toInstant() : null)
                        .location(rs.getString("location"))
                        .build();

                fetchMembersAndStructure(collectivity);

                return collectivity;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find collectivity by membership fee id: " + membershipFeeId, e);
        }
    }
}