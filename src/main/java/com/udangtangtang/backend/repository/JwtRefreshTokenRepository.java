package com.udangtangtang.backend.repository;

import com.udangtangtang.backend.domain.JwtRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JwtRefreshTokenRepository extends JpaRepository<JwtRefreshToken, String> {
}
