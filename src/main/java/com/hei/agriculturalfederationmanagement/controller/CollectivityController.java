package com.hei.agriculturalfederationmanagement.controller;

import com.hei.agriculturalfederationmanagement.entity.dto.CollectivityInformation;
import com.hei.agriculturalfederationmanagement.entity.dto.CollectivityResponse;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateCollectivity;
import com.hei.agriculturalfederationmanagement.exception.ConflictException;
import com.hei.agriculturalfederationmanagement.exception.NotFoundException;
import com.hei.agriculturalfederationmanagement.service.CollectivityService;
import lombok.AllArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/collectivities")
public class CollectivityController {
    private final CollectivityService service;

    @PostMapping
    public ResponseEntity<?> createCollectivities(@RequestBody(required = false) List<CreateCollectivity> createCollectivities){
        try{
            if(createCollectivities == null){
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mandatory body not provided");
            }
            List<CollectivityResponse> collectivities = service.createCollectivities(createCollectivities);
            return ResponseEntity.status(HttpStatus.CREATED).body(collectivities);
        }catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

    @PutMapping("/{id}/informations")
    public ResponseEntity<?> assignInformations(@PathVariable Integer id, @RequestBody(required = false) CollectivityInformation collectivityInformation){
        try{
            if(collectivityInformation == null){
                return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Mandatory body not provided");
            }
            CollectivityResponse response = service.assignIdentity(id, collectivityInformation);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred: " + e.getMessage());
        }
    }

}
