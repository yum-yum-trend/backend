package com.udangtangtang.backend.repository;

import com.udangtangtang.backend.domain.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findAllByUserId(Long userId);
    Page<Article> findAllByUserId(Long userId, Pageable pageable);
    Page<Article> findAllByTagsName(String searchTag, Pageable pageable);
    Page<Article> findAll(Pageable pageable);
    Page<Article> findAllByLocationRoadAddressNameStartsWith(Pageable pageable, String location);
    Page<Article> findAllByLocationCategoryName(Pageable pageable, String category);
    Page<Article> findAllByLocationRoadAddressNameStartsWithAndLocationCategoryName(Pageable pageable, String location, String category);
    Page<Article> findAllByLocationRoadAddressNameStartsWithAndTagsName(Pageable pageable, String location, String tagName);

    Page<Article> findAllByTagsNameAndIdLessThan(String searchTag, Pageable pageable, Long articleLastId);
    Page<Article> findAllByIdLessThan(Pageable pageable, Long articleLastId);
    Page<Article> findAllByLocationRoadAddressNameStartsWithAndIdLessThan(Pageable pageable, String location, Long articleLastId);
    Page<Article> findAllByLocationCategoryNameAndIdLessThan(Pageable pageable, String category, Long articleLastId);
    Page<Article> findAllByLocationRoadAddressNameStartsWithAndLocationCategoryNameAndIdLessThan(Pageable pageable, String location, String category, Long articleLastId);
    Page<Article> findAllByLocationRoadAddressNameStartsWithAndTagsNameAndIdLessThan(Pageable pageable, String location, String tagName, Long articleLastId);

}
