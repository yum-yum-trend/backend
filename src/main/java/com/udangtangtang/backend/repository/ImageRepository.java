package com.udangtangtang.backend.repository;

import com.udangtangtang.backend.domain.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}
