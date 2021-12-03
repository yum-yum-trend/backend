package com.udangtangtang.backend.repository;

import com.udangtangtang.backend.domain.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findAllByUserId(Long userId);
    Page<Article> findAllByTagsName(String searchTag, Pageable pageable);
    Page<Article> findAll(Pageable pageable);
}
