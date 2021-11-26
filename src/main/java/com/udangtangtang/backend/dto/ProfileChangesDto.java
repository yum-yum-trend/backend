package com.udangtangtang.backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProfileChangesDto {
    private String nowPassword;
    private String newPassword;
    private String userProfileIntro;
}
