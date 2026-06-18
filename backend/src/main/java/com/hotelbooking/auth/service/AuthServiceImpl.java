package com.hotelbooking.auth.service;

import com.hotelbooking.auth.dto.AuthResponse;
import com.hotelbooking.auth.dto.LoginRequest;
import com.hotelbooking.auth.dto.RegisterRequest;
import com.hotelbooking.auth.entity.User;
import com.hotelbooking.auth.repository.UserRepository;
import com.hotelbooking.common.exception.BusinessException;
import com.hotelbooking.common.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;

    @Transactional
    @Override
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("Email already registered", HttpStatus.CONFLICT);
        }

        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone())
                .build();

        userRepository.save(user);
        return buildAuthResponse(user, generateToken(user));
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        return buildAuthResponse(user, generateToken(user));
    }

    @Override
    public AuthResponse getMe(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return buildAuthResponse(user, null);
    }

    @Transactional
    @Override
    public AuthResponse updateProfile(String email, String fullName, String phone) {
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setFullName(fullName);
        user.setPhone(phone);
        userRepository.save(user);
        return buildAuthResponse(user, null);
    }

    private String generateToken(User user) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getEmail());
        return jwtUtil.generateToken(userDetails, Map.of("role", user.getRole().name()));
    }

    private AuthResponse buildAuthResponse(User user, String token) {
        return AuthResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .token(token)
                .build();
    }
}
