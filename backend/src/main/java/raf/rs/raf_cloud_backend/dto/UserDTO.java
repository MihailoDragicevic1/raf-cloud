package raf.rs.raf_cloud_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public class UserDTO {
    private Long id;
    @NotBlank(message = "Email je obavezan")
    @Email(message = "Email mora biti validan")
    private String email;
    @NotBlank(message = "Ime je obavezno")
    private String name;
    @NotBlank(message = "Prezime je obavezno")
    private String lastname;
    @NotBlank(message = "Lozinka je obavezna")
    private String password;
    @NotNull(message = "Permissions su obavezni")
    private Set<String> permissions;
    @NotNull(message = "Role je obavezan")
    private String role;
    public UserDTO() {}

    public UserDTO(Long id, String email, String name, String lastname, Set<String> permissions) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.lastname = lastname;
        this.permissions = permissions;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Set<String> getPermissions() {
        return permissions;
    }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public void setPermissions(Set<String> permissions) {
        this.permissions = permissions;
    }
    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}

