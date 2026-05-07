package com.hei.agriculturalfederationmanagement.repository;

import com.hei.agriculturalfederationmanagement.entity.*;
import com.hei.agriculturalfederationmanagement.entity.enums.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.sql.Date;
import java.time.Instant;
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
            INSERT INTO collectivity (id, number, name, speciality, federation_approval,
                                     authorization_date, location, id_federation, creation_date)
            VALUES (?, ?, ?, ?, ?, ?, ?, 'fed-1', NOW())
        """;

        String insertMemberSql = """
            INSERT INTO member_collectivity (id_member, id_collectivity, occupation, start_date)
            VALUES (?, ?, ?::VARCHAR, ?)
        """;

        try {
            connection.setAutoCommit(false);

            String collectivityId = "col-" + System.currentTimeMillis();

            try (PreparedStatement stmt = connection.prepareStatement(insertCollectivitySql)) {
                stmt.setString(1, collectivityId);
                stmt.setString(2, collectivity.getNumber());
                stmt.setString(3, collectivity.getName());
                stmt.setString(4, collectivity.getSpeciality());
                stmt.setBoolean(5, collectivity.isFederationApproval());
                stmt.setDate(6, collectivity.getAuthorizationDate() != null ?
                        Date.valueOf(collectivity.getAuthorizationDate()) : null);
                stmt.setString(7, collectivity.getLocation());
                stmt.executeUpdate();
            }

            // Create default cash account
            String insertAccountSql = "INSERT INTO account (id, id_collectivity) VALUES (?, ?)";
            String insertCashAccountSql = "INSERT INTO cash_account (id_account) VALUES (?)";

            String cashAccountId = collectivityId + "-A-CASH";
            try (PreparedStatement stmt = connection.prepareStatement(insertAccountSql)) {
                stmt.setString(1, cashAccountId);
                stmt.setString(2, collectivityId);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = connection.prepareStatement(insertCashAccountSql)) {
                stmt.setString(1, cashAccountId);
                stmt.executeUpdate();
            }

            // Insert members
            try (PreparedStatement memberStmt = connection.prepareStatement(insertMemberSql)) {
                Timestamp now = Timestamp.from(Instant.now());

                for (String memberId : memberIds) {
                    String occupation = determineOccupation(memberId, presidentId, vicePresidentId,
                            treasurerId, secretaryId);

                    memberStmt.setString(1, memberId);
                    memberStmt.setString(2, collectivityId);
                    memberStmt.setString(3, occupation);
                    memberStmt.setTimestamp(4, now);
                    memberStmt.addBatch();
                }
                memberStmt.executeBatch();
            }

            connection.commit();
            collectivity.setId(collectivityId);
            return findById(collectivityId);

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                throw new RuntimeException("Failed to rollback transaction", rollbackEx);
            }
            throw new RuntimeException("Failed to save collectivity with members " + e.getMessage());
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
        return "JUNIOR";
    }

    public Collectivity findById(String id) {
        String collectivitySql = """
            SELECT id, number, name, speciality, creation_date,
                   federation_approval, authorization_date, location
            FROM collectivity
            WHERE id = ?
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
                        .creationDatetime(rs.getTimestamp("creation_date") != null ?
                                rs.getDate("creation_date").toLocalDate() : null)
                        .federationApproval(rs.getBoolean("federation_approval"))
                        .authorizationDate(rs.getTimestamp("authorization_date") != null ?
                                rs.getDate("authorization_date").toLocalDate() : null)
                        .location(rs.getString("location"))
                        .build();

                fetchMembersAndStructure(collectivity);
                return collectivity;
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find collectivity: " + e.getMessage());
        }
    }
    private void loadRefereesForMembersWithCache(List<Member> members, Map<String, Member> globalCache) {
        if (members == null || members.isEmpty()) return;

        Set<String> idsToLoad = new HashSet<>();
        for (Member m : members) {
            if (m.getReferees().isEmpty()) {
                idsToLoad.add(m.getId());
            }
        }

        if (idsToLoad.isEmpty()) return;

        String placeholders = idsToLoad.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = String.format("""
        SELECT mr.id_candidate, mr.id_referee,
               m.first_name, m.last_name, m.email, m.phone_number, m.gender
        FROM member_referee mr
        JOIN member m ON mr.id_referee = m.id
        WHERE mr.id_candidate IN (%s)
    """, placeholders);

        Set<String> newRefereeIds = new HashSet<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            int idx = 1;
            for (String id : idsToLoad) {
                stmt.setString(idx++, id);
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String candidateId = rs.getString("id_candidate");
                String refereeId = rs.getString("id_referee");

                Member referee = globalCache.computeIfAbsent(refereeId, id -> {
                    try {
                        Member m = Member.builder()
                                .id(id)
                                .firstName(rs.getString("first_name"))
                                .lastName(rs.getString("last_name"))
                                .email(rs.getString("email"))
                                .phoneNumber(rs.getString("phone_number"))
                                .gender(Gender.valueOf(rs.getString("gender")))
                                .referees(new ArrayList<>())
                                .build();
                        newRefereeIds.add(id);
                        return m;
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });

                Member candidate = globalCache.get(candidateId);
                if (candidate != null && !candidate.getReferees().contains(referee)) {
                    candidate.getReferees().add(referee);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load referees " + e.getMessage());
        }

        if (!newRefereeIds.isEmpty()) {
            List<Member> newMembers = newRefereeIds.stream()
                    .map(globalCache::get)
                    .filter(Objects::nonNull)
                    .toList();
            loadRefereesForMembersWithCache(newMembers, globalCache);
        }
    }

    private void fetchMembersAndStructure(Collectivity collectivity) {
        String sql = """
        SELECT m.id, m.first_name, m.last_name, m.birth_date, m.enrolment_date,
               m.address, m.email, m.phone_number, m.profession, m.gender,
               mc.occupation
        FROM member_collectivity mc
        JOIN member m ON mc.id_member = m.id
        WHERE mc.id_collectivity = ? AND mc.end_date IS NULL
    """;

        List<Member> members = new ArrayList<>();
        Structure structure = Structure.builder().build();

        Map<String, Member> globalMemberCache = new HashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, collectivity.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String memberId = rs.getString("id");

                Member member = globalMemberCache.computeIfAbsent(memberId, id -> {
                    try {
                        return Member.builder()
                                .id(id)
                                .firstName(rs.getString("first_name"))
                                .lastName(rs.getString("last_name"))
                                .birthDate(rs.getDate("birth_date") != null ?
                                        rs.getDate("birth_date").toLocalDate() : null)
                                .enrolmentDate(rs.getDate("enrolment_date") != null ?
                                        rs.getDate("enrolment_date").toLocalDate() : null)
                                .address(rs.getString("address"))
                                .email(rs.getString("email"))
                                .phoneNumber(rs.getString("phone_number"))
                                .profession(rs.getString("profession"))
                                .gender(Gender.valueOf(rs.getString("gender")))
                                .referees(new ArrayList<>())
                                .build();
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });

                members.add(member);

                String occupation = rs.getString("occupation");
                switch (occupation) {
                    case "PRESIDENT" -> structure.setPresident(member);
                    case "VICE_PRESIDENT" -> structure.setVicePresident(member);
                    case "TREASURER" -> structure.setTreasurer(member);
                    case "SECRETARY" -> structure.setSecretary(member);
                }
            }

            loadRefereesForMembersWithCache(members, globalMemberCache);

            collectivity.setMembers(members);
            collectivity.setStructure(structure);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch members and structure " + e.getMessage());
        }
    }
    public Map<String, String> findMemberOccupations(String collectivityId) {
        String sql = """
        SELECT id_member, occupation
        FROM member_collectivity
        WHERE id_collectivity = ? AND end_date IS NULL
    """;

        Map<String, String> occupations = new HashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                occupations.put(rs.getString("id_member"), rs.getString("occupation"));
            }
            return occupations;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find occupations " + e.getMessage());
        }
    }
    public void loadRefereesForMembers(List<Member> members) {
        if (members == null || members.isEmpty()) return;

        Map<String, Member> memberMap = members.stream()
                .collect(Collectors.toMap(Member::getId, m -> m));
        for (Member m : memberMap.values()) {
            if (m.getReferees() == null) {
                m.setReferees(new ArrayList<>());
            }
        }
        List<String> memberIds = members.stream().map(Member::getId).toList();
        if (memberIds.isEmpty()) return;
        String placeholders = memberIds.stream().map(id -> "?").collect(Collectors.joining(","));

        String sql = String.format("""
            SELECT mr.id_candidate, mr.id_referee,
                   m.first_name, m.last_name, m.email, m.phone_number, m.gender
            FROM member_referee mr
            JOIN member m ON mr.id_referee = m.id
            WHERE mr.id_candidate IN (%s)
        """, placeholders);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < memberIds.size(); i++) {
                stmt.setString(i + 1, memberIds.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String candidateId = rs.getString("id_candidate");
                Member candidate = memberMap.get(candidateId);
                if (candidate != null) {
                    Member referee = Member.builder()
                            .id(rs.getString("id_referee"))
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
            throw new RuntimeException("Failed to load referees " + e.getMessage());
        }
    }

    public boolean existsByNumber(String number) {
        if (number == null) return false;
        String sql = "SELECT COUNT(id) FROM collectivity WHERE number = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, number);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check number existence " + e.getMessage());
        }
    }

    public boolean existsByName(String name) {
        if (name == null) return false;
        String sql = "SELECT COUNT(id) FROM collectivity WHERE name = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check name existence " + e.getMessage());
        }
    }

    public void assignIdentity(String id, String number, String name) {
        String updateSql = """
        UPDATE collectivity 
        SET number = COALESCE(?, number), 
            name = COALESCE(?, name) 
        WHERE id = ?
    """;
        try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
            stmt.setString(1, number);
            stmt.setString(2, name);
            stmt.setString(3, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to assign identity " + e.getMessage());
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

    public Map<String, Account> loadAccountsWithTransactions(String collectivityId, Instant at) {
        String sql = """
            SELECT a.id AS account_id, a.id_collectivity,
                   ca.id AS cash_account_id,
                   ba.id AS bank_account_id, ba.holder_name AS bank_holder_name, ba.bank_name,
                   ba.bank_code, ba.branch_code, ba.account_number, ba.rib_key,
                   ma.id AS mobile_account_id, ma.holder_name AS mobile_holder_name,
                   ma.service_name, ma.phone_number,
                   t.id AS transaction_id, t.amount AS transaction_amount,
                   t.transaction_type, t.transaction_date, t.payment_mode
            FROM account a
            LEFT JOIN cash_account ca ON a.id = ca.id_account
            LEFT JOIN bank_account ba ON a.id = ba.id_account
            LEFT JOIN mobile_money_account ma ON a.id = ma.id_account
            LEFT JOIN transaction t ON a.id = t.id_account
            WHERE a.id_collectivity = ?
        """;

        if (at != null) {
            sql += " AND (t.transaction_date <= ? OR t.id IS NULL)";
        }
        sql += " ORDER BY a.id, t.transaction_date";

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
                                .transactions(new ArrayList<>())
                                .build();

                        if (rs.getString("cash_account_id") != null) {
                            newAccount.setCashAccount(CashAccount.builder()
                                    .id(rs.getString("cash_account_id"))
                                    .build());
                        } else if (rs.getString("bank_account_id") != null) {
                            newAccount.setBankAccount(BankAccount.builder()
                                    .id(rs.getString("bank_account_id"))
                                    .holderName(rs.getString("bank_holder_name"))
                                    .bankName(Bank.valueOf(rs.getString("bank_name")))
                                    .bankCode(rs.getString("bank_code"))
                                    .branchCode(rs.getString("branch_code"))
                                    .accountNumber(rs.getString("account_number"))
                                    .ribKey(rs.getString("rib_key"))
                                    .build());
                        } else if (rs.getString("mobile_account_id") != null) {
                            newAccount.setMobileMoneyAccount(MobileMoneyAccount.builder()
                                    .id(rs.getString("mobile_account_id"))
                                    .holderName(rs.getString("mobile_holder_name"))
                                    .serviceName(MobileBankingService.valueOf(rs.getString("service_name")))
                                    .phoneNumber(rs.getString("phone_number"))
                                    .build());
                        }
                        return newAccount;
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                });

                String transactionId = rs.getString("transaction_id");
                if (transactionId != null) {
                    Transaction transaction = Transaction.builder()
                            .id(transactionId)
                            .amount(rs.getDouble("transaction_amount"))
                            .transactionType(TransactionType.valueOf(rs.getString("transaction_type")))
                            .transactionDate(rs.getDate("transaction_date").toLocalDate())
                            .paymentMode(PaymentMode.valueOf(rs.getString("payment_mode")))
                            .account(account)
                            .build();
                    account.getTransactions().add(transaction);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load accounts " + e.getMessage());
        }

        return accountMap;
    }
    public List<Transaction> findTransactionsByCollectivityIdAndDateRange(
            String collectivityId, Instant from, Instant to) {

        String sql = """
        SELECT t.id, t.amount, t.transaction_date, t.payment_mode,
               t.id_account, t.id_member,
               m.first_name, m.last_name, m.birth_date, m.enrolment_date,
               m.address, m.email, m.phone_number, m.profession, m.gender
        FROM transaction t
        JOIN member m ON t.id_member = m.id
        WHERE t.id_collectivity = ?
        AND t.transaction_type = 'IN'
        AND t.transaction_date >= ?
        AND t.transaction_date < ?
        ORDER BY t.transaction_date DESC
    """;

        List<Transaction> transactions = new ArrayList<>();
        Map<String, Account> accountMap = loadAccountsWithTransactions(collectivityId, null);
        Map<String, Member> memberCache = new HashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, collectivityId);
            stmt.setTimestamp(2, Timestamp.from(from));
            stmt.setTimestamp(3, Timestamp.from(to));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String memberId = rs.getString("id_member");

                // Use cache to avoid creating duplicate member objects
                Member member = memberCache.computeIfAbsent(memberId, id -> {
                    try {
                        return Member.builder()
                                .id(id)
                                .firstName(rs.getString("first_name"))
                                .lastName(rs.getString("last_name"))
                                .birthDate(rs.getDate("birth_date") != null ?
                                        rs.getDate("birth_date").toLocalDate() : null)
                                .enrolmentDate(rs.getDate("enrolment_date") != null ?
                                        rs.getDate("enrolment_date").toLocalDate() : null)
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

                Transaction transaction = Transaction.builder()
                        .id(rs.getString("id"))
                        .amount(rs.getDouble("amount"))
                        .transactionDate(rs.getDate("transaction_date").toLocalDate() != null ?
                                rs.getDate("transaction_date").toLocalDate() : null)
                        .paymentMode(PaymentMode.valueOf(rs.getString("payment_mode")))
                        .account(accountMap.get(rs.getString("id_account")))
                        .member(member)
                        .build();
                transactions.add(transaction);
            }

            // Load referees for all unique members found in transactions
            if (!memberCache.isEmpty()) {
                List<Member> uniqueMembers = new ArrayList<>(memberCache.values());
                loadRefereesForMembers(uniqueMembers);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find transactions", e);
        }

        return transactions;
    }
}