package raf.rs.raf_cloud_backend.dto;

import jakarta.validation.constraints.NotBlank;

public class MachineCreateRequestDTO {

    @NotBlank(message = "Naziv je obavezan")
    private String name;

    @NotBlank(message = "Tip je obavezan")
    private String type;

    @NotBlank(message = "Opis je obavezan")
    private String description;

    public MachineCreateRequestDTO() {
    }

    public MachineCreateRequestDTO(String name, String type, String description) {
        this.name = name;
        this.type = type;
        this.description = description;
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
}

