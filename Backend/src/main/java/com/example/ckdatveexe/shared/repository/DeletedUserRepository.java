package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.DeletedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeletedUserRepository extends JpaRepository<DeletedUser, Integer> {

    Page<DeletedUser> findAllByOrderByDeletedAtDesc(Pageable pageable);

    Optional<DeletedUser> findByOriginalUserId(Integer originalUserId);

    boolean existsByOriginalUserId(Integer originalUserId);
}