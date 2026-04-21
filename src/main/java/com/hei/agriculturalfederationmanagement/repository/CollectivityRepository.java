package com.hei.agriculturalfederationmanagement.repository;

import com.hei.agriculturalfederationmanagement.entity.Collectivity;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateCollectivity;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.util.List;

@Repository
@AllArgsConstructor
public class CollectivityRepository {
    private final Connection connection;

    public List<Collectivity> save(List<Collectivity> collectivities) {
        String sql = """
                insert into collectivity (number, name, speciality, creation_datetime,
                                        federation_approval, authorization_date, id_federation, id_location)
                                       values (?, ?, ?, ?, ?, ?, ?, ?)
                """;


        try


        return null;
    }
}
