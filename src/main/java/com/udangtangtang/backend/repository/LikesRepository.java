package com.udangtangtang.backend.repository;

import com.udangtangtang.backend.domain.Likes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikesRepository extends JpaRepository<Likes, Long> {
    Likes findByUserId(Long userId);
    Likes findByArticleId(Long articleId);
    Optional<Likes> findByUserIdAndArticleId(Long userId, Long articleId);
    Long countByArticleId(Long articleId);
}
