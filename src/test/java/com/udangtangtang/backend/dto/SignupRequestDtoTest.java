package com.udangtangtang.backend.dto;

import com.udangtangtang.backend.dto.request.SignupRequestDto;
import com.udangtangtang.backend.validation.ValidationGroups;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
public class SignupRequestDtoTest {
    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    public static void init(){
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Nested
    @DisplayName("정상 케이스")
    class Success {
        @Test
        @DisplayName("유효한 회원가입 요청 데이터")
        void success() {
            SignupRequestDto signupRequestDto = new SignupRequestDto("Kermit", "kermit1234", "kermit@gaegul.com");

            Set<ConstraintViolation<SignupRequestDto>> validations1 = validator.validate(signupRequestDto, ValidationGroups.PatternCheckGroup.class);
            Set<ConstraintViolation<SignupRequestDto>> validations2 = validator.validate(signupRequestDto, ValidationGroups.NotEmptyGroup.class);

            assertEquals(validations1.size(), 0);
            assertEquals(validations2.size(), 0);
        }

        @Nested
        class Email {
            @Test
            @DisplayName("이메일 (-) 포함")
            void firstCharacterIsDash() {
                SignupRequestDto signupRequestDto = new SignupRequestDto("Kermit", "kermit1234", "ker-mit@gaegul.com");

                Set<ConstraintViolation<SignupRequestDto>> validations1 = validator.validate(signupRequestDto, ValidationGroups.PatternCheckGroup.class);
                Set<ConstraintViolation<SignupRequestDto>> validations2 = validator.validate(signupRequestDto, ValidationGroups.NotEmptyGroup.class);

                assertEquals(validations1.size(), 0);
                assertEquals(validations2.size(), 0);
            }

            @Test
            @DisplayName("이메일 (_) 포함")
            void firstCharacterIsUnderscore() {
                SignupRequestDto signupRequestDto = new SignupRequestDto("Kermit", "kermit1234", "ker_mit@gaegul.com");

                Set<ConstraintViolation<SignupRequestDto>> validations1 = validator.validate(signupRequestDto, ValidationGroups.PatternCheckGroup.class);
                Set<ConstraintViolation<SignupRequestDto>> validations2 = validator.validate(signupRequestDto, ValidationGroups.NotEmptyGroup.class);

                assertEquals(validations1.size(), 0);
                assertEquals(validations2.size(), 0);
            }

            @Test
            @DisplayName("이메일 (.) 포함")
            void firstCharacterIsDot() {
                SignupRequestDto signupRequestDto = new SignupRequestDto("Kermit", "kermit1234", "ker.mit@gaegul.com");

                Set<ConstraintViolation<SignupRequestDto>> validations1 = validator.validate(signupRequestDto, ValidationGroups.PatternCheckGroup.class);
                Set<ConstraintViolation<SignupRequestDto>> validations2 = validator.validate(signupRequestDto, ValidationGroups.NotEmptyGroup.class);

                assertEquals(validations1.size(), 0);
                assertEquals(validations2.size(), 0);
            }

            @Test
            @DisplayName("이메일 특수기호(-_.) 포함")
            void specialSymbol() {
                SignupRequestDto signupRequestDto = new SignupRequestDto("Kermit", "kermit1234", "ker-m_i.t@gae-g_u.l.com");

                Set<ConstraintViolation<SignupRequestDto>> validations1 = validator.validate(signupRequestDto, ValidationGroups.PatternCheckGroup.class);
                Set<ConstraintViolation<SignupRequestDto>> validations2 = validator.validate(signupRequestDto, ValidationGroups.NotEmptyGroup.class);

                assertEquals(validations1.size(), 0);
                assertEquals(validations2.size(), 0);
            }
        }
    }

    @Nested
    @DisplayName("비정상 케이스")
    class Fail {
        @Nested
        @DisplayName("미입력")
        class Empty {
            @Test
            @DisplayName("아이디 미입력_빈문자열")
            void inputEmptyUsername_emptyString() {
                SignupRequestDto signupRequestDto = new SignupRequestDto("", "kermit1234", "kermit@gaegul.com");
                Set<ConstraintViolation<SignupRequestDto>> validations = validator.validate(signupRequestDto, ValidationGroups.NotEmptyGroup.class);

                assertEquals(validations.size(), 1);
                validations.forEach(
                        error -> {
                            assertThat(error.getMessage()).isEqualTo("아이디는 필수 입력값입니다.");
                        }
                );
            }

            @Test
            @DisplayName("아이디 미입력_널값")
            void inputEmptyUsername_null() {
                SignupRequestDto signupRequestDto = new SignupRequestDto(null, "kermit1234", "kermit@gaegul.com");
                Set<ConstraintViolation<SignupRequestDto>> validations = validator.validate(signupRequestDto, ValidationGroups.NotEmptyGroup.class);

                assertEquals(validations.size(), 1);
                validations.forEach(
                        error -> {
                            assertThat(error.getMessage()).isEqualTo("아이디는 필수 입력값입니다.");
                        }
                );
            }

            @Test
            @DisplayName("비밀번호 미입력")
            void inputEmptyPassword() {
                SignupRequestDto signupRequestDto = new SignupRequestDto("Kermit", "", "kermit@gaegul.com");
                Set<ConstraintViolation<SignupRequestDto>> validations = validator.validate(signupRequestDto, ValidationGroups.NotEmptyGroup.class);

                assertEquals(validations.size(), 1);
                validations.forEach(
                        error -> {
                            assertThat(error.getMessage()).isEqualTo("비밀번호는 필수 입력값입니다.");
                        }
                );
            }

            @Test
            @DisplayName("이메일 미입력")
            void inputEmptyEmail() {
                SignupRequestDto signupRequestDto = new SignupRequestDto("Kermit", "kermit1234", "");
                Set<ConstraintViolation<SignupRequestDto>> validations = validator.validate(signupRequestDto, ValidationGroups.NotEmptyGroup.class);

                assertEquals(validations.size(), 1);
                validations.forEach(
                        error -> {
                            assertThat(error.getMessage()).isEqualTo("이메일은 필수 입력값입니다.");
                        }
                );
            }
        }

        @Nested
        @DisplayName("아이디 형식 불일치")
        class UsernameFormatInconsistency {
            @Test
            @DisplayName("2자리 미만")
            void lessThan2Digit() {
                SignupRequestDto signupRequestDto = new SignupRequestDto("k", "kermit1234", "kermit@gaegul.com");
                Set<ConstraintViolation<SignupRequestDto>> validations = validator.validate(signupRequestDto, ValidationGroups.PatternCheckGroup.class);

                assertEquals(validations.size(), 1);
                validations.forEach(
                        error -> {
                            assertThat(error.getMessage()).isEqualTo("아이디는 영문, 숫자만 가능하며 2 ~ 10자리까지 가능합니다.");
                        }
                );
            }

            @Test
            @DisplayName("10자리 초과")
            void moreThan10Digit() {
                SignupRequestDto signupRequestDto = new SignupRequestDto("KermitKermit", "kermit1234", "kermit@gaegul.com");
                Set<ConstraintViolation<SignupRequestDto>> validations = validator.validate(signupRequestDto, ValidationGroups.PatternCheckGroup.class);

                assertEquals(validations.size(), 1);
                validations.forEach(
                        error -> {
                            assertThat(error.getMessage()).isEqualTo("아이디는 영문, 숫자만 가능하며 2 ~ 10자리까지 가능합니다.");
                        }
                );
            }

            @Test
            @DisplayName("한글 입력")
            void korean() {
                SignupRequestDto signupRequestDto = new SignupRequestDto("커밋은개구리", "kermit1234", "kermit@gaegul.com");
                Set<ConstraintViolation<SignupRequestDto>> validations = validator.validate(signupRequestDto, ValidationGroups.PatternCheckGroup.class);

                assertEquals(validations.size(), 1);
                validations.forEach(
                        error -> {
                            assertThat(error.getMessage()).isEqualTo("아이디는 영문, 숫자만 가능하며 2 ~ 10자리까지 가능합니다.");
                        }
                );
            }

            @Test
            @DisplayName("특수문자 입력")
            void specialSymbol() {
                SignupRequestDto signupRequestDto = new SignupRequestDto("Kermit!*^#", "kermit1234", "kermit@gaegul.com");
                Set<ConstraintViolation<SignupRequestDto>> validations = validator.validate(signupRequestDto, ValidationGroups.PatternCheckGroup.class);

                assertEquals(validations.size(), 1);
                validations.forEach(
                        error -> {
                            assertThat(error.getMessage()).isEqualTo("아이디는 영문, 숫자만 가능하며 2 ~ 10자리까지 가능합니다.");
                        }
                );
            }

            @Test
            @DisplayName("공백 입력")
            void blank() {
                SignupRequestDto signupRequestDto = new SignupRequestDto("   ", "kermit1234", "kermit@gaegul.com");
                Set<ConstraintViolation<SignupRequestDto>> validations = validator.validate(signupRequestDto, ValidationGroups.PatternCheckGroup.class);

                assertEquals(validations.size(), 1);
                validations.forEach(
                        error -> {
                            assertThat(error.getMessage()).isEqualTo("아이디는 영문, 숫자만 가능하며 2 ~ 10자리까지 가능합니다.");
                        }
                );
            }
        }

        @Nested
        @DisplayName("비밀번호 형식 불일치")
        class PasswordFormatInconsistency {
            @Test
            @DisplayName("8자리 미만")
            void lessThan8Digit() {
                SignupRequestDto signupRequestDto = new SignupRequestDto("Kermit", "kermit1", "kermit@gaegul.com");
                Set<ConstraintViolation<SignupRequestDto>> validations = validator.validate(signupRequestDto, ValidationGroups.PatternCheckGroup.class);

                assertEquals(validations.size(), 1);
                validations.forEach(
                        error -> {
                            assertThat(error.getMessage()).isEqualTo("비밀번호는 영문과 숫자 조합으로 8 ~ 16자리까지 가능합니다.");
                        }
                );
            }

            @Test
            @DisplayName("16자리 초과")
            void moreThan10Digit() {
                SignupRequestDto signupRequestDto = new SignupRequestDto("Kermit", "kermit12345678910", "kermit@gaegul.com");
                Set<ConstraintViolation<SignupRequestDto>> validations = validator.validate(signupRequestDto, ValidationGroups.PatternCheckGroup.class);

                assertEquals(validations.size(), 1);
                validations.forEach(
                        error -> {
                            assertThat(error.getMessage()).isEqualTo("비밀번호는 영문과 숫자 조합으로 8 ~ 16자리까지 가능합니다.");
                        }
                );
            }

            @Test
            @DisplayName("한글 입력")
            void korean() {
                SignupRequestDto signupRequestDto = new SignupRequestDto("Kermit", "커밋1234", "kermit@gaegul.com");
                Set<ConstraintViolation<SignupRequestDto>> validations = validator.validate(signupRequestDto, ValidationGroups.PatternCheckGroup.class);

                assertEquals(validations.size(), 1);
                validations.forEach(
                        error -> {
                            assertThat(error.getMessage()).isEqualTo("비밀번호는 영문과 숫자 조합으로 8 ~ 16자리까지 가능합니다.");
                        }
                );
            }

            @Test
            @DisplayName("특수문자 입력")
            void specialSymbol() {
                SignupRequestDto signupRequestDto = new SignupRequestDto("Kermit", "kermit1234!*^#", "kermit@gaegul.com");
                Set<ConstraintViolation<SignupRequestDto>> validations = validator.validate(signupRequestDto, ValidationGroups.PatternCheckGroup.class);

                assertEquals(validations.size(), 1);
                validations.forEach(
                        error -> {
                            assertThat(error.getMessage()).isEqualTo("비밀번호는 영문과 숫자 조합으로 8 ~ 16자리까지 가능합니다.");
                        }
                );
            }
        }

        @Nested
        @DisplayName("이메일 형식 불일치")
        class EmailFormatInconsistency {
            @Test
            @DisplayName("@ 미포함")
            void missAt() {
                SignupRequestDto signupRequestDto = new SignupRequestDto("Kermit", "kermit1234", "kermitgaegul.com");
                Set<ConstraintViolation<SignupRequestDto>> validations = validator.validate(signupRequestDto, ValidationGroups.PatternCheckGroup.class);

                assertEquals(validations.size(), 1);
                validations.forEach(
                        error -> {
                            assertThat(error.getMessage()).isEqualTo("올바르지 않은 이메일 형식입니다.");
                        }
                );
            }

            @Test
            @DisplayName("@ 1개 초과 포함")
            void inconsistentAtFormat() {
                SignupRequestDto signupRequestDto = new SignupRequestDto("Kermit", "kermit1234", "kermit@@gaegul.com");
                Set<ConstraintViolation<SignupRequestDto>> validations = validator.validate(signupRequestDto, ValidationGroups.PatternCheckGroup.class);

                assertEquals(validations.size(), 1);
                validations.forEach(
                        error -> {
                            assertThat(error.getMessage()).isEqualTo("올바르지 않은 이메일 형식입니다.");
                        }
                );
            }

            @Test
            @DisplayName("최상위 도메인 미포함")
            void missDomain() {
                SignupRequestDto signupRequestDto = new SignupRequestDto("Kermit", "kermit1234", "kermit@gaegul");
                Set<ConstraintViolation<SignupRequestDto>> validations = validator.validate(signupRequestDto, ValidationGroups.PatternCheckGroup.class);

                assertEquals(validations.size(), 1);
                validations.forEach(
                        error -> {
                            assertThat(error.getMessage()).isEqualTo("올바르지 않은 이메일 형식입니다.");
                        }
                );
            }

            @Test
            @DisplayName("최상위 도메인 형식 불일치_2자리 미만")
            void inconsistentDomainFormat_lessThan2Digit() {
                SignupRequestDto signupRequestDto = new SignupRequestDto("Kermit", "kermit1234", "kermit@gaegul.a");
                Set<ConstraintViolation<SignupRequestDto>> validations = validator.validate(signupRequestDto, ValidationGroups.PatternCheckGroup.class);

                assertEquals(validations.size(), 1);
                validations.forEach(
                        error -> {
                            assertThat(error.getMessage()).isEqualTo("올바르지 않은 이메일 형식입니다.");
                        }
                );
            }

            @Test
            @DisplayName("최상위 도메인 형식 불일치_3자리 초과")
            void inconsistentDomainFormat_moreThan3Digit() {
                SignupRequestDto signupRequestDto = new SignupRequestDto("Kermit", "kermit1234", "kermit@gaegul.abcd");
                Set<ConstraintViolation<SignupRequestDto>> validations = validator.validate(signupRequestDto, ValidationGroups.PatternCheckGroup.class);

                assertEquals(validations.size(), 1);
                validations.forEach(
                        error -> {
                            assertThat(error.getMessage()).isEqualTo("올바르지 않은 이메일 형식입니다.");
                        }
                );
            }

            @Test
            @DisplayName("한글 입력")
            void korean() {
                SignupRequestDto signupRequestDto = new SignupRequestDto("Kermit", "kermit1234", "커밋@개굴.컴");
                Set<ConstraintViolation<SignupRequestDto>> validations = validator.validate(signupRequestDto, ValidationGroups.PatternCheckGroup.class);

                assertEquals(validations.size(), 1);
                validations.forEach(
                        error -> {
                            assertThat(error.getMessage()).isEqualTo("올바르지 않은 이메일 형식입니다.");
                        }
                );
            }

            @Test
            @DisplayName("특수문자 입력")
            void specialSymbol() {
                SignupRequestDto signupRequestDto = new SignupRequestDto("Kermit", "kermit1234", "ker!#~mit@gae^*gul.com");
                Set<ConstraintViolation<SignupRequestDto>> validations = validator.validate(signupRequestDto, ValidationGroups.PatternCheckGroup.class);

                assertEquals(validations.size(), 1);
                validations.forEach(
                        error -> {
                            assertThat(error.getMessage()).isEqualTo("올바르지 않은 이메일 형식입니다.");
                        }
                );
            }
        }
    }
}
