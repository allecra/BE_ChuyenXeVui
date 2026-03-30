package com.example.ckdatveexe.module.post.service;

import com.example.ckdatveexe.module.post.dto.*;
import com.example.ckdatveexe.shared.entity.Post;
import com.example.ckdatveexe.shared.entity.PostStatus;
import com.example.ckdatveexe.shared.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    private PostResponse toResponse(Post post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getThumbnail(),
                post.getStatus(),
                post.getCreatedBy(),
                post.getCreatedAt()
        );
    }

    // ================= ADMIN =================
    public Page<PostResponse> getAllForAdmin(String keyword, PostStatus status, Pageable pageable) {

        if (keyword != null && !keyword.isEmpty() && status != null) {
            return postRepository.findByTitleContainingIgnoreCaseAndStatus(keyword, status, pageable)
                    .map(this::toResponse);
        }

        if (status != null) {
            return postRepository.findByStatus(status, pageable)
                    .map(this::toResponse);
        }

        if (keyword != null && !keyword.isEmpty()) {
            return postRepository.findByTitleContainingIgnoreCase(keyword, pageable)
                    .map(this::toResponse);
        }

        return postRepository.findAll(pageable).map(this::toResponse);
    }

    public PostResponse create(PostCreateRequest request, String username, String role) {

        Post post = new Post();
        post.setTitle(request.getTitle().trim());
        post.setContent(request.getContent().trim());
        post.setThumbnail(request.getThumbnail().trim());
        post.setCreatedBy(username);

        // 🔥 LOGIC CHUẨN
        if (role.contains("ADMIN")) {
            post.setStatus(PostStatus.PUBLISHED);
            post.setApprovedBy(username);
            post.setApprovedAt(LocalDateTime.now());
        } else {
            post.setStatus(PostStatus.PENDING);
        }

        return toResponse(postRepository.save(post));
    }

    public PostResponse update(Integer id, PostUpdateRequest request) {
        Post post = findById(id);

        if (request.getTitle() != null) post.setTitle(request.getTitle().trim());
        if (request.getContent() != null) post.setContent(request.getContent().trim());
        if (request.getThumbnail() != null) post.setThumbnail(request.getThumbnail().trim());

        if (request.getStatus() != null) {
            try {
                PostStatus status = PostStatus.valueOf(request.getStatus().toUpperCase());
                post.setStatus(status);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Status không hợp lệ");
            }
        }

        return toResponse(postRepository.save(post));
    }

    public PostResponse approve(Integer id, String admin) {
        Post post = findById(id);

        if (post.getStatus() != PostStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ duyệt bài PENDING");
        }

        post.setStatus(PostStatus.PUBLISHED);
        post.setApprovedBy(admin);
        post.setApprovedAt(LocalDateTime.now());

        return toResponse(postRepository.save(post));
    }

    public PostResponse reject(Integer id, String reason, String admin) {
        Post post = findById(id);

        if (post.getStatus() != PostStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Chỉ reject bài PENDING");
        }

        post.setStatus(PostStatus.REJECTED);
        post.setRejectReason(reason);
        post.setApprovedBy(admin);
        post.setApprovedAt(LocalDateTime.now());

        return toResponse(postRepository.save(post));
    }

    public void delete(Integer id) {
        Post post = findById(id);
        post.setStatus(PostStatus.DELETED);
        postRepository.save(post);
    }

    public PostResponse updateByBus(Integer id, PostUpdateRequest request, String username) {

        Post post = findById(id);

        // chỉ sửa bài của mình
        if (!post.getCreatedBy().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền sửa bài này");
        }

        // chỉ sửa khi PENDING hoặc REJECTED
        if (post.getStatus() != PostStatus.PENDING && post.getStatus() != PostStatus.REJECTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Chỉ được sửa bài khi đang chờ duyệt hoặc bị từ chối");
        }

        if (request.getTitle() != null) post.setTitle(request.getTitle().trim());
        if (request.getContent() != null) post.setContent(request.getContent().trim());
        if (request.getThumbnail() != null) post.setThumbnail(request.getThumbnail().trim());

        // nếu sửa từ REJECTED → quay lại PENDING
        if (post.getStatus() == PostStatus.REJECTED) {
            post.setStatus(PostStatus.PENDING);
            post.setRejectReason(null);
        }

        return toResponse(postRepository.save(post));
    }

    public void deleteByBus(Integer id, String username) {

        Post post = findById(id);

        // chỉ xóa bài của mình
        if (!post.getCreatedBy().equals(username)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Không có quyền xóa bài này");
        }

        // chỉ xóa khi PENDING
        if (post.getStatus() != PostStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Chỉ được xóa bài khi đang chờ duyệt");
        }

        post.setStatus(PostStatus.DELETED);
        postRepository.save(post);
    }

    public Page<PostResponse> getMyPosts(String username, Pageable pageable) {
        return postRepository.findByCreatedBy(username, pageable)
                .map(this::toResponse);
    }

    public PostResponse getById(Integer id) {
        return toResponse(findById(id));
    }

    private Post findById(Integer id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post không tồn tại"));
    }

    // ================= PUBLIC =================
    public Page<PostResponse> getPublished(Pageable pageable) {
        return postRepository.findByStatus(PostStatus.PUBLISHED, pageable)
                .map(this::toResponse);
    }

    public PostResponse getPublishedById(Integer id) {
        Post post = findById(id);
        if (post.getStatus() != PostStatus.PUBLISHED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post không tồn tại");
        }
        return toResponse(post);
    }
}