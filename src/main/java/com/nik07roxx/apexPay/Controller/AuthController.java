package com.nik07roxx.apexPay.Controller;

import com.nik07roxx.apexPay.DTO.User.UserCreationRequest;
import com.nik07roxx.apexPay.DTO.User.UserCreationResponse;
import com.nik07roxx.apexPay.DTO.User.UserLoginRequest;
import com.nik07roxx.apexPay.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserCreationResponse> createUser(@Valid @RequestBody UserCreationRequest request)
    {
        return new ResponseEntity<>(userService.createUser(request), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<String> verifyUser(@Valid @RequestBody UserLoginRequest userLoginRequest)
    {
        return ResponseEntity.ok(userService.verifyUser(userLoginRequest));
    }
}
