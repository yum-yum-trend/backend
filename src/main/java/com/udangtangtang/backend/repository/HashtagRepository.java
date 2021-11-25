package com.udangtangtang.backend.repository;

import com.udangtangtang.backend.domain.Hashtag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashtagRepository extends JpaRepository<Hashtag, Long> {
}
