package com.udangtangtang.backend.repository;

import com.udangtangtang.backend.domain.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findAllByUserId(Long userId);
    Page<Article> findAllByUserId(Long userId, Pageable pageable);
    Page<Article> findAllByTagsName(String searchTag, Pageable pageable);
    Page<Article> findAll(Pageable pageable);
    Page<Article> findAllByLocationRoadAddressNameStartsWith(Pageable pageable, String location);
    Page<Article> findAllByLocationCategoryName(Pageable pageable, String category);
    Page<Article> findAllByLocationRoadAddressNameStartsWithAndLocationCategoryName(Pageable pageable, String location, String category);
}
