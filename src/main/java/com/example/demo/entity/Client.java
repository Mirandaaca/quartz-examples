package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "clients")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String phone;

    @ManyToOne
    @JoinColumn(name = "queue_id")
    @JsonBackReference
    private Queue queue;

    @Column(nullable = false)
    private Integer position;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClientStatus status;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "postponed_at")
    private LocalDateTime postponedAt;

    @Column(name = "postpone_minutes")
    private Integer postponeMinutes;

    @Column(name = "notified_at")
    private LocalDateTime notifiedAt;

    public enum ClientStatus {
        WAITING,
        POSTPONED,
        NOTIFIED,
        ATTENDING,
        ATTENDED,
        CANCELLED,
        EXPIRED
    }
}
