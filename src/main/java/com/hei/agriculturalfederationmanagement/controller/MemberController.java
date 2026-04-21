package com.hei.agriculturalfederationmanagement.controller;

import com.hei.agriculturalfederationmanagement.entity.Member;
import com.hei.agriculturalfederationmanagement.entity.dto.CreateMember;
import com.hei.agriculturalfederationmanagement.service.MemberService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/members")
@AllArgsConstructor
public class MemberController {
    private final MemberService service;

    @PostMapping("/")
    public ResponseEntity<?> createMember(@RequestBody List<CreateMember> members) {
        try{
            return ResponseEntity.status(HttpStatus.CREATED).body(service.createMembers(members));
        }
        catch (RuntimeException ex){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }
}
