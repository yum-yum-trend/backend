package com.udangtangtang.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.GsonBuilder;
import com.udangtangtang.backend.domain.Article;
import com.udangtangtang.backend.domain.Image;
import com.udangtangtang.backend.domain.User;
import com.udangtangtang.backend.domain.UserRole;
import com.udangtangtang.backend.dto.request.LocationRequestDto;
import com.udangtangtang.backend.dto.request.SignupRequestDto;
import com.udangtangtang.backend.dto.request.TokenRequestDto;
import com.udangtangtang.backend.dto.request.UserRequestDto;
import com.udangtangtang.backend.repository.ArticleRepository;
import com.udangtangtang.backend.repository.JwtRefreshTokenRepository;
import com.udangtangtang.backend.repository.UserRepository;
import com.udangtangtang.backend.security.UserDetailsImpl;
import com.udangtangtang.backend.service.ArticleService;
import com.udangtangtang.backend.service.UserService;
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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
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
public class ApiTest {
    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    ArticleService articleService;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserDetailsService userDetailsService;

    @Autowired
    JwtRefreshTokenRepository jwtRefreshTokenRepository;

    @Autowired
    JwtTokenUtil jwtTokenUtil;


    @BeforeEach
    public void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) throws ServletException {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .alwaysDo(MockMvcResultHandlers.print())
                .apply(springSecurity())
                .apply(documentationConfiguration(restDocumentation)).build();
    }

    @Test
    @Order(1)
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
    @Order(2)
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
    @Order(3)
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

    @Test
    @Order(4)
    public void 게시물_생성() throws Exception {
        // given
        User user = new User("Kermit", "1234", "Kermit@gaegulgaegul.com", UserRole.USER);
        userRepository.save(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String text = "게시물 본문";
        String locationRequestDto = "{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}";
        String tagNames = "얌얌트랜트,음식,사진,공유";

        MockMultipartFile file1 = getMockMultipartFile("imageFiles", "cute_chun_sik.jpeg", "multipart/form-data", "src/test/resources/images/cute_chun_sik.jpeg");
        MockMultipartFile file2 = getMockMultipartFile("imageFiles", "ring_ding_kermit.jpeg", "multipart/form-data", "src/test/resources/images/ring_ding_kermit.jpeg");

        mockMvc.perform(multipart("/articles")
                        .file(file1)
                        .file(file2)
                        .param("text", text)
                        .param("location", locationRequestDto)
                        .param("tagNames", tagNames)
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(userDetails.getUsername()))
                )
                .andExpect(status().isOk())
                .andDo(document("articles/create",
                        requestParameters(
                                parameterWithName("text").description("게시글 본문"),
                                parameterWithName("location").description("위치 정보"),
                                parameterWithName("tagNames").description("태그 이름 리스트")
                        )
                ));
    }

    @Test
    @Order(5)
    public void 모든_게시물_조회() throws Exception {
        // given
        String searchTag = "yummy";
        String sortBy = "createdAt";
        String isAsc = "false";
        String page = "1";

        mockMvc.perform(get("/articles")
                        .queryParam("searchTag", searchTag)
                        .queryParam("sortBy", sortBy)
                        .queryParam("isAsc", isAsc)
                        .queryParam("currentPage", page))
                .andExpect(status().isOk())
                .andDo(document("articles/read-all",
                        requestParameters(
                                parameterWithName("searchTag").description("검색 태그"),
                                parameterWithName("sortBy").description("정렬 타입"),
                                parameterWithName("isAsc").description("오름차순 정렬 플래그"),
                                parameterWithName("currentPage").description("현재 페이지")
                        )
                ));
    }

    @Test
    @Order(6)
    public void 특정_게시물_조회() throws Exception {
        // given
        User user = new User("Kermit", "1234", "Kermit@gaegulgaegul.com", UserRole.USER);
        userRepository.save(user);

        String text = "게시물 본문";
        LocationRequestDto locationRequestDto = new LocationRequestDto("{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}");
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<MultipartFile> imageFiles = Arrays.asList(
                getMockMultipartFile("cute_chun_sik", "cute_chun_sik.jpeg", "multipart/form-data", "src/test/resources/images/cute_chun_sik.jpeg"),
                getMockMultipartFile("ring_ding_kermit", "cute_chun_sik.jpeg", "multipart/form-data", "src/test/resources/images/ring_ding_kermit.jpeg")
        );

        Article article = articleService.createArticle(user, text, locationRequestDto, tagNames, imageFiles);

        mockMvc.perform(get("/articles/{id}", article.getId()))
                .andExpect(status().isOk())
                .andDo(document("articles/read-one"));
    }

    @Test
    @Order(7)
    public void 게시물_수정() throws Exception {
        // given
        User user = new User("Kermit", "1234", "Kermit@gaegulgaegul.com", UserRole.USER);
        userRepository.save(user);

        String text = "게시물 본문";
        LocationRequestDto locationRequestDto = new LocationRequestDto("{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}");
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<MultipartFile> imageFiles = Arrays.asList(
                getMockMultipartFile("cute_chun_sik", "cute_chun_sik.jpeg", "multipart/form-data", "src/test/resources/images/cute_chun_sik.jpeg"),
                getMockMultipartFile("ring_ding_kermit", "cute_chun_sik.jpeg", "multipart/form-data", "src/test/resources/images/ring_ding_kermit.jpeg")
        );

        Article article = articleService.createArticle(user, text, locationRequestDto, tagNames, imageFiles);
        String rmImageIds = "";
        for(Image image : article.getImages()) {
            rmImageIds += image.getId() + ",";
        }
        rmImageIds = rmImageIds.substring(0, rmImageIds.length()-1);

        String updateText = "게시물 본문 - 수정 내용";
        String updateLocationRequestDto = "{}";
        String updateTagNames = "서울,맛집";
        MockMultipartFile updateFile = getMockMultipartFile("imageFiles", "test.jpg", "multipart/form-data", "src/test/resources/images/test.jpg");

        mockMvc.perform(MockMvcRequestBuilders.multipart("/articles/{id}", article.getId())
                        .file(updateFile)
                        .param("text", updateText)
                        .param("location", updateLocationRequestDto)
                        .param("tagNames", updateTagNames)
                        .param("rmImageIds", rmImageIds)
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(user.getUsername()))
                )
                .andExpect(status().isOk())
                .andDo(document("articles/update",
                        requestParameters(
                                parameterWithName("text").description("게시글 본문"),
                                parameterWithName("location").description("위치 정보"),
                                parameterWithName("tagNames").description("태그 이름 리스트"),
                                parameterWithName("rmImageIds").description("삭제할 이미지 아이디 리스트")
                        )
                ));
    }

    @Test
    @Order(8)
    public void 게시물_삭제() throws Exception {
        // given
        User user = new User("Kermit", "1234", "Kermit@gaegulgaegul.com", UserRole.USER);
        userRepository.save(user);

        String text = "게시물 본문";
        LocationRequestDto locationRequestDto = new LocationRequestDto("{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}");
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<MultipartFile> imageFiles = Arrays.asList(
                getMockMultipartFile("cute_chun_sik", "cute_chun_sik.jpeg", "multipart/form-data", "src/test/resources/images/cute_chun_sik.jpeg"),
                getMockMultipartFile("ring_ding_kermit", "cute_chun_sik.jpeg", "multipart/form-data", "src/test/resources/images/ring_ding_kermit.jpeg")
        );

        Article article = articleService.createArticle(user, text, locationRequestDto, tagNames, imageFiles);

        mockMvc.perform(delete("/articles/{id}", article.getId())
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(user.getUsername())))
                        .andExpect(status().isOk())
                        .andDo(document("articles/delete"));
    }

    @Test
    @Order(9)
    public void 트랜드_맵_호출() throws Exception {
        mockMvc.perform(get("/trend"))
                .andExpect(status().isOk())
                .andDo(document("trend"));
    }

    @Test
    @Order(10)
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
    @Order(11)
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
    @Order(12)
    public void 손님_전체_좋아요_보기() throws Exception {
        mockMvc.perform(get("/likes/guest"))
                .andExpect(status().isOk())
                .andDo(document("likes/guest"));
    }

    @Test
    @Order(13)
    public void 사용자_게시글_좋아요_보기() throws Exception {
        // given
        User user = new User("testuser", "testuser", "testuser@testuser.com", UserRole.USER);
        userRepository.save(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String text = "게시물 본문";
        LocationRequestDto locationRequestDto = new LocationRequestDto("{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}");
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<MultipartFile> imageFiles = Arrays.asList(
                getMockMultipartFile("cute_chun_sik", "cute_chun_sik.jpeg", "multipart/form-data", "src/test/resources/images/cute_chun_sik.jpeg"),
                getMockMultipartFile("ring_ding_kermit", "cute_chun_sik.jpeg", "multipart/form-data", "src/test/resources/images/ring_ding_kermit.jpeg")
        );

        Article article = articleService.createArticle(user, text, locationRequestDto, tagNames, imageFiles);

        mockMvc.perform(get("/likes/{id}", article.getId())
                .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(userDetails.getUsername())))
                .andExpect(status().isOk())
                .andDo(document("likes"));
    }

    @Test
    @Order(14)
    public void 프로필_이미지_삭제() throws Exception {
        User user = userRepository.save(new User("Kermit", "1234", "Kermit@gaegulgaegul.com", UserRole.USER));

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

