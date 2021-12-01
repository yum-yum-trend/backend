package com.udangtangtang.backend.repository;

import com.udangtangtang.backend.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findAllByUserId(Long userId);
    List<Article> findAllByTagsName(String searchTag);
}
