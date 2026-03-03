package com.race.command.entity;

import com.race.command.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "first_name", length = 255)
    private String firstName;

    @Column(name = "last_name", length = 255)
    private String lastName;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column
    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Role role;

    @Column(nullable = false, length = 255)
    private String password;
}

