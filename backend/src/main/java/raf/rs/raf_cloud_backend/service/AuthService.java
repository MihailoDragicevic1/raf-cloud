package raf.rs.raf_cloud_backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import raf.rs.raf_cloud_backend.dto.*;
import raf.rs.raf_cloud_backend.entity.User;
import raf.rs.raf_cloud_backend.repository.UserRepository;

import java.security.MessageDigest;
import java.util.Optional;
import java.util.Set;

import static raf.rs.raf_cloud_backend.annotation.Permissions.*;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private WebSocketBroadcaster webSocketBroadcaster;

    public LoginResponse login(LoginRequest loginRequest) throws Exception {

        Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());

        if (userOptional.isEmpty()) {
            webSocketBroadcaster.notifyAuthenticationFailed(
                    loginRequest.getEmail(),
                    "User not found"
            );
            throw new Exception("User not found");
        }

        User user = userOptional.get();

        String hashedPassword = hashPassword(loginRequest.getPassword());
        if (!hashedPassword.equals(user.getPassword())) {
            webSocketBroadcaster.notifyAuthenticationFailed(
                    loginRequest.getEmail(),
                    "Invalid password"
            );
            throw new Exception("Invalid password");
        }

        String token = jwtTokenProvider.generateToken(user);

        UserDTO userDTO = convertToDTO(user);

        webSocketBroadcaster.notifyUserLoggedIn(user.getId(), user.getEmail());

        return new LoginResponse(token, userDTO);
    }

    public RegisterResponse register(RegisterRequest registerRequest) throws Exception {

        Optional<User> existingUser = userRepository.findByEmail(registerRequest.getEmail());
        if (existingUser.isPresent()) {
            webSocketBroadcaster.notifyError(
                    "USER_REGISTER_FAILED",
                    "User with this email already exists",
                    null
            );
            throw new Exception("User with this email already exists");
        }

        User newUser = new User();
        newUser.setEmail(registerRequest.getEmail());
        newUser.setName(registerRequest.getName());
        newUser.setLastname(registerRequest.getLastname());
        newUser.setPassword(hashPassword(registerRequest.getPassword()));
        newUser.setRole("User");

        newUser.setPermissions(Set.of(
                READ_MACHINES,
                SEARCH_MACHINES,
                CREATE_MACHINE,
                READ_USER
        ));

        User savedUser = userRepository.save(newUser);

        webSocketBroadcaster.notifyUserCreated(
                savedUser.getId(),
                savedUser.getEmail(),
                "New user registered"
        );

        UserDTO userDTO = convertToDTO(savedUser);
        return new RegisterResponse("Registracija uspešna!", userDTO);
    }

    private String hashPassword(String password) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(password.getBytes("UTF-8"));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private UserDTO convertToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setId(user.getId());
        userDTO.setEmail(user.getEmail());
        userDTO.setName(user.getName());
        userDTO.setLastname(user.getLastname());
        userDTO.setPermissions(user.getPermissions());
        userDTO.setRole(user.getRole());
        return userDTO;
    }
}
