package raf.rs.raf_cloud_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_operations")
public class ScheduledOperation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "machine_id", nullable = false)
    private Machine machine;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduledOperationType operation;

    @Column(nullable = false)
    private LocalDateTime scheduledTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduledOperationStatus status = ScheduledOperationStatus.PENDING;

    @Column(nullable = false)
    private Long createdBy;

    @Column
    private String errorMessage;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column
    private LocalDateTime executedAt;


    public ScheduledOperation() {
    }

    public ScheduledOperation(Machine machine, ScheduledOperationType operation,
                              LocalDateTime scheduledTime, Long createdBy) {
        this.machine = machine;
        this.operation = operation;
        this.scheduledTime = scheduledTime;
        this.createdBy = createdBy;
        this.status = ScheduledOperationStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Machine getMachine() {
        return machine;
    }

    public void setMachine(Machine machine) {
        this.machine = machine;
    }

    public Long getMachineId() {
        return machine != null ? machine.getId() : null;
    }


    public ScheduledOperationType getOperation() {
        return operation;
    }

    public void setOperation(ScheduledOperationType operation) {
        this.operation = operation;
    }

    public LocalDateTime getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(LocalDateTime scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public ScheduledOperationStatus getStatus() {
        return status;
    }

    public void setStatus(ScheduledOperationStatus status) {
        this.status = status;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(LocalDateTime executedAt) {
        this.executedAt = executedAt;
    }
}

