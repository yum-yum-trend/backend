package com.udangtangtang.backend.dto.request;

import com.udangtangtang.backend.validation.ValidationGroups;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class SignupRequestDto {
    @NotBlank(message = "아이디는 필수 입력값입니다.", groups = ValidationGroups.NotEmptyGroup.class)
    @Pattern(regexp = "[a-zA-Z0-9]{2,9}",
             message = "아이디는 영문, 숫자만 가능하며 2 ~ 10자리까지 가능합니다.", groups = ValidationGroups.PatternCheckGroup.class)
    private String username;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.", groups = ValidationGroups.NotEmptyGroup.class)
    @Pattern(regexp = "^(?=.*\\d)(?=.*[a-zA-Z])[0-9a-zA-Z]{8,16}",
             message = "비밀번호는 영문과 숫자 조합으로 8 ~ 16자리까지 가능합니다.", groups = ValidationGroups.PatternCheckGroup.class)
    private String password;

    @NotBlank(message = "이메일은 필수 입력값입니다.", groups = ValidationGroups.NotEmptyGroup.class)
    @Pattern(regexp = "^[0-9a-zA-Z]([-_\\.]?[0-9a-zA-Z])*@[0-9a-zA-Z]([-_\\.]?[0-9a-zA-Z])*\\.[a-zA-Z]{2,3}",
             message = "올바르지 않은 이메일 형식입니다.", groups = ValidationGroups.PatternCheckGroup.class)
    private String email;

    private boolean admin = false;
    private String adminToken = "";

    public SignupRequestDto(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
}
