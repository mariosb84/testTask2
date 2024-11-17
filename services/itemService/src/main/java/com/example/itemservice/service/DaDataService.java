package com.example.itemservice.service;

import com.example.itemservice.feign.PhoneSource;
import com.example.itemservice.feign.domain.dto.PhoneDataDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface DaDataService {

    ResponseEntity<List<PhoneDataDto>> checkPhoneData(PhoneSource phoneSource);

}
