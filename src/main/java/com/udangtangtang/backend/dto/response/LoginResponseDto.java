package com.udangtangtang.backend.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class LoginResponseDto {
    private Long id;
    private String username;
    private String accessToken;
    private String refreshToken;
}
