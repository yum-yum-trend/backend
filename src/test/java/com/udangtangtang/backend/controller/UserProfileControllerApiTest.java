package com.udangtangtang.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.GsonBuilder;
import com.udangtangtang.backend.domain.Article;
import com.udangtangtang.backend.domain.User;
import com.udangtangtang.backend.domain.UserRole;
import com.udangtangtang.backend.dto.request.LocationRequestDto;
import com.udangtangtang.backend.dto.request.ProfileRequestDto;
import com.udangtangtang.backend.repository.UserRepository;
import com.udangtangtang.backend.security.UserDetailsImpl;
import com.udangtangtang.backend.service.ArticleService;
import com.udangtangtang.backend.util.JwtTokenUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class UserProfileControllerApiTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    JwtTokenUtil jwtTokenUtil;

    @Autowired
    ArticleService articleService;

    @Autowired
    PasswordEncoder passwordEncoder;

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
    public void 유저_네비게이션_프로필사진_보기() throws Exception {
        // given
        User user = new User("testuser", "testuser", "testuser@testuser.com", UserRole.USER);
        userRepository.save(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        mockMvc.perform(get("/profile/navbar-image/{userId}", user.getId())
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(userDetails.getUsername())))
                .andExpect(status().isOk())
                .andDo(document("profile/navbar-image"));
    }

    @Test
    public void 유저_프로필정보_보기() throws Exception {
        // given
        User user = new User("testuser", "testuser", "testuser@testuser.com", UserRole.USER);
        userRepository.save(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        mockMvc.perform(get("/profile/{userId}", user.getId())
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(userDetails.getUsername())))
                .andExpect(status().isOk())
                .andDo(document("profile/show"));
    }

    @Test
    public void 유저_게시글_보기() throws Exception {
        // given
        User user = new User("testuser", "testuser", "testuser@testuser.com", UserRole.USER);
        userRepository.save(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String text = "게시물 본문";
        LocationRequestDto locationRequestDto = new LocationRequestDto("{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}");
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<MultipartFile> imageFiles = Arrays.asList(
                getMockMultipartFile("cute_chun_sik", "cute_chun_sik.jpeg", "multipart/form-data", "src/test/resources/images/cute_chun_sik.jpeg"),
                getMockMultipartFile("ring_ding_kermit", "ring_ding_kermit.jpeg", "multipart/form-data", "src/test/resources/images/ring_ding_kermit.jpeg")
        );

        Article article = articleService.createArticle(user, text, locationRequestDto, tagNames, imageFiles);
        String page = "0";

        mockMvc.perform(get("/profile/articles/{userId}", user.getId())
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(userDetails.getUsername()))
                        .param("sortBy", "createdAt")
                        .param("isAsc", "false")
                        .param("currentPage", page))
                .andExpect(status().isOk())
                .andDo(document("profile/articles",
                        requestParameters(
                                parameterWithName("sortBy").description("정렬 타입"),
                                parameterWithName("isAsc").description("오름차순 정렬 플래그"),
                                parameterWithName("currentPage").description("현재 페이지")
                        )
                        ));
    }

    @Test
    public void 프로필_이미지_변경() throws Exception {
        // given
        User user = new User("testuser", "testuser", "testuser@testuser.com", UserRole.USER);
        userRepository.save(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        MockMultipartFile file = new MockMultipartFile("newProfileImage", "cute_chun_sik.jpeg", "multipart/form-data", "src/test/resources/images/cute_chun_sik.jpeg".getBytes());

        mockMvc.perform(multipart("/profile/image-change/{userId}", user.getId())
                        .file(file)
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(userDetails.getUsername())))
                .andExpect(status().isOk())
                .andDo(document("profile/image-change"));
    }

    @Test
    public void 프로필_비밀번호_변경() throws Exception {
        // given
        User user = new User("testuser", passwordEncoder.encode("testuser"), "testuser@testuser.com", UserRole.USER);
        userRepository.save(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        ProfileRequestDto dto = new ProfileRequestDto();
        dto.setNowPassword("testuser");
        dto.setNewPassword("testuser1111");
        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(dto);

        mockMvc.perform(put("/profile/pw/{userId}", user.getId())
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(userDetails.getUsername()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("profile/pw",
                        requestFields(
                                fieldWithPath("nowPassword").description("현재 비밀번호"),
                                fieldWithPath("newPassword").description("새로운 비밀번호")
                        )
                ));
    }

    @Test
    public void 프로필_자기소개_변경() throws Exception {
        // given
        User user = new User("testuser", "testuser", "testuser@testuser.com", UserRole.USER);
        userRepository.save(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        ProfileRequestDto dto = new ProfileRequestDto();
        dto.setUserProfileIntro("테스트 프로필 자기소개 내용");
        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(dto);

        mockMvc.perform(post("/profile/intro/{userId}", user.getId())
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(userDetails.getUsername()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("profile/intro",
                        requestFields(
                                fieldWithPath("userProfileIntro").description("유저 프로필 소개글")
                        )
                ));
    }

    @Test
    public void 프로필_이미지_삭제() throws Exception {
        User user = userRepository.save(new User("Kermit", "Kermit1234", "Kermit@gaegulgaegul.com", UserRole.USER));

        mockMvc.perform(delete("/profile/{userId}", user.getId())
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(user.getUsername())))
                .andExpect(status().isOk())
                .andDo(document("profile/image/delete"));
    }

    private MockMultipartFile getMockMultipartFile(String fileName, String originalFilename, String contentType, String path) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(new File(path));
        return new MockMultipartFile(fileName, originalFilename, contentType, fileInputStream);
    }
}
