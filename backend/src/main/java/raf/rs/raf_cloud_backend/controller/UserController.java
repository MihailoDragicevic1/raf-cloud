package raf.rs.raf_cloud_backend.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import raf.rs.raf_cloud_backend.annotation.Permissions;
import raf.rs.raf_cloud_backend.annotation.Secured;
import raf.rs.raf_cloud_backend.dto.UserDTO;
import raf.rs.raf_cloud_backend.entity.User;
import raf.rs.raf_cloud_backend.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @Secured(Permissions.READ_USER)
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @Secured(Permissions.READ_USER)
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Secured(Permissions.CREATE_USER)
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDTO userDTO) {
        try {
            User user = new User();
            user.setEmail(userDTO.getEmail());
            user.setName(userDTO.getName());
            user.setLastname(userDTO.getLastname());
            user.setPassword(userDTO.getPassword());
            user.setPermissions(userDTO.getPermissions());

            User createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    @Secured(Permissions.UPDATE_USER)
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody UserDTO userDTO) {
        try {
            User user = new User();
            user.setId(id);
            user.setEmail(userDTO.getEmail());
            user.setName(userDTO.getName());
            user.setLastname(userDTO.getLastname());
            user.setPassword(userDTO.getPassword());
            user.setPermissions(userDTO.getPermissions());

            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Secured(Permissions.DELETE_USER)
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

