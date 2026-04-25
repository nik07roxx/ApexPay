package com.nik07roxx.apexPay.DTO.User;

// Notice: No password field here!
public record UserCreationResponse(
        Long id,
        String username,
        String email,
        String message // e.g., "User created successfully"
) {}