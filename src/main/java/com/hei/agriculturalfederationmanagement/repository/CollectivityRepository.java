package com.hei.agriculturalfederationmanagement.repository;

import com.hei.agriculturalfederationmanagement.entity.*;
import com.hei.agriculturalfederationmanagement.entity.enums.*;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@AllArgsConstructor
public class CollectivityRepository {
    private final Connection connection;

    public Collectivity save(Collectivity collectivity, List<Integer> memberIds,
                                                    Integer presidentId, Integer vicePresidentId,
                                                    Integer treasurerId, Integer secretaryId) {
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

            int collectivityId;
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
                    collectivityId = rs.getInt("id");
                } else {
                    throw new SQLException("Failed to insert collectivity, no ID returned");
                }
            }

            try (PreparedStatement memberStmt = connection.prepareStatement(insertMemberSql)) {
                Timestamp now = Timestamp.from(Instant.now());

                for (Integer memberId : memberIds) {
                    String occupation = determineOccupation(memberId, presidentId, vicePresidentId,
                            treasurerId, secretaryId);

                    memberStmt.setInt(1, memberId);
                    memberStmt.setInt(2, collectivityId);
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

    private String determineOccupation(Integer memberId, Integer presidentId, Integer vicePresidentId,
                                       Integer treasurerId, Integer secretaryId) {
        if (memberId.equals(presidentId)) return "PRESIDENT";
        if (memberId.equals(vicePresidentId)) return "VICE_PRESIDENT";
        if (memberId.equals(treasurerId)) return "TREASURER";
        if (memberId.equals(secretaryId)) return "SECRETARY";

        if (hasMinimumSeniority(memberId)) {
            return "SENIOR";
        }
        return "JUNIOR";
    }

    private boolean hasMinimumSeniority(Integer memberId) {
        String sql = """
            select enrolment_date from member where id = ?
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, memberId);
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

    public Collectivity findById(Integer id) {
        String collectivitySql = """
            select id, number, name, speciality, creation_datetime, 
                   federation_approval, authorization_date, location
            from collectivity
            where id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(collectivitySql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Collectivity collectivity = Collectivity.builder()
                        .id(rs.getInt("id"))
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
        Map<Integer, Member> memberCache = new HashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, collectivity.getId());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Integer memberId = rs.getInt("id");

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

        Map<Integer, Member> memberMap = members.stream()
                .collect(Collectors.toMap(Member::getId, m -> m));

        List<Integer> memberIds = members.stream()
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
                stmt.setInt(i + 1, memberIds.get(i));
            }

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Integer candidateId = rs.getInt("id_candidate");
                Integer refereeId = rs.getInt("id_referee");

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
                                                 List<List<Integer>> memberIdsList,
                                                 List<Integer> presidentIds,
                                                 List<Integer> vicePresidentIds,
                                                 List<Integer> treasurerIds,
                                                 List<Integer> secretaryIds) {
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

    public void assignIdentity(Integer id, String number, String name) {
        String updateSql = "update collectivity set number = ?, name = ? where id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateSql)) {
            stmt.setString(1, number);
            stmt.setString(2, name);
            stmt.setInt(3, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to assign identity", e);
        }
    }

    public List<Transaction> findTransactionsByCollectivityIdAndDateRange(
            Integer collectivityId,
            Instant from,
            Instant to) {

        String sql = """
        select
            t.id,
            t.amount,
            t.transaction_date,
            t.payment_mode,
            t.id_account,
            t.id_member,
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
        join account a on t.id_account = a.id
        join member m on t.id_member = m.id
        left join cash_account ca on a.id = ca.id_account
        left join bank_account ba on a.id = ba.id_account
        left join mobile_money_account ma on a.id = ma.id_account
        where t.id_collectivity = ?
          and t.transaction_type = 'IN'
          and t.transaction_date >= ?
          and t.transaction_date < ?
        order by t.transaction_date desc
    """;

        List<Transaction> transactions = new ArrayList<>();
        Map<Integer, Member> memberCache = new HashMap<>();

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, collectivityId);
            stmt.setTimestamp(2, Timestamp.from(from));
            stmt.setTimestamp(3, Timestamp.from(to));
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Transaction transaction = Transaction.builder()
                        .id(rs.getInt("id"))
                        .amount(rs.getBigDecimal("amount").doubleValue())
                        .transactionDate(rs.getTimestamp("transaction_date").toInstant())
                        .paymentMode(rs.getString("payment_mode") != null ?
                                PaymentMode.valueOf(rs.getString("payment_mode")) : null)
                        .idAccount(rs.getInt("id_account"))
                        .idMember(rs.getInt("id_member"))
                        .build();

                Account account = buildAccountFromResultSet(rs);
                transaction.setAccount(account);

                Integer memberId = rs.getInt("id_member");
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

            List<Member> uniqueMembers = new ArrayList<>(memberCache.values());
            loadRefereesForMembers(uniqueMembers);

            return transactions;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to find transactions", e);
        }
    }


    private Account buildAccountFromResultSet(ResultSet rs) throws SQLException {
        Integer accountId = rs.getInt("id_account");
        Integer collectivityId = rs.getInt("id_collectivity");
        Integer federationId = rs.getInt("id_federation");

        Account account = Account.builder()
                .id(accountId)
                .idCollectivity(collectivityId)
                .idFederation(federationId)
                .build();

        if (rs.getObject("cash_account_id") != null) {
            CashAccount cashAccount = CashAccount.builder()
                    .id(rs.getInt("cash_account_id"))
                    .account(account)
                    .build();
            account.setCashAccount(cashAccount);
        } else if (rs.getObject("bank_account_id") != null) {
            BankAccount bankAccount = BankAccount.builder()
                    .id(rs.getInt("bank_account_id"))
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
                    .id(rs.getInt("mobile_account_id"))
                    .account(account)
                    .holderName(rs.getString("mobile_holder_name"))
                    .serviceName(MobileMoneyService.valueOf(rs.getString("service_name")))
                    .phoneNumber(rs.getString("phone_number"))
                    .build();
            account.setMobileMoneyAccount(mobileAccount);
        }

        return account;
    }

}