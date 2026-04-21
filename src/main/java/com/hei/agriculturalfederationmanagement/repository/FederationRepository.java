package com.hei.agriculturalfederationmanagement.repository;

import com.hei.agriculturalfederationmanagement.entity.Federation;
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

    public Optional<Federation> findFederation(){
        String sql = "select f.id from federation";

        try(PreparedStatement pstmt = connection.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery()){
            if(!rs.next()){

            }
            return
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch federation ",e);
        }
    }
}
