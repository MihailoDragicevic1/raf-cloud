package raf.rs.raf_cloud_backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import raf.rs.raf_cloud_backend.annotation.Permissions;
import raf.rs.raf_cloud_backend.entity.User;
import raf.rs.raf_cloud_backend.repository.UserRepository;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initializeData(UserRepository userRepository) {
        return args -> {
            if (userRepository.findByEmail("admin@raf.rs").isEmpty()) {
                User admin = new User();
                admin.setEmail("admin@raf.rs");
                admin.setPassword(hashPassword("admin123"));
                admin.setName("Admin");
                admin.setLastname("Administrator");
                admin.setRole("Administrator");

                Set<String> adminPermissions = new HashSet<>();
                adminPermissions.add(Permissions.CREATE_USER);
                adminPermissions.add(Permissions.READ_USER);
                adminPermissions.add(Permissions.UPDATE_USER);
                adminPermissions.add(Permissions.DELETE_USER);
                adminPermissions.add(Permissions.SEARCH_MACHINES);
                adminPermissions.add(Permissions.POWER_ON_MACHINE);
                adminPermissions.add(Permissions.POWER_OFF_MACHINE);
                adminPermissions.add(Permissions.RESTART_MACHINE);
                adminPermissions.add(Permissions.CREATE_MACHINE);
                adminPermissions.add(Permissions.DESTROY_MACHINE);

                admin.setPermissions(adminPermissions);
                userRepository.save(admin);
                System.out.println("Admin korisnik kreiran: admin@raf.rs / admin123");
            }
        };
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