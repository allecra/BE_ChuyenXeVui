package com.example.ckdatveexe.shared.repository;

import com.example.ckdatveexe.shared.entity.Post;
import com.example.ckdatveexe.shared.entity.PostStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Integer> {

    Page<Post> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    Page<Post> findByTitleContainingIgnoreCaseAndStatus(
            String title, PostStatus status, Pageable pageable);
}