package com.nik07roxx.apexPay.DTO.User;

import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {}