package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "queues")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Queue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueueType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QueueStatus status;

    @Column(name = "workspace_id", nullable = false)
    private Long workspaceId;

    @Column(name = "average_service_time_minutes")
    private Integer averageServiceTimeMinutes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "queue", cascade = CascadeType.ALL)
    @JsonManagedReference
    @Builder.Default
    private List<Client> clients = new ArrayList<>();

    public enum QueueType {
        FIFO,
        LIFO,
        PRIORITY,
        VIP
    }

    public enum QueueStatus {
        ACTIVE,
        PAUSED,
        CLOSED
    }
}
