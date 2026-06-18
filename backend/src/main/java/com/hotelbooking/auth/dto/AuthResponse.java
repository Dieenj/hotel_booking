package com.hotelbooking.auth.dto;

import com.hotelbooking.auth.enums.Role;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class AuthResponse {
    private Long id;
    private String email;
    private String fullName;
    private String phone;
    private Role role;
    private String token;
}
