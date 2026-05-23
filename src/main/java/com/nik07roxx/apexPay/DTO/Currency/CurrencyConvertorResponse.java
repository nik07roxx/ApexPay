package com.nik07roxx.apexPay.DTO.Currency;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Map;

// import com.fasterxml.jackson.databind.ObjectMapper; // version 2.11.1
// import com.fasterxml.jackson.annotation.JsonProperty; // version 2.11.1
/* ObjectMapper om = new ObjectMapper();
Root root = om.readValue(myJsonString, Root.class); */

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CurrencyConvertorResponse {

    @JsonProperty("result")
    public String result;

    @JsonProperty("time_last_update_unix")
    public int timeLastUpdateUnix;

    @JsonProperty("time_last_update_utc")
    public String timeLastUpdateUtc;

    @JsonProperty("time_next_update_unix")
    public int timeNextUpdateUnix;

    @JsonProperty("time_next_update_utc")
    public String timeNextUpdateUtc;

    @JsonProperty("base_code")
    public String baseCode;

    @JsonProperty("conversion_rates")
    private Map<String, BigDecimal> conversionRates;

    public BigDecimal getRate(String currencyCode) {
        if (conversionRates == null) return null;
        return conversionRates.get(currencyCode.toUpperCase());
    }
}