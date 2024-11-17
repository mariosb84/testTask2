package com.example.itemservice.controller;

import com.example.itemservice.feign.PhoneSource;
import com.example.itemservice.feign.domain.dto.PhoneDataDto;
import com.example.itemservice.service.DaDataServiceData;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DaDataController {

    private final DaDataServiceData daDataService;

    @PreAuthorize("hasRole('USER') || hasRole('OPERATOR') || hasRole('ADMIN')")
    @PostMapping("/phone")
    public ResponseEntity<List<PhoneDataDto>> checkPhoneData(@RequestBody PhoneSource phoneSource) {
       return daDataService.checkPhoneData(phoneSource);
    }

}
