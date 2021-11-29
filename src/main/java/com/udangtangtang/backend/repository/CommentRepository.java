package com.udangtangtang.backend.repository;

import com.udangtangtang.backend.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findAllByArticleId (Long articleId);
}
