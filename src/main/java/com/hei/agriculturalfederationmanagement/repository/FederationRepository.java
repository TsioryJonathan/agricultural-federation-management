package com.hei.agriculturalfederationmanagement.repository;

import com.hei.agriculturalfederationmanagement.entity.Federation;
import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.Structure;
import com.hei.agriculturalfederationmanagement.entity.enums.FederationOccupation;
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
        String sql = "select id, cotisation_percentage from federation";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                Federation federation = Federation.builder()
                        .id(rs.getInt("id"))
                        .structure(findFederationStructure())
                        .build();
                return Optional.of(federation);
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch federation", e);
        }
    }

    private Structure findFederationStructure() {
        String sql = """
            select mf.id_member, mf.occupation
            from mandate_federation mf
            where mf.end_date is null
        """;

        Structure structure = Structure.builder().build();

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Integer memberId = rs.getInt("id_member");
                String occupation = rs.getString("occupation");
                Member member = memberRepository.findById(memberId).orElse(null);

                switch (FederationOccupation.valueOf(occupation)) {
                    case PRESIDENT -> structure.setPresident(member);
                    case VICE_PRESIDENT -> structure.setVicePresident(member);
                    case TREASURER -> structure.setTreasurer(member);
                    case SECRETARY -> structure.setSecretary(member);
                }
            }

            return structure;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch federation structure", e);
        }
    }
}
