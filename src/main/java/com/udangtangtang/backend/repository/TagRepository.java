package com.udangtangtang.backend.repository;


import com.udangtangtang.backend.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TagRepository extends JpaRepository<Tag, Long> {
    void deleteAllByArticleId(Long articleId);
}