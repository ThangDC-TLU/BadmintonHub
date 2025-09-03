package com.badmintonhub.notiservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@RequiredArgsConstructor

@Entity
@Table(name = "notifications")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String title;

    @NotNull
    @PastOrPresent
    @Column(nullable = false)
    private Instant timestamp;

    @NotBlank
    @Column(nullable = false, length = 1000)
    private String message;

    @PrePersist
    public void prePersist() {
        if (timestamp == null) timestamp = Instant.now();
        if (title != null) title = title.trim();
        if (message != null) message = message.trim();
    }

    @PreUpdate
    public void preUpdate() {
        if (title != null) title = title.trim();
        if (message != null) message = message.trim();
    }
}

