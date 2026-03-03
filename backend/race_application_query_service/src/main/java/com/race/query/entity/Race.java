package com.race.query.entity;

import com.race.query.enums.Distance;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "races")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Race {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Distance distance;
}

