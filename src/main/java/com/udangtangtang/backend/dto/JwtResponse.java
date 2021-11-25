package com.udangtangtang.backend.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class JwtResponse{

    private final String token;
    private final Long id;
    private final String username;
}
