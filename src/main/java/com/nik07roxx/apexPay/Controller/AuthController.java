package com.nik07roxx.apexPay.Controller;

import com.nik07roxx.apexPay.DTO.User.UserCreationRequest;
import com.nik07roxx.apexPay.DTO.User.UserCreationResponse;
import com.nik07roxx.apexPay.DTO.User.UserLoginRequest;
import com.nik07roxx.apexPay.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/auth")
@Tag(name = "User Authentication", description = "Endpoints for User Signup and Login")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "Signup a new user(USER/ADMIN)")
    public ResponseEntity<UserCreationResponse> createUser(@Valid @RequestBody UserCreationRequest request)
    {
        return new ResponseEntity<>(userService.createUser(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @Operation(summary = "Login")
    public ResponseEntity<String> verifyUser(@Valid @RequestBody UserLoginRequest userLoginRequest)
    {
        return ResponseEntity.ok(userService.verifyUser(userLoginRequest));
    }
}
