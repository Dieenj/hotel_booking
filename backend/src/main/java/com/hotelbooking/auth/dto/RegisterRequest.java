package com.hotelbooking.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 255, message = "Full name must be at most 255 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must be at most 255 characters")
    private String email;

    @NotBlank(message = "Password is required")
    // BCrypt only uses the first 72 bytes.
    @Size(min = 6, max = 72, message = "Password must be 6-72 characters")
    private String password;

    @Size(max = 30, message = "Phone must be at most 30 characters")
    private String phone;
}
