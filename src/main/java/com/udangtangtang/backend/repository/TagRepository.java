package com.udangtangtang.backend.repository;


import com.udangtangtang.backend.domain.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Long> {
    void deleteAllByArticleId(Long articleId);

    @Query("SELECT DISTINCT name FROM Tag")
    List<String> findDistinctNames();
}