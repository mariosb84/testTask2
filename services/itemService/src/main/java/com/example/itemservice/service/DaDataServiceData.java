package com.example.itemservice.service;

import com.example.itemservice.feign.DaDataApiClient;
import com.example.itemservice.feign.GetPhoneData;
import com.example.itemservice.feign.PhoneSource;
import com.example.itemservice.feign.domain.dto.PhoneDataDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DaDataServiceData implements DaDataService {

    private final DaDataApiClient apiClient;
    private final GetPhoneData getPhoneData;

    @Override
    public ResponseEntity<List<PhoneDataDto>> checkPhoneData(PhoneSource phoneSource) {
        var checkResult = getPhoneData.getStandPhoneData(
                apiClient.readPhoneDataByPhoneNumber(phoneSource));
        if (checkResult != null) {
            return ResponseEntity.ok(checkResult);
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "номер телефона не найден!");
    }

}
