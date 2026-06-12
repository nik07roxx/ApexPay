package com.nik07roxx.apexPay.Service.Implementation;

import com.nik07roxx.apexPay.DTO.Currency.CurrencyConvertorResponse;
import com.nik07roxx.apexPay.exceptions.ExternalServiceUnavailableException;
import com.nik07roxx.apexPay.model.CurrencyType;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class CurrencyConvertorService {

    private String apiUrl = "https://v6.exchangerate-api.com/v6/API_KEY/latest/CUR";

    @Value("${app.exchangerate-api.key}")
    private String apiKey;

    private final RestTemplate restTemplate;
    private final RedisService redisService;
    private final CircuitBreaker externalAPICircuitBreaker;

    @Timed(value = "apexpay.external.currency.api", description = "Time taken to fetch data from external Currency API")
    public CurrencyConvertorResponse getConvertedCurrencyAmount(CurrencyType currencyInput)
    {
        CurrencyConvertorResponse currencyConvertorResponse =
                redisService.get("currencies_of_" + currencyInput.toString(),
                        CurrencyConvertorResponse.class);
        if(currencyConvertorResponse != null) // Return response from cache
        {
            return currencyConvertorResponse;
        }
        else // Return response from External API
        {
            // Wrapping the call in circuit breaker supplier
            Supplier<CurrencyConvertorResponse> decoratedNetworkCall = CircuitBreaker.decorateSupplier(
                    externalAPICircuitBreaker,
                    () -> fetchFromExternalAPI(currencyInput) // Actual RestTemplate logic below
            );

            try {
                // Try running it through the circuit breaker shield
                return decoratedNetworkCall.get();
            }
            catch (CallNotPermittedException e) {
                // This triggers instantly if the breaker is OPEN (saves network resources!)
                return handleCurrencyFallback(currencyInput, "Circuit is OPEN! Blocked call to external vendor.");
            }
            catch (Exception e) {
                // This catches standard API timeouts, 500 errors, or network crashes
                return handleCurrencyFallback(currencyInput, "External API error: " + e.getMessage());
            }
        }
    }

    private CurrencyConvertorResponse fetchFromExternalAPI(CurrencyType currencyInput)
    {
        String apiUrlWithInfo = apiUrl.replace("API_KEY", apiKey).replace("CUR", currencyInput.toString());
        ResponseEntity<CurrencyConvertorResponse> exchange = restTemplate.exchange(apiUrlWithInfo, HttpMethod.GET, null, CurrencyConvertorResponse.class);

        if (exchange.getStatusCode() == HttpStatus.OK) {
            redisService.set("currencies_of_" + currencyInput.toString(), exchange.getBody(), 300L);
            return exchange.getBody();
        } else {
            throw new RuntimeException("No currency found to exchange for: " + currencyInput);
        }
    }

    // --- GRACEFUL FALLBACK SYSTEM ---
    private CurrencyConvertorResponse handleCurrencyFallback(CurrencyType currencyInput, String reason)
    {
        System.err.println("⚠️ [FALLBACK ACTIVE] For currency: " + currencyInput + " | Reason: " + reason);

        throw new ExternalServiceUnavailableException(
                "Currency conversion service is temporarily unavailable due to a remote gateway issue. Please try again shortly."
        );
    }
}
