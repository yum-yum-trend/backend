package com.udangtangtang.backend.dto.request;

import com.udangtangtang.backend.validation.ValidationGroups;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SignupUsernameRequestDto {
    @NotBlank(message = "아이디는 필수 입력값입니다.", groups = ValidationGroups.NotEmptyGroup.class)
    @Pattern(regexp = "[a-zA-Z0-9]{2,9}",
             message = "아이디는 영문, 숫자만 가능하며 2 ~ 10자리까지 가능합니다.", groups = ValidationGroups.PatternCheckGroup.class)
    private String username;
}
