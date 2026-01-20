package com.example.ckdatveexe.config;

import com.example.ckdatveexe.shared.entity.Role;
import com.example.ckdatveexe.shared.entity.RoleName;
import com.example.ckdatveexe.shared.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        initializeRoles();
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
}