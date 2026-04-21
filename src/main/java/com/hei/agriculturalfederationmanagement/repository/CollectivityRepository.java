package com.hei.agriculturalfederationmanagement.repository;

import com.hei.agriculturalfederationmanagement.entity.Collectivity;
import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.MemberCollectivity;
import com.hei.agriculturalfederationmanagement.entity.Structure;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateCollectivity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Repository
@AllArgsConstructor
public class CollectivityRepository {
    private final Connection connection;

    public Collectivity save(Collectivity collectivity) {
        String sql = """
            insert into collectivity (number, name, speciality, creation_datetime, 
                                     federation_approval, authorization_date, location)
            values (?, ?, ?, ?, ?, ?, ?)
            returning id
        """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, collectivity.getNumber());
            stmt.setString(2, collectivity.getName());
            stmt.setString(3, collectivity.getSpeciality());
            stmt.setTimestamp(4, Timestamp.valueOf(String.valueOf(collectivity.getCreationDatetime())));
            stmt.setBoolean(5, collectivity.isFederationApproval());
            stmt.setTimestamp(6, collectivity.getAuthorizationDate() != null ?
                    Timestamp.valueOf(String.valueOf(collectivity.getAuthorizationDate())) : null);

            stmt.setString(7, collectivity.getLocation());

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                collectivity.setId(rs.getInt("id"));
            }
            return collectivity;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save collectivity", e);
        }
    }

    public List<Collectivity> saveAll(List<Collectivity> collectivities) {
        List<Collectivity> savedCollectivities = new ArrayList<>();
        for (Collectivity collectivity : collectivities) {
            savedCollectivities.add(save(collectivity));
        }
        return savedCollectivities;
    }

    private Structure findStructureByCollectivityId(Integer collectivityId) {
        Structure structure = Structure.builder().build();

        List<MemberCollectivity> memberships = memberCollectivityRepository.findActiveByCollectivityId(collectivityId);

        for (MemberCollectivity membership : memberships) {
            switch (membership.getOccupation()) {
                case PRESIDENT -> structure.setPresident(membership.getMember());
                case VICE_PRESIDENT -> structure.setVicePresident(membership.getMember());
                case TREASURER -> structure.setTreasurer(membership.getMember());
                case SECRETARY -> structure.setSecretary(membership.getMember());
            }
        }

        return structure;
    }

    public List<Collectivity> findAllByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();

        String placeholders = String.join(",", ids.stream().map(i -> "?").toArray(String[]::new));
        String sql = "select id,number,name,speciality,creation_datetime,federation_approval,authorization_date,location from collectivity where id in (%s)".formatted(placeholders);

        List<Collectivity> collectivities = new ArrayList<>();
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (int i = 0; i < ids.size(); i++) {
                stmt.setInt(i + 1, ids.get(i));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Collectivity collectivity = mapResultSetToCollectivity(rs);
                collectivity.setStructure(findStructureByCollectivityId(collectivity.getId()));
                collectivity.setMembers(findMembersByCollectivityId(collectivity.getId()));
                collectivities.add(collectivity);
            }
            return collectivities;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find collectivities", e);
        }
    }

    private List<Member> findMembersByCollectivityId(Integer collectivityId) {
        List<MemberCollectivity> memberships = memberCollectivityRepository.findActiveByCollectivityId(collectivityId);
        return memberships.stream()
                .map(MemberCollectivity::getMember)
                .toList();
    }

    private Collectivity mapResultSetToCollectivity(ResultSet rs) throws SQLException {
        return Collectivity.builder()
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
    }
}
