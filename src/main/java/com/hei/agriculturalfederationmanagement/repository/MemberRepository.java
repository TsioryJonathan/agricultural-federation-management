package com.hei.agriculturalfederationmanagement.repository;

import com.hei.agriculturalfederationmanagement.entity.Collectivity;
import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.MemberCollectivity;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateMember;
import com.hei.agriculturalfederationmanagement.entity.enums.CollectivityOccupation;
import com.hei.agriculturalfederationmanagement.entity.enums.Gender;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.sql.Date;
import java.time.Instant;
import java.util.*;

@Repository
@AllArgsConstructor
public class MemberRepository {

    private final Connection connection;

    public List<Member> findByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();

        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));

        String sql = """
        SELECT
            m.id AS m_id,
            m.first_name,
            m.last_name,
            m.birth_date,
            m.enrolment_date,
            m.address,
            m.email,
            m.phone_number,
            m.profession,
            m.gender,

            mc.id AS mc_id,
            mc.start_date,
            mc.end_date,
            mc.occupation,

            c.id AS c_id,
            c.name,
            c.number,
            c.speciality,
            c.authorization_date,
            c.location

        FROM member m
        LEFT JOIN member_collectivity mc ON m.id = mc.id_member
        LEFT JOIN collectivity c ON mc.id_collectivity = c.id
        WHERE m.id IN (%s)
        """.formatted(placeholders);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {

            for (int i = 0; i < ids.size(); i++) {
                stmt.setInt(i + 1, ids.get(i));
            }

            ResultSet rs = stmt.executeQuery();

            Map<Integer, Member> map = new HashMap<>();

            while (rs.next()) {
                int id = rs.getInt("m_id");

                Member member = map.get(id);
                if (member == null) {
                    member = mapBasicMember(rs);
                    map.put(id, member);
                }

                if (rs.getObject("mc_id") != null) {
                    member.getMemberCollectivities()
                            .add(mapMemberCollectivity(rs, member));
                }
            }

            return new ArrayList<>(map.values());

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean existsById(Integer id) {
        String sql = "SELECT COUNT(*) FROM member WHERE id = ?";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);

            ResultSet rs = stmt.executeQuery();
            rs.next();

            return rs.getInt(1) > 0;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Member> saveAll(List<Member> members, List<CreateMember> dtos) {

        String insertMemberSql = """
        INSERT INTO member(
            first_name, last_name, birth_date, enrolment_date,
            address, email, phone_number, profession, gender
        )
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

        String insertMcSql = """
        INSERT INTO member_collectivity(
            id_member, id_collectivity, occupation, start_date, end_date
        )
        VALUES (?, ?, ?, ?, ?)
    """;

        String insertRefSql = """
        INSERT INTO member_referee(
            id_candidate, id_referee, id_collectivity, relationship, created_at
        )
        VALUES (?, ?, ?, ?, ?)
    """;

        try {
            connection.setAutoCommit(false);

            List<Member> result = new ArrayList<>();

            try (
                    PreparedStatement memberStmt = connection.prepareStatement(insertMemberSql, Statement.RETURN_GENERATED_KEYS);
                    PreparedStatement mcStmt = connection.prepareStatement(insertMcSql);
                    PreparedStatement refStmt = connection.prepareStatement(insertRefSql)
            ) {

                for (int i = 0; i < members.size(); i++) {

                    Member member = members.get(i);
                    CreateMember dto = dtos.get(i);

                    // ---------------- MEMBER ----------------
                    memberStmt.setString(1, member.getFirstName());
                    memberStmt.setString(2, member.getLastName());
                    memberStmt.setDate(3, Date.valueOf(member.getBirthDate()));
                    memberStmt.setTimestamp(4, Timestamp.from(Instant.now()));
                    memberStmt.setString(5, member.getAddress());
                    memberStmt.setString(6, member.getEmail());
                    memberStmt.setString(7, member.getPhoneNumber());
                    memberStmt.setString(8, member.getProfession());
                    memberStmt.setString(9, member.getGender().name());

                    memberStmt.executeUpdate();

                    ResultSet keys = memberStmt.getGeneratedKeys();
                    if (!keys.next()) throw new RuntimeException("No generated key");

                    int memberId = keys.getInt(1);
                    member.setId(memberId);

                    // ---------------- MEMBER_COLLECTIVITY ----------------
                    mcStmt.setInt(1, memberId);
                    mcStmt.setInt(2, dto.getCollectivityIdentifier());
                    mcStmt.setString(3, dto.getOccupation().name());
                    mcStmt.setTimestamp(4, Timestamp.from(Instant.now()));
                    mcStmt.setTimestamp(5, null);

                    mcStmt.executeUpdate();

                    // ---------------- REFEREES ----------------
                    if (dto.getReferees() != null) {
                        for (Integer refId : dto.getReferees()) {

                            refStmt.setInt(1, memberId);
                            refStmt.setInt(2, refId);
                            refStmt.setInt(3, dto.getCollectivityIdentifier());
                            refStmt.setString(4, "FRIEND");
                            refStmt.setTimestamp(5, Timestamp.from(Instant.now()));

                            refStmt.addBatch();
                        }

                        refStmt.executeBatch();
                    }

                    result.add(member);
                }

                connection.commit();
                return result;

            } catch (Exception e) {
                connection.rollback();
                throw new RuntimeException("Save members failed", e);
            } finally {
                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Member mapBasicMember(ResultSet rs) throws SQLException {
        return Member.builder()
                .id(rs.getInt("m_id"))
                .firstName(rs.getString("first_name"))
                .lastName(rs.getString("last_name"))
                .birthDate(rs.getDate("birth_date").toLocalDate())
                .enrolmentDate(rs.getTimestamp("enrolment_date").toInstant())
                .address(rs.getString("address"))
                .email(rs.getString("email"))
                .phoneNumber(rs.getString("phone_number"))
                .profession(rs.getString("profession"))
                .gender(Gender.valueOf(rs.getString("gender")))
                .memberCollectivities(new ArrayList<>())
                .build();
    }

    private MemberCollectivity mapMemberCollectivity(ResultSet rs, Member member) throws SQLException {

        Collectivity c = Collectivity.builder()
                .id(rs.getInt("c_id"))
                .name(rs.getString("name"))
                .number(rs.getString("number"))
                .speciality(rs.getString("speciality"))
                .authorizationDate(
                        rs.getTimestamp("authorization_date") != null
                                ? rs.getTimestamp("authorization_date").toInstant()
                                : null
                )
                .location(rs.getString("location"))
                .build();

        MemberCollectivity mc = MemberCollectivity.builder()
                .id(rs.getInt("mc_id"))
                .startDate(rs.getTimestamp("start_date").toInstant())
                .endDate(rs.getTimestamp("end_date") != null
                        ? rs.getTimestamp("end_date").toInstant()
                        : null)
                .occupation(CollectivityOccupation.valueOf(rs.getString("occupation")))
                .build();

        mc.setMember(member);
        mc.setCollectivity(c);

        return mc;
    }
}