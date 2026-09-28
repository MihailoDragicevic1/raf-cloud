package raf.rs.raf_cloud_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import raf.rs.raf_cloud_backend.entity.User;
import raf.rs.raf_cloud_backend.exception.InvalidOperationException;
import raf.rs.raf_cloud_backend.exception.ResourceNotFoundException;
import raf.rs.raf_cloud_backend.repository.UserRepository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WebSocketBroadcaster webSocketBroadcaster;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User createUser(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            webSocketBroadcaster.notifyError(
                    "USER_CREATE_FAILED",
                    "Email already exists: " + user.getEmail(),
                    null
            );
            throw new InvalidOperationException("Email already exists: " + user.getEmail());
        }

        user.setPassword(hashPassword(user.getPassword()));

        User savedUser = userRepository.save(user);

        webSocketBroadcaster.notifyUserCreated(
                savedUser.getId(),
                savedUser.getEmail(),
                savedUser.getEmail()
        );

        return savedUser;
    }

    public User updateUser(Long id, User updatedUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> {
                    webSocketBroadcaster.notifyError(
                            "USER_UPDATE_FAILED",
                            "User not found with ID: " + id,
                            id
                    );
                    return new ResourceNotFoundException("User not found with ID: " + id);
                });

        existingUser.setName(updatedUser.getName());
        existingUser.setLastname(updatedUser.getLastname());
        existingUser.setEmail(updatedUser.getEmail());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(hashPassword(updatedUser.getPassword()));
        }

        if (updatedUser.getPermissions() != null) {
            existingUser.setPermissions(updatedUser.getPermissions());
        }

        User savedUser = userRepository.save(existingUser);

        webSocketBroadcaster.notifyUserProfileUpdated(
                savedUser.getId(),
                savedUser.getEmail()
        );

        return savedUser;
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            webSocketBroadcaster.notifyError(
                    "USER_DELETE_FAILED",
                    "User not found with ID: " + id,
                    id
            );
            throw new ResourceNotFoundException("User not found with ID: " + id);
        }

        Optional<User> userOpt = userRepository.findById(id);
        String email = userOpt.map(User::getEmail).orElse("unknown");

        userRepository.deleteById(id);

        webSocketBroadcaster.notifyUserDeleted(id, email);
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}


