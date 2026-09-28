package raf.rs.raf_cloud_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "error_logs")
public class Error {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long machineId;

    @Column(nullable = false)
    private String operation;

    @Column(nullable = false)
    private String message;

    @Column(nullable = false)
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(nullable = false)
    private Long userId;

    // ----- KONSTRUKTORI -----

    public Error() {
    }

    public Error(Long machineId, String operation, String message, Long userId) {
        this.machineId = machineId;
        this.operation = operation;
        this.message = message;
        this.userId = userId;
        this.timestamp = LocalDateTime.now();
    }

    // ----- GETTERI I SETTERI -----

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMachineId() { return machineId; }
    public void setMachineId(Long machineId) { this.machineId = machineId; }

    public String getOperation() { return operation; }
    public void setOperation(String operation) { this.operation = operation; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
}

