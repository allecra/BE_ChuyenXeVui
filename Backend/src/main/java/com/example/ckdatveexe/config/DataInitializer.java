package com.example.ckdatveexe.config;

import com.example.ckdatveexe.shared.entity.Role;
import com.example.ckdatveexe.shared.entity.RoleName;
import com.example.ckdatveexe.shared.entity.User;
import com.example.ckdatveexe.shared.entity.UserStatus;
import com.example.ckdatveexe.shared.repository.RoleRepository;
import com.example.ckdatveexe.shared.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
        initializeAdminUser();
    }

    private void initializeRoles() {
        try {
            // Đảm bảo các role được tạo theo thứ tự để có ID cố định
            createRoleIfNotExists(RoleName.ROLE_ADMIN);
            createRoleIfNotExists(RoleName.ROLE_USER); // Đây sẽ là ID = 2
            createRoleIfNotExists(RoleName.ROLE_DIRECT_STATION);

            log.info("Role initialization completed - ROLE_USER should have ID: 2");
        } catch (Exception e) {
            log.error("Failed to initialize roles", e);
        }
    }

    private void createRoleIfNotExists(RoleName roleName) {
        if (roleRepository.findByRoleName(roleName).isEmpty()) {
            Role role = new Role();
            role.setRoleName(roleName);
            Role savedRole = roleRepository.save(role);
            log.info("Created role: {} with ID: {}", roleName, savedRole.getId());
        } else {
            Role existingRole = roleRepository.findByRoleName(roleName).get();
            log.info("Role already exists: {} with ID: {}", roleName, existingRole.getId());
        }
    }

    private void initializeAdminUser() {
        try {
            String adminEmail = "admin@ckdatveexe.com";

            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                Role adminRole = roleRepository.findByRoleName(RoleName.ROLE_ADMIN)
                        .orElseThrow(() -> new RuntimeException("ROLE_ADMIN not found"));

                User adminUser = new User();
                adminUser.setEmail(adminEmail);
                adminUser.setPassword(passwordEncoder.encode("MatKhau@123"));
                adminUser.setFirstName("Admin");
                adminUser.setLastName("System");
                adminUser.setPhone("0123456789");
                adminUser.setStatus(UserStatus.ACTIVE);
                adminUser.setRoles(Set.of(adminRole));

                User savedAdmin = userRepository.save(adminUser);
                log.info("Created admin user: {} with ID: {}", adminEmail, savedAdmin.getId());
            } else {
                log.info("Admin user already exists: {}", adminEmail);
            }
        } catch (Exception e) {
            log.error("Failed to initialize admin user", e);
        }
    }
}