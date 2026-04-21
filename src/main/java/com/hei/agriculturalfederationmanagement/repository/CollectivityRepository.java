package com.hei.agriculturalfederationmanagement.repository;

import com.hei.agriculturalfederationmanagement.entity.Collectivity;
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
                                     federation_approval, authorization_date, id_location)
            VALUES (?, ?, ?, ?, ?, ?, ?)
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

            stmt.setInt(7, collectivity.getLocation().getId());

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
}
