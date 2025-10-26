package com.polstat.simkas.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "activity_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // actor who performed action (nullable)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id")
    private User actor;

    private String aksi;

    private String targetTable;

    private Long targetId;

    @Column(columnDefinition = "json")
    private String payload; // store JSON as string

    @Column(name = "created_at", updatable = false)
    private Instant createdAt = Instant.now();
}
