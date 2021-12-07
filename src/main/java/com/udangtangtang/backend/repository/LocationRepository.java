package com.udangtangtang.backend.repository;

import com.udangtangtang.backend.domain.Location;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LocationRepository extends JpaRepository<Location, Long> {
}
