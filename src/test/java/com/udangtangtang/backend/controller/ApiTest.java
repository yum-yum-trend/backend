package com.udangtangtang.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.GsonBuilder;
import com.udangtangtang.backend.domain.Article;
import com.udangtangtang.backend.domain.Comment;
import com.udangtangtang.backend.domain.User;
import com.udangtangtang.backend.domain.UserRole;
import com.udangtangtang.backend.dto.request.*;
import com.udangtangtang.backend.repository.ArticleRepository;
import com.udangtangtang.backend.repository.CommentRepository;
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
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oauth2Login;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@SpringBootTest()
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class ApiTest {
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
    ArticleService articleService;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    CommentRepository commentRepository;

    static public String token = "";

    Article createdArticle = null;
    List<Long> imageIds = null;

    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .alwaysDo(MockMvcResultHandlers.print())
                .apply(springSecurity())
                .apply(documentationConfiguration(restDocumentation)).build();

    }

    @Test
    @Order(1)
    public void 회원가입() throws Exception {
        SignupRequestDto dto = new SignupRequestDto();
        dto.setUsername("testuser");
        dto.setPassword("testuser");
        dto.setEmail("testuser@naver.com");
        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(dto);

        mockMvc.perform(post("/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("user/signup",
                        requestFields(
                                fieldWithPath("username").description("유저명"),
                                fieldWithPath("password").description("패스워드"),
                                fieldWithPath("email").description("이메일"),
                                fieldWithPath("admin").description("관리자 유무"),
                                fieldWithPath("adminToken").description("관리자 토큰")
                        )
                ));
    }

    @Test
    @Order(2)
    public void 로그인() throws Exception {
        String password = passwordEncoder.encode("testuser");
        User user = new User("testuser", password, "testuser@testuser.com", UserRole.USER);
        userRepository.save(user);

        UserRequestDto dto = new UserRequestDto();
        dto.setUsername("testuser");
        dto.setPassword("testuser");
        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(dto);

        mockMvc.perform(post("/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonString)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("user/login",
                        requestFields(
                                fieldWithPath("username").description("유저명"),
                                fieldWithPath("password").description("패스워드")
                        )
                ));
    }

    @Test
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
    @Order(3)
    public void 트랜드_맵_호출() throws Exception {
        mockMvc.perform(get("/trend"))
                .andExpect(status().isOk())
                .andDo(document("trend"));
    }

    @Test
    @Order(4)
    public void 트랜드_차트_호출() throws Exception {
        String location = "서울";

        mockMvc.perform(get("/trend/chart")
                        .param("location", location))
                .andExpect(status().isOk())
                .andDo(document("trend/chart",
                        requestParameters(
                                parameterWithName("location").description("지역명")
                        )
                ));
    }

    @Test
    @Order(5)
    public void 사용자_전체_좋아요_보기() throws Exception {
        // given
        User user = new User("testuser", "testuser", "testuser@testuser.com", UserRole.USER);
        userRepository.save(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        mockMvc.perform(get("/likes")
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(userDetails.getUsername())))
                .andExpect(status().isOk())
                .andDo(document("likes"));
    }

    @Test
    @Order(6)
    public void 손님_전체_좋아요_보기() throws Exception {
        mockMvc.perform(get("/likes/guest"))
                .andExpect(status().isOk())
                .andDo(document("likes/guest"));
    }

    @Test
    @Order(7)
    public void 사용자_게시글_좋아요_보기() throws Exception {
        // given
        User user = new User("testuser", "testuser", "testuser@testuser.com", UserRole.USER);
        userRepository.save(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String text = "게시물 본문";
        LocationRequestDto locationRequestDto = new LocationRequestDto("{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}");
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<MultipartFile> imageFiles = Arrays.asList(
                getMockMultipartFile("cute_chun_sik", "jpeg", "multipart/form-data", "src/test/resources/images/cute_chun_sik.jpeg"),
                getMockMultipartFile("ring_ding_kermit", "jpeg", "multipart/form-data", "src/test/resources/images/ring_ding_kermit.jpeg")
        );

        Article article = articleService.createArticle(user, text, locationRequestDto, tagNames, imageFiles);

        mockMvc.perform(get("/likes/{id}", article.getId())
                .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(userDetails.getUsername())))
                .andExpect(status().isOk())
                .andDo(document("likes"));
    }

    @Test
    @Order(8)
    public void 손님_게시글_좋아요_보기() throws Exception {
        // given
        User user = new User("testuser", "testuser", "testuser@testuser.com", UserRole.USER);
        userRepository.save(user);

        String text = "게시물 본문";
        LocationRequestDto locationRequestDto = new LocationRequestDto("{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}");
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<MultipartFile> imageFiles = Arrays.asList(
                getMockMultipartFile("cute_chun_sik", "jpeg", "multipart/form-data", "src/test/resources/images/cute_chun_sik.jpeg"),
                getMockMultipartFile("ring_ding_kermit", "jpeg", "multipart/form-data", "src/test/resources/images/ring_ding_kermit.jpeg")
        );

        Article article = articleService.createArticle(user, text, locationRequestDto, tagNames, imageFiles);

        mockMvc.perform(get("/likes/guest/{id}", article.getId()))
                .andExpect(status().isOk())
                .andDo(document("likes/guest"));
    }

    @Test
    @Order(9)
    public void 마이페이지_게시글_좋아요_보기() throws Exception {
        // given
        User user = new User("testuser", "testuser", "testuser@testuser.com", UserRole.USER);
        userRepository.save(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String text = "게시물 본문";
        LocationRequestDto locationRequestDto = new LocationRequestDto("{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}");
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<MultipartFile> imageFiles = Arrays.asList(
                getMockMultipartFile("cute_chun_sik", "jpeg", "multipart/form-data", "src/test/resources/images/cute_chun_sik.jpeg"),
                getMockMultipartFile("ring_ding_kermit", "jpeg", "multipart/form-data", "src/test/resources/images/ring_ding_kermit.jpeg")
        );

        articleService.createArticle(user, text, locationRequestDto, tagNames, imageFiles);

        mockMvc.perform(get("/profile/likes/{id}", user.getId())
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(userDetails.getUsername())))
                .andExpect(status().isOk())
                .andDo(document("profile/likes"));
    }

    @Test
    @Order(10)
    public void 게시글_댓글_보기() throws Exception {
        // given
        User user = new User("testuser", "testuser", "testuser@testuser.com", UserRole.USER);
        userRepository.save(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String text = "게시물 본문";
        LocationRequestDto locationRequestDto = new LocationRequestDto("{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}");
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<MultipartFile> imageFiles = Arrays.asList(
                getMockMultipartFile("cute_chun_sik", "jpeg", "multipart/form-data", "src/test/resources/images/cute_chun_sik.jpeg"),
                getMockMultipartFile("ring_ding_kermit", "jpeg", "multipart/form-data", "src/test/resources/images/ring_ding_kermit.jpeg")
        );

        Article article = articleService.createArticle(user, text, locationRequestDto, tagNames, imageFiles);
        Comment comment = new Comment(user, article, "test 댓글입니다.");
        commentRepository.save(comment);

        mockMvc.perform(get("/comment/{articleId}", article.getId())
                .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(userDetails.getUsername())))
                .andExpect(status().isOk())
                .andDo(document("comment"));
    }

    @Test
    @Order(11)
    public void 게시글_댓글_저장() throws Exception {
        // given
        User user = new User("testuser", "testuser", "testuser@testuser.com", UserRole.USER);
        userRepository.save(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String text = "게시물 본문";
        LocationRequestDto locationRequestDto = new LocationRequestDto("{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}");
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<MultipartFile> imageFiles = Arrays.asList(
                getMockMultipartFile("cute_chun_sik", "jpeg", "multipart/form-data", "src/test/resources/images/cute_chun_sik.jpeg"),
                getMockMultipartFile("ring_ding_kermit", "jpeg", "multipart/form-data", "src/test/resources/images/ring_ding_kermit.jpeg")
        );

        Article article = articleService.createArticle(user, text, locationRequestDto, tagNames, imageFiles);

        CommentRequestDto dto = new CommentRequestDto();
        dto.setCommentText("Test댓글입니다.");
        String jsonString = new GsonBuilder().setPrettyPrinting().create().toJson(dto);


        mockMvc.perform(post("/comment/{articleId}", article.getId())
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(userDetails.getUsername()))
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonString)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(document("comment",
                        requestFields(
                                fieldWithPath("commentText").description("댓글 내용")
                        )
                ));
    }

    @Test
    @Order(12)
    public void 게시글_댓글_삭제() throws Exception {
        // given
        User user = new User("testuser", "testuser", "testuser@testuser.com", UserRole.USER);
        userRepository.save(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String text = "게시물 본문";
        LocationRequestDto locationRequestDto = new LocationRequestDto("{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}");
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<MultipartFile> imageFiles = Arrays.asList(
                getMockMultipartFile("cute_chun_sik", "jpeg", "multipart/form-data", "src/test/resources/images/cute_chun_sik.jpeg"),
                getMockMultipartFile("ring_ding_kermit", "jpeg", "multipart/form-data", "src/test/resources/images/ring_ding_kermit.jpeg")
        );

        Article article = articleService.createArticle(user, text, locationRequestDto, tagNames, imageFiles);
        Comment comment = new Comment(user, article, "test 댓글입니다.");
        commentRepository.save(comment);

        mockMvc.perform(delete("/comment/{commentId}", comment.getId())
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(userDetails.getUsername())))
                .andExpect(status().isOk())
                .andDo(document("comment"));
    }

    @Test
    @Order(13)
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
    @Order(14)
    public void 유저_프로필정보_보기() throws Exception {
        // given
        User user = new User("testuser", "testuser", "testuser@testuser.com", UserRole.USER);
        userRepository.save(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        mockMvc.perform(get("/profile/{userId}", user.getId())
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(userDetails.getUsername())))
                .andExpect(status().isOk())
                .andDo(document("profile"));
    }

    @Test
    @Order(15)
    public void 유저_게시글_보기() throws Exception {
        // given
        User user = new User("testuser", "testuser", "testuser@testuser.com", UserRole.USER);
        userRepository.save(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String text = "게시물 본문";
        LocationRequestDto locationRequestDto = new LocationRequestDto("{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}");
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<MultipartFile> imageFiles = Arrays.asList(
                getMockMultipartFile("cute_chun_sik", "jpeg", "multipart/form-data", "src/test/resources/images/cute_chun_sik.jpeg"),
                getMockMultipartFile("ring_ding_kermit", "jpeg", "multipart/form-data", "src/test/resources/images/ring_ding_kermit.jpeg")
        );

        Article article = articleService.createArticle(user, text, locationRequestDto, tagNames, imageFiles);
        String page = "0";

        mockMvc.perform(get("/profile/articles/{userId}", user.getId())
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(userDetails.getUsername()))
                        .param("sortBy", "createdAt")
                        .param("isAsc", "false")
                        .param("currentPage", page))
                .andExpect(status().isOk())
                .andDo(document("profile/articles"));
    }

    @Test
    @Order(16)
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
    @Order(17)
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
    @Order(18)
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



    private MockMultipartFile getMockMultipartFile(String fileName, String extension, String contentType, String path) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(new File(path));
        return new MockMultipartFile(fileName, fileName + "." + extension, contentType, fileInputStream);
    }
}