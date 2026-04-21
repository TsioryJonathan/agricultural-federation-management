package com.hei.agriculturalfederationmanagement.repository;

import com.hei.agriculturalfederationmanagement.entity.Federation;
import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.Structure;
import com.hei.agriculturalfederationmanagement.entity.enums.FederationOccupation;
import com.hei.agriculturalfederationmanagement.entity.enums.Gender;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
@AllArgsConstructor
public class FederationRepository {
    private final Connection connection;

    public Optional<Federation> findFederation() {
        String sql = """
            SELECT 
                f.id as federation_id,
                f.cotisation_percentage,
                mf.id_member,
                mf.occupation,
                m.first_name,
                m.last_name,
                m.birth_date,
                m.enrolment_date,
                m.address,
                m.email,
                m.phone_number,
                m.profession,
                m.gender
            FROM federation f
            LEFT JOIN mandate_federation mf ON f.id = mf.id_federation AND mf.end_date IS NULL
            LEFT JOIN member m ON mf.id_member = m.id
            ORDER BY f.id
        """;

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            Federation federation = null;
            Structure structure = Structure.builder().build();

            while (rs.next()) {
                if (federation == null) {
                    federation = Federation.builder()
                            .id(rs.getInt("federation_id"))
                            .cotisationPercentage(rs.getDouble("cotisation_percentage"))
                            .build();
                }

                int memberId = rs.getInt("id_member");
                if (memberId > 0) {
                    Member member = mapResultSetToMember(rs);
                    String occupation = rs.getString("occupation");

                    switch (FederationOccupation.valueOf(occupation)) {
                        case PRESIDENT -> structure.setPresident(member);
                        case VICE_PRESIDENT -> structure.setVicePresident(member);
                        case TREASURER -> structure.setTreasurer(member);
                        case SECRETARY -> structure.setSecretary(member);
                    }
                }
            }

            if (federation != null) {
                federation.setStructure(structure);
                return Optional.of(federation);
            }
            return Optional.empty();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch federation", e);
        }
    }

    private Member mapResultSetToMember(ResultSet rs) throws SQLException {
        return Member.builder()
                .id(rs.getInt("id_member"))
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