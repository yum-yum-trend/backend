package com.udangtangtang.backend.service;

import com.udangtangtang.backend.domain.User;
import com.udangtangtang.backend.dto.ProfileRequestDto;
import com.udangtangtang.backend.dto.SignupRequestDto;
import com.udangtangtang.backend.exception.ApiRequestException;
import com.udangtangtang.backend.repository.UserRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class UserProfileServiceTest {

    @Autowired
    UserProfileService userProfileService;
    @Autowired
    UserRepository userRepository;
    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserService userService;

    @Test
    public void 프로필_이미지_수정() throws IOException {
        // given
        SignupRequestDto signupRequestDto = createSignupRequestDto("tester1", "123", "test@test.com");
        userService.registerUser(signupRequestDto);
        User user = userRepository.findByUsername(signupRequestDto.getUsername()).orElseThrow(
                () -> new ApiRequestException("해당 유저가 존재하지 않습니다!"));

        // when
        String fileName = "testImageUpload";
        String contentType = "jpg";
        String filePath = "src/test/resources/test.jpg";
        MockMultipartFile mockMultipartFile = getMockMultipartFile(fileName, contentType, filePath);

        String url = userProfileService.updateProfileImage(user.getId(), mockMultipartFile);

        // then
        assertFalse(url.isEmpty(), "이미지 URL이 DB에 저장됨.");
    }

    @Test
    public void 프로필_이미지_초기화() throws IOException {
        // given
        SignupRequestDto signupRequestDto = createSignupRequestDto("tester1", "123", "test@test.com");
        userService.registerUser(signupRequestDto);
        User user = userRepository.findByUsername(signupRequestDto.getUsername()).orElseThrow(
                () -> new ApiRequestException("해당 유저가 존재하지 않습니다!"));

        String fileName = "testImageUpload";
        String contentType = "jpg";
        String filePath = "src/test/resources/test.jpg";
        MockMultipartFile mockMultipartFile = getMockMultipartFile(fileName, contentType, filePath);
        userProfileService.updateProfileImage(user.getId(), mockMultipartFile);

        // when
        userProfileService.resetUserProfileImage(user.getId());

        assertNull(user.getUserProfileImageUrl(), "이미지 초기화 성공");
    }

    @Test(expected = Exception.class)
    public void 비밀번호_변경_성공_1() throws Exception {
        // given
        SignupRequestDto signupRequestDto = createSignupRequestDto("tester1", "123", "test@test.com");
        userService.registerUser(signupRequestDto);
        User user = userRepository.findByUsername(signupRequestDto.getUsername()).orElseThrow(
                () -> new ApiRequestException("해당 유저가 존재하지 않습니다!"));
        ProfileRequestDto profileRequestDto = createProfileRequestDto("123", "456", "");

        // when
        userProfileService.updateUserPassword(user.getId(), profileRequestDto);
        authenticate(user.getUsername(), "123");

        // then
        fail("비밀번호가 잘 변경되었다면, 오류가 발생되어야 함.");
    }

    @Test
    public void 비밀번호_변경_성공_2() throws Exception {
        // given
        SignupRequestDto signupRequestDto = createSignupRequestDto("tester1", "123", "test@test.com");
        userService.registerUser(signupRequestDto);
        User user = userRepository.findByUsername(signupRequestDto.getUsername()).orElseThrow(
                () -> new ApiRequestException("해당 유저가 존재하지 않습니다!"));
        ProfileRequestDto profileRequestDto = createProfileRequestDto("123", "456", "");

        // when
        userProfileService.updateUserPassword(user.getId(), profileRequestDto);

        // then
        authenticate(user.getUsername(), profileRequestDto.getNewPassword());
    }

    @Test
    public void 상태_메세지_변경() {
        // given
        SignupRequestDto signupRequestDto = createSignupRequestDto("tester1", "123", "test@test.com");
        userService.registerUser(signupRequestDto);
        User user = userRepository.findByUsername(signupRequestDto.getUsername()).orElseThrow(
                () -> new ApiRequestException("해당 유저가 존재하지 않습니다!"));
        ProfileRequestDto profileRequestDto = createProfileRequestDto("", "", "Hi");

        // when
        String changedText = userProfileService.updateUserProfileIntroText(user.getId(), profileRequestDto);

        // then
        assertEquals(changedText, profileRequestDto.getUserProfileIntro(), "상태 메세지가 변경되었음.");
    }


    private SignupRequestDto createSignupRequestDto(String username, String password, String email) {
        SignupRequestDto signupRequestDto = new SignupRequestDto();
        signupRequestDto.setUsername(username);
        signupRequestDto.setPassword(password);
        signupRequestDto.setEmail(email);
        return signupRequestDto;
    }

    private ProfileRequestDto createProfileRequestDto(String nowPassword, String newPassword, String userProfileIntro) {
        ProfileRequestDto profileRequestDto = new ProfileRequestDto();
        profileRequestDto.setNowPassword(nowPassword);
        profileRequestDto.setNewPassword(newPassword);
        profileRequestDto.setUserProfileIntro(userProfileIntro);
        return profileRequestDto;
    }

    private MockMultipartFile getMockMultipartFile(String fileName, String contentType, String path) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(new File(path));
        return new MockMultipartFile(fileName, fileName + "." + contentType, contentType, fileInputStream);
    }

    private void authenticate(String username, String password) throws Exception {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }


}
