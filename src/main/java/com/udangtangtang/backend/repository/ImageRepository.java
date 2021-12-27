package com.udangtangtang.backend.repository;

import com.udangtangtang.backend.domain.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImageRepository extends JpaRepository<Image, Long> {
    List<Image> findByIdIn(List<Long> imageIds);
    List<Image> findAllByArticleId(Long articleId);
}
