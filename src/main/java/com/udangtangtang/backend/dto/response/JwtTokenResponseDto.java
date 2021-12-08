package com.udangtangtang.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class JwtTokenResponseDto {
    private String accessToken;
    private String refreshToken;

    public JwtTokenResponseDto(String accessToken) {
        this.accessToken = accessToken;
    }
}
