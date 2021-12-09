package com.udangtangtang.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.GsonBuilder;
import com.udangtangtang.backend.domain.User;
import com.udangtangtang.backend.domain.UserRole;
import com.udangtangtang.backend.dto.request.LogoutRequestDto;
import com.udangtangtang.backend.dto.request.SignupRequestDto;
import com.udangtangtang.backend.dto.request.TokenRequestDto;
import com.udangtangtang.backend.dto.request.UserRequestDto;
import com.udangtangtang.backend.repository.UserRepository;
import com.udangtangtang.backend.security.UserDetailsImpl;
import com.udangtangtang.backend.service.UserService;
import com.udangtangtang.backend.util.JwtTokenUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.servlet.ServletException;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class UserControllerApiTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @Autowired
    UserService userService;

    static public String token = "";

    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) throws ServletException {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .alwaysDo(MockMvcResultHandlers.print())
                .apply(springSecurity())
                .apply(documentationConfiguration(restDocumentation)).build();
    }

    @Test
    @Transactional
    public void 회원가입() throws Exception {
        SignupRequestDto signupRequestDto = new SignupRequestDto();
        signupRequestDto.setUsername("Kermit");
        signupRequestDto.setPassword("1234");
        signupRequestDto.setEmail("Kermit@gaegulgaegul.com");
        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(signupRequestDto);

        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("signup",
                        requestFields(
                                fieldWithPath("username").description("사용자 이름"),
                                fieldWithPath("password").description("비밀번호"),
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("admin").description("관리자 확인 플래그"),
                                fieldWithPath("adminToken").description("관리자 토큰")
                        )
                ));
    }

    @Test
    @Transactional
    public void 로그인() throws Exception {
        User user = new User("Kermit", passwordEncoder.encode("1234"), "Kermit@gaegulgaegul.com", UserRole.USER);
        userRepository.save(user);

        UserRequestDto userRequestDto = new UserRequestDto();
        userRequestDto.setUsername("Kermit");
        userRequestDto.setPassword("1234");

        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(userRequestDto);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("login",
                        requestFields(
                                fieldWithPath("username").description("사용자 이름"),
                                fieldWithPath("password").description("비밀번호")
                        )
                ));
    }

    @Test
    @Transactional
    public void 로그아웃() throws Exception {
        // given
        User user = new User("testuser", "testuser", "testuser@testuser.com", UserRole.USER);
        userRepository.save(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        LogoutRequestDto logout = new LogoutRequestDto();
        logout.setUsername(user.getUsername());
        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(logout);

        mockMvc.perform(post("/logout")
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(userDetails.getUsername()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("user/logout",
                        requestFields(
                                fieldWithPath("username").description("유저명")
                        )
                ));
    }


    @Test
    @Transactional
    public void 엑세스_토큰_재발급() throws Exception {
        String username = "Kermit";
        String password = "1234";
        String email = "kermit@gaegulgaegul.com";

        // 회원가입
        SignupRequestDto signupRequestDto = new SignupRequestDto(username, password, email);
        userService.createUser(signupRequestDto);

        // 로그인 - Access Token 발급 & Refresh Token 발급 후 데이터베이스 저장
        UserRequestDto userRequestDto = new UserRequestDto(username, password);
        userService.createAuthenticationToken(userRequestDto);

        // 토큰 재발급
        TokenRequestDto tokenRequestDto = new TokenRequestDto(jwtTokenUtil.generateAccessToken(username), jwtTokenUtil.generateRefreshToken());
        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(tokenRequestDto);

        mockMvc.perform(post("/auth/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("auth/token",
                        requestFields(
                                fieldWithPath("accessToken").description("JWT Access Token"),
                                fieldWithPath("refreshToken").description("JWT Refresh Token")
                        )
                ));
    }
}
