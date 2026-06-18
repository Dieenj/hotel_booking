package com.hotelbooking.auth.service;

import com.hotelbooking.auth.dto.AuthResponse;
import com.hotelbooking.auth.dto.LoginRequest;
import com.hotelbooking.auth.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse getMe(String email);
    AuthResponse updateProfile(String email, String fullName, String phone);
}
