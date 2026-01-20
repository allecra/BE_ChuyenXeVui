package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.Role;
import com.example.ckdatveexe.shared.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByRoleName(RoleName roleName);
}