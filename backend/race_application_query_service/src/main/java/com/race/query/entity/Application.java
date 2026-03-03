package com.race.query.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 255)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 255)
    private String lastName;

    @Column(length = 255)
    private String club;

    @Column(name = "race_id", nullable = false)
    private UUID raceId;
}

