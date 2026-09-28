package raf.rs.raf_cloud_backend.dto;

import raf.rs.raf_cloud_backend.entity.ScheduledOperationStatus;
import raf.rs.raf_cloud_backend.entity.ScheduledOperationType;

import java.time.LocalDateTime;

public class ScheduledOperationDTO {

    private Long id;
    private Long machineId;
    private ScheduledOperationType operation;
    private LocalDateTime scheduledTime;
    private ScheduledOperationStatus status;
    private Long createdBy;
    private String errorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime executedAt;


    public ScheduledOperationDTO() {
    }

    public ScheduledOperationDTO(Long id, Long machineId, ScheduledOperationType operation,
                                 LocalDateTime scheduledTime, ScheduledOperationStatus status,
                                 Long createdBy, String errorMessage, LocalDateTime createdAt,
                                 LocalDateTime executedAt) {
        this.id = id;
        this.machineId = machineId;
        this.operation = operation;
        this.scheduledTime = scheduledTime;
        this.status = status;
        this.createdBy = createdBy;
        this.errorMessage = errorMessage;
        this.createdAt = createdAt;
        this.executedAt = executedAt;
    }


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMachineId() { return machineId; }
    public void setMachineId(Long machineId) { this.machineId = machineId; }

    public ScheduledOperationType getOperation() { return operation; }
    public void setOperation(ScheduledOperationType operation) { this.operation = operation; }

    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }

    public ScheduledOperationStatus getStatus() { return status; }
    public void setStatus(ScheduledOperationStatus status) { this.status = status; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExecutedAt() { return executedAt; }
    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }
}

