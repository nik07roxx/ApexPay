package com.nik07roxx.apexPay.Service;

import com.nik07roxx.apexPay.DTO.User.UserCreationRequest;
import com.nik07roxx.apexPay.DTO.User.UserCreationResponse;
import com.nik07roxx.apexPay.DTO.User.UserLoginRequest;

public interface UserService {
    UserCreationResponse createUser(UserCreationRequest userCreationRequest);
    String verifyUser(UserLoginRequest userLoginRequest);
}
