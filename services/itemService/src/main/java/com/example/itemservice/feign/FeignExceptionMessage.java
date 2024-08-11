package com.example.itemservice.feign;

import lombok.Data;

@Data
public class FeignExceptionMessage {
    private String timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
}
