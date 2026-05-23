package com.nik07roxx.apexPay.Service.Implementation;

import com.nik07roxx.apexPay.DTO.Currency.CurrencyConvertorResponse;
import com.nik07roxx.apexPay.model.CurrencyType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class CurrencyConvertorService {

    private String apiUrl = "https://v6.exchangerate-api.com/v6/API_KEY/latest/CUR";

    @Value("${app.exchangerate-api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public CurrencyConvertorResponse getConvertedCurrencyAmount(CurrencyType currencyInput)
    {
        // correct the API URL
        String apiUrlWithInfo = apiUrl.replace("API_KEY", apiKey).replace("CUR", currencyInput.toString());
        ResponseEntity<CurrencyConvertorResponse> exchange = restTemplate.exchange(apiUrlWithInfo, HttpMethod.GET, null, CurrencyConvertorResponse.class);
        if(exchange.getStatusCode() == HttpStatus.OK)
            return exchange.getBody();
        else
            throw new RuntimeException("No currency found to exchange for: "+currencyInput);
    }
}
