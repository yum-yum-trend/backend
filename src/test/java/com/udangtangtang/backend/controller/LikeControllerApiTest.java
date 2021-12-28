package com.udangtangtang.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udangtangtang.backend.domain.Article;
import com.udangtangtang.backend.domain.User;
import com.udangtangtang.backend.domain.UserRole;
import com.udangtangtang.backend.dto.request.ArticleCreateRequestDto;
import com.udangtangtang.backend.dto.request.LocationRequestDto;
import com.udangtangtang.backend.repository.UserRepository;
import com.udangtangtang.backend.security.UserDetailsImpl;
import com.udangtangtang.backend.service.ArticleService;
import com.udangtangtang.backend.service.LikeService;
import com.udangtangtang.backend.util.JwtTokenUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith({RestDocumentationExtension.class, SpringExtension.class})
@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureRestDocs
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
public class LikeControllerApiTest {
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
    LikeService likeService;

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
    public void 사용자_전체_좋아요_보기() throws Exception {
        // given
        User user = new User("testuser", "testuser", "testuser@testuser.com", UserRole.USER);
        userRepository.save(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        mockMvc.perform(get("/likes")
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(userDetails.getUsername())))
                .andExpect(status().isOk())
                .andDo(document("likes/show"));
    }

    @Test
    public void 손님_전체_좋아요_보기() throws Exception {
        mockMvc.perform(get("/likes/guest"))
                .andExpect(status().isOk())
                .andDo(document("likes/guest/show"));
    }

    @Test
    public void 사용자_게시글_좋아요_보기() throws Exception {
        // given
        User user = new User("testuser", "testuser", "testuser@testuser.com", UserRole.USER);
        userRepository.save(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String text = "게시물 본문";
        String location = "{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}";
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<Long> imageIds = new ArrayList<>();

        Article article = articleService.createArticle(user, new ArticleCreateRequestDto(text, location, tagNames, imageIds));

        mockMvc.perform(get("/likes/{id}", article.getId())
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(userDetails.getUsername())))
                .andExpect(status().isOk())
                .andDo(document("likes/article"));
    }

    @Test
    public void 손님_게시글_좋아요_보기() throws Exception {
        // given
        User user = new User("testuser", "testuser", "testuser@testuser.com", UserRole.USER);
        userRepository.save(user);

        String text = "게시물 본문";
        String location = "{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}";
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<Long> imageIds = new ArrayList<>();

        Article article = articleService.createArticle(user, new ArticleCreateRequestDto(text, location, tagNames, imageIds));

        mockMvc.perform(get("/likes/guest/{id}", article.getId()))
                .andExpect(status().isOk())
                .andDo(document("likes/guest/article"));
    }

    @Test
    public void 마이페이지_게시글_좋아요_보기() throws Exception {
        // given
        User user = new User("testuser", "testuser", "testuser@testuser.com", UserRole.USER);
        userRepository.save(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String text = "게시물 본문";
        String location = "{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}";
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<Long> imageIds = new ArrayList<>();

        Article article = articleService.createArticle(user, new ArticleCreateRequestDto(text, location, tagNames, imageIds));

        mockMvc.perform(get("/profile/likes/{id}", user.getId())
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(userDetails.getUsername())))
                .andExpect(status().isOk())
                .andDo(document("likes/profile/show"));
    }

    @Test
    public void 좋아요_추가() throws Exception {
        // given
        User user = new User("testuser", "testuser", "testuser@testuser.com", UserRole.USER);
        userRepository.save(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String text = "게시물 본문";
        String location = "{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}";
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<Long> imageIds = new ArrayList<>();

        Article article = articleService.createArticle(user, new ArticleCreateRequestDto(text, location, tagNames, imageIds));
        String articleId = Long.toString(article.getId());

        mockMvc.perform(put("/articles/like")
                        .param("articleId", articleId)
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(userDetails.getUsername())))
                .andExpect(status().isOk())
                .andDo(document("likes/articles/add",
                        requestParameters(
                                parameterWithName("articleId").description("게시글 아이디")
                        )
                        ));
    }

    @Test
    public void 좋아요_삭제() throws Exception {
        // given
        User user = new User("testuser", "testuser", "testuser@testuser.com", UserRole.USER);
        userRepository.save(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String text = "게시물 본문";
        String location = "{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}";
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<Long> imageIds = new ArrayList<>();

        Article article = articleService.createArticle(user, new ArticleCreateRequestDto(text, location, tagNames, imageIds));
        String articleId = Long.toString(article.getId());

        likeService.increaseLikeCount(user.getId(), article.getId());

        mockMvc.perform(put("/articles/unlike")
                        .param("articleId", articleId)
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(userDetails.getUsername())))
                .andExpect(status().isOk())
                .andDo(document("likes/articles/delete",
                        requestParameters(
                                parameterWithName("articleId").description("게시글 아이디")
                        )));
    }



    private MockMultipartFile getMockMultipartFile(String fileName, String originalFilename, String contentType, String path) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(new File(path));
        return new MockMultipartFile(fileName, originalFilename, contentType, fileInputStream);
    }
}
