package org.fiverty.frauddetection.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @Column(nullable = false)
    private String tableName;

    @Column(nullable = false)
    private UUID recordId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Operation operation;

    @Column(columnDefinition = "jsonb")
    private String oldState;

    @Column(columnDefinition = "jsonb")
    private String newState;

    @Column(nullable = false)
    private String executedBy;

    @Column(nullable = false)
    private Instant executedAt;

    public enum Operation {
        INSERT, UPDATE, DELETE
    }
}