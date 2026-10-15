package com.hotelbooking.auth.repository;

import com.hotelbooking.auth.entity.User;
import com.hotelbooking.common.exception.ResourceNotFoundException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Emails are matched case-insensitively ("Smith@x.com" and "smith@x.com" are one account);
    // new accounts store them lower-cased.
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);

    default User getByEmailOrThrow(String email) {
        return findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
