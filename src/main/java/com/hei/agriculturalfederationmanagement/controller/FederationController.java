package com.hei.agriculturalfederationmanagement.controller;

import com.hei.agriculturalfederationmanagement.service.FederationService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/federation")
@AllArgsConstructor
public class FederationController {

    private FederationService service;

    @GetMapping
    private ResponseEntity<?> findFederation(){
        try{
            return ResponseEntity.status(HttpStatus.OK).body(service.findFederation());
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Federation not found!")
        }
    }

}
