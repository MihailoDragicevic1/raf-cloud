package raf.rs.raf_cloud_backend.dto;

import jakarta.validation.constraints.NotNull;
import raf.rs.raf_cloud_backend.entity.ScheduledOperationType;

import java.time.LocalDateTime;

public class ScheduledOperationCreateRequestDTO {


    @NotNull(message = "Machine ID je obavezan")
    private Long machineId;

    @NotNull(message = "Operacija je obavezna")
    private ScheduledOperationType operation;

    @NotNull(message = "Zakazano vreme je obavezno")
    private LocalDateTime scheduledTime;


    public ScheduledOperationCreateRequestDTO() {
    }

    public ScheduledOperationCreateRequestDTO(Long machineId, ScheduledOperationType operation,
                                              LocalDateTime scheduledTime) {
        this.machineId = machineId;
        this.operation = operation;
        this.scheduledTime = scheduledTime;
    }


    public Long getMachineId() { return machineId; }
    public void setMachineId(Long machineId) { this.machineId = machineId; }

    public ScheduledOperationType getOperation() { return operation; }
    public void setOperation(ScheduledOperationType operation) { this.operation = operation; }

    public LocalDateTime getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(LocalDateTime scheduledTime) { this.scheduledTime = scheduledTime; }
}

