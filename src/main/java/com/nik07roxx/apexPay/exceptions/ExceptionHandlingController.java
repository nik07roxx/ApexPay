package com.nik07roxx.apexPay.exceptions;

import com.nik07roxx.apexPay.DTO.Error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class ExceptionHandlingController {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatusException(ResponseStatusException exception)
    {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                exception.getStatusCode().value(),
                ((HttpStatus)exception.getStatusCode()).getReasonPhrase(),
                exception.getReason(),
                null
        );
        return new ResponseEntity<>(errorResponse,exception.getStatusCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation Failed",
                "Please check the fields and try again.",
                errors
        );
        return new ResponseEntity<>(errorResponse,HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({AccountNotFoundException.class,
            CustomerNotFoundException.class,
            InsufficientFundsException.class,
            InvalidRequestException.class})
    public ResponseEntity<ErrorResponse> handleValidationExceptions(RuntimeException ex) {

                HttpStatus status = HttpStatus.BAD_REQUEST;
                if (ex instanceof AccountNotFoundException || ex instanceof CustomerNotFoundException) {
                    status = HttpStatus.NOT_FOUND;
                }

                ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(), status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                null
        );
        return new ResponseEntity<>(errorResponse, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred. Please try again later.",
                null
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(AccessDeniedException ex,
                                                                           HttpServletRequest request) {
        Map<String, Object> errorResponse = new HashMap<>();

        // Add custom details
        errorResponse.put("timestamp", LocalDateTime.now());
        errorResponse.put("status", HttpStatus.FORBIDDEN.value());
        errorResponse.put("error", "Access Denied");
        errorResponse.put("message", "You do not have the required administrative privileges to perform this action.");
        errorResponse.put("path", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
}
