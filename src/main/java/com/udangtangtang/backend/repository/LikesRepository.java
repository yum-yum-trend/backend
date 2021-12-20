package com.udangtangtang.backend.repository;

import com.udangtangtang.backend.domain.Likes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikesRepository extends JpaRepository<Likes, Long> {
    Page<Likes> findAllByUserId(Long userId, Pageable pageable);
    Likes findByUserId(Long userId);
    Likes findByArticleId(Long articleId);
    Optional<Likes> findByUserIdAndArticleId(Long userId, Long articleId);
    Long countByArticleId(Long articleId);
}
