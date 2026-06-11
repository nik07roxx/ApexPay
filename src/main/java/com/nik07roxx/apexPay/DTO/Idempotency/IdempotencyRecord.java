package com.nik07roxx.apexPay.DTO.Idempotency;

import com.nik07roxx.apexPay.model.RequestStatus;
import java.io.Serializable;

public class IdempotencyRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    private RequestStatus status;
    private int responseStatusCode;
    private String responseBody; // JSON string representation

    public IdempotencyRecord() {}

    public IdempotencyRecord(RequestStatus status) {
        this.status = status;
    }

    public IdempotencyRecord(RequestStatus status, int responseStatusCode, String responseBody) {
        this.status = status;
        this.responseStatusCode = responseStatusCode;
        this.responseBody = responseBody;
    }

    // Getters and Setters
    public RequestStatus getStatus() { return status; }
    public void setStatus(RequestStatus status) { this.status = status; }
    public int getResponseStatusCode() { return responseStatusCode; }
    public void setResponseStatusCode(int responseStatusCode) { this.responseStatusCode = responseStatusCode; }
    public String getResponseBody() { return responseBody; }
    public void setResponseBody(String responseBody) { this.responseBody = responseBody; }
}
