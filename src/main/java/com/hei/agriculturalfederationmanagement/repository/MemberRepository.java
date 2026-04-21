package com.hei.agriculturalfederationmanagement.repository;

import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.enums.Gender;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Repository
@AllArgsConstructor
public class MemberRepository {

    private final Connection connection;

    public List<Member> findByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();

        String placeholders = String.join(",", ids.stream().map(i -> "?").toArray(String[]::new));
        String sql = "select id, first_name,last_name,birth_date,enrolment_date,address,email,phone_number,profession,gender from member where id in (%s)".formatted(placeholders);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < ids.size(); i++) {
                stmt.setInt(i + 1, ids.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            List<Member> members = new ArrayList<>();
            while (rs.next()) {
                members.add(mapResultSetToMember(rs));
            }
            return members;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find members by ids", e);
        }
    }


    public boolean existsById(Integer id) {
        String sql = "select count(id) from member where id = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getInt(1) <= 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check member existence", e);
        }
    }

    private Member mapResultSetToMember(ResultSet rs) throws SQLException {
        return Member.builder()
                .id(rs.getInt("id"))
                .firstName(rs.getString("first_name"))
                .lastName(rs.getString("last_name"))
                .birthDate(rs.getDate("birth_date").toLocalDate())
                .enrolmentDate(rs.getTimestamp("enrolment_date").toInstant())
                .address(rs.getString("address"))
                .email(rs.getString("email"))
                .phoneNumber(rs.getString("phone_number"))
                .profession(rs.getString("profession"))
                .gender(Gender.valueOf(rs.getString("gender")))
                .build();
    }
}
