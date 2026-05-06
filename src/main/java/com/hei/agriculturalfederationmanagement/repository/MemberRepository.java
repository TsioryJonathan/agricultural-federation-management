package com.hei.agriculturalfederationmanagement.repository;

import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.enums.Gender;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@AllArgsConstructor
public class MemberRepository {
    private final Connection connection;

    public Member save(Member member) {
        String insertSql = """
            INSERT INTO member (id, first_name, last_name, birth_date, gender, address, 
                              profession, phone_number, email, enrolment_date, is_superuser)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), ?)
            ON CONFLICT (id) DO UPDATE SET
                first_name = EXCLUDED.first_name,
                last_name = EXCLUDED.last_name,
                birth_date = EXCLUDED.birth_date,
                gender = EXCLUDED.gender,
                address = EXCLUDED.address,
                profession = EXCLUDED.profession,
                phone_number = EXCLUDED.phone_number,
                email = EXCLUDED.email,
                is_superuser = EXCLUDED.is_superuser
        """;

        try (PreparedStatement stmt = connection.prepareStatement(insertSql)) {
            String id = member.getId() != null ? member.getId() : "M" + System.currentTimeMillis();
            stmt.setString(1, id);
            stmt.setString(2, member.getFirstName());
            stmt.setString(3, member.getLastName());
            stmt.setDate(4, Date.valueOf(member.getBirthDate()));
            stmt.setString(5, member.getGender().name());
            stmt.setString(6, member.getAddress());
            stmt.setString(7, member.getProfession());
            stmt.setString(8, member.getPhoneNumber());
            stmt.setString(9, member.getEmail());
            stmt.setBoolean(10, member.isSuperuser());
            stmt.executeUpdate();
            member.setId(id);
            return member;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save member " + e.getMessage());
        }
    }

    public List<Member> saveAll(List<Member> members) {
        List<Member> savedMembers = new ArrayList<>();
        for (Member member : members) {
            savedMembers.add(save(member));
        }
        return savedMembers;
    }

    public Optional<Member> findById(String id) {
        String sql = """
            SELECT id, first_name, last_name, birth_date, gender, address, 
                   profession, phone_number, email, enrolment_date, is_superuser
            FROM member WHERE id = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Member member = mapMember(rs);
                loadRefereesForMember(member);
                return Optional.of(member);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find member " + e.getMessage());
        }
    }

    public List<Member> findByIds(List<String> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();

        String placeholders = ids.stream().map(id -> "?").collect(Collectors.joining(","));
        String sql = String.format("""
            SELECT id, first_name, last_name, birth_date, gender, address, 
                   profession, phone_number, email, enrolment_date, is_superuser
            FROM member WHERE id IN (%s)
        """, placeholders);

        List<Member> members = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < ids.size(); i++) {
                stmt.setString(i + 1, ids.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                members.add(mapMember(rs));
            }

            // Load referees for all members
            for (Member member : members) {
                loadRefereesForMember(member);
            }

            return members;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find members " + e.getMessage());
        }
    }

    private Member mapMember(ResultSet rs) throws SQLException {
        return Member.builder()
                .id(rs.getString("id"))
                .firstName(rs.getString("first_name"))
                .lastName(rs.getString("last_name"))
                .birthDate(rs.getDate("birth_date").toLocalDate())
                .gender(Gender.valueOf(rs.getString("gender")))
                .address(rs.getString("address"))
                .profession(rs.getString("profession"))
                .phoneNumber(rs.getString("phone_number"))
                .email(rs.getString("email"))
                .enrolmentDate(rs.getDate("enrolment_date").toLocalDate() != null ?
                        rs.getDate("enrolment_date").toLocalDate() : null)
                .isSuperuser(rs.getBoolean("is_superuser"))
                .referees(new ArrayList<>())
                .build();
    }

    private void loadRefereesForMember(Member member) {
        String sql = """
            SELECT m.id, m.first_name, m.last_name, m.birth_date, m.gender, m.address,
                   m.profession, m.phone_number, m.email, m.enrolment_date, m.is_superuser,
                   mr.relationship
            FROM member_referee mr
            JOIN member m ON mr.id_referee = m.id
            WHERE mr.id_candidate = ?
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, member.getId());
            ResultSet rs = stmt.executeQuery();

            List<Member> referees = new ArrayList<>();
            while (rs.next()) {
                referees.add(mapMember(rs));
            }
            member.setReferees(referees);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load referees for member: " + member.getId() + e.getMessage());
        }
    }

    public boolean existsById(String id) {
        String sql = "SELECT COUNT(id) FROM member WHERE id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check member existence " + e.getMessage());
        }
    }

    public void addReferee(String candidateId, String refereeId, String relationship) {
        String sql = """
            INSERT INTO member_referee (id_candidate, id_referee, relationship) 
            VALUES (?, ?, ?)
            ON CONFLICT (id_candidate, id_referee) DO NOTHING
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, candidateId);
            stmt.setString(2, refereeId);
            stmt.setString(3, relationship != null ? relationship : "Parrainage");
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add referee " + e.getMessage());
        }
    }

    public void addToCollectivity(String memberId, String collectivityId, String occupation) {
        String sql = """
            INSERT INTO member_collectivity (id_member, id_collectivity, occupation, start_date)
            VALUES (?, ?, ?, NOW())
            ON CONFLICT (id_member, id_collectivity, start_date) DO NOTHING
        """;
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, memberId);
            stmt.setString(2, collectivityId);
            stmt.setString(3, occupation);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add member to collectivity " + e.getMessage());
        }
    }

    public List<String> findCollectivityIdsByMemberId(String memberId) {
        String sql = """
            SELECT DISTINCT id_collectivity 
            FROM member_collectivity 
            WHERE id_member = ? AND end_date IS NULL
        """;

        List<String> collectivityIds = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, memberId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                collectivityIds.add(rs.getString("id_collectivity"));
            }
            return collectivityIds;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find collectivity ids for member " + e.getMessage());
        }
    }

    public String findCollectivityIdByMemberId(String memberId) {
        String sql = """
            SELECT id_collectivity 
            FROM member_collectivity 
            WHERE id_member = ? AND end_date IS NULL
            LIMIT 1
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, memberId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("id_collectivity");
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find collectivity id for member " + e.getMessage());
        }
    }

    /**
     * Get the occupation of a member in a specific collectivity
     */
    public String findOccupationByMemberIdAndCollectivityId(String memberId, String collectivityId) {
        String sql = """
            SELECT occupation 
            FROM member_collectivity 
            WHERE id_member = ? 
            AND id_collectivity = ? 
            AND end_date IS NULL
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, memberId);
            stmt.setString(2, collectivityId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getString("occupation");
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find occupation for member: " + memberId +
                    " in collectivity: " + collectivityId, e);
        }
    }

    /**
     * Get the occupation of a member in a specific collectivity (returns Optional)
     */
    public Optional<String> findOccupationByMemberIdAndCollectivityIdOptional(String memberId, String collectivityId) {
        String sql = """
            SELECT occupation 
            FROM member_collectivity 
            WHERE id_member = ? 
            AND id_collectivity = ? 
            AND end_date IS NULL
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, memberId);
            stmt.setString(2, collectivityId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(rs.getString("occupation"));
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find occupation for member: " + memberId +
                    " in collectivity: " + collectivityId, e);
        }
    }
}