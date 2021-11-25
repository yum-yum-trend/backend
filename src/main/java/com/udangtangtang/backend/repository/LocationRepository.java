package com.udangtangtang.backend.repository;

import com.udangtangtang.backend.domain.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
