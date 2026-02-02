package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.DeletedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DeletedUserRepository extends JpaRepository<DeletedUser, Integer> {
    // Thêm nếu cần, ví dụ:
    // Optional<DeletedUser> findByOriginalUserId(Integer originalUserId);
}