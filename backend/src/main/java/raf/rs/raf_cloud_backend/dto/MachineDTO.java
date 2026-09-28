package raf.rs.raf_cloud_backend.dto;

import raf.rs.raf_cloud_backend.entity.MachineStatus;

import java.time.LocalDateTime;

public class MachineDTO {

    private Long id;
    private String name;
    private String type;
    private String description;
    private MachineStatus status;
    private Long createdBy;
    private Boolean active;
    private LocalDateTime createdAt;
    private Boolean isBusy;

    // ----- KONSTRUKTORI -----

    public MachineDTO() {
    }

    public MachineDTO(Long id,
                      String name,
                      String type,
                      String description,
                      MachineStatus status,
                      Long createdBy,
                      Boolean active,
                      LocalDateTime createdAt,
                      Boolean isBusy) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.status = status;
        this.createdBy = createdBy;
        this.active = active;
        this.createdAt = createdAt;
        this.isBusy = isBusy;
    }

    // ----- GETTERI I SETTERI -----

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MachineStatus getStatus() {
        return status;
    }

    public void setStatus(MachineStatus status) {
        this.status = status;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsBusy() {
        return isBusy;
    }

    public void setIsBusy(Boolean isBusy) {
        this.isBusy = isBusy;
    }
}
