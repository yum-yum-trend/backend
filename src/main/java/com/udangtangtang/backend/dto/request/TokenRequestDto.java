package com.udangtangtang.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TokenRequestDto {
    private String accessToken;
    private String refreshToken;
}