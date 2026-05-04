package com.nik07roxx.apexPay.DTO.Error;

import java.time.LocalDateTime;
import java.util.Map;

public record ErrorResponse (
        LocalDateTime timestamp,
        Integer status,
        String error,
        String message,

        // only populated when validation fails
        Map<String,String>validationErrors){
}
