package com.hotelbooking.auth.entity;

import com.hotelbooking.auth.enums.Role;
import com.hotelbooking.common.entity.AuditableEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "users")
@Getter
@SuperBuilder
@NoArgsConstructor
public class User extends AuditableEntity {

    @Column(unique = true, nullable = false)
    private String email;

    @Setter
    @Column(nullable = false)
    private String password;

    @Setter
    @Column(nullable = false)
    private String fullName;

    @Setter
    private String phone;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.GUEST;

    @Setter
    @Builder.Default
    private boolean active = true;
}
