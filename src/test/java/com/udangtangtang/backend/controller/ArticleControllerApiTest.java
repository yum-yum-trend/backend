package com.udangtangtang.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udangtangtang.backend.domain.Article;
import com.udangtangtang.backend.domain.User;
import com.udangtangtang.backend.domain.UserRole;
import com.udangtangtang.backend.dto.request.ArticleCreateRequestDto;
import com.udangtangtang.backend.dto.request.ArticleUpdateRequestDto;
import com.udangtangtang.backend.repository.UserRepository;
import com.udangtangtang.backend.security.UserDetailsImpl;
import com.udangtangtang.backend.service.ArticleService;
import com.udangtangtang.backend.service.ImageService;
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
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import javax.servlet.ServletException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
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
public class ArticleControllerApiTest {
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
    ImageService imageService;

    List<Long> imageIds = new ArrayList<>();

    @BeforeAll
    public void createImages() throws IOException {
        MockMultipartFile file1 = getMockMultipartFile("imageFiles", "cute_chun_sik.jpeg", "multipart/form-data", "src/test/resources/images/cute_chun_sik.jpeg");
        MockMultipartFile file2 = getMockMultipartFile("imageFiles", "ring_ding_kermit.jpeg", "multipart/form-data", "src/test/resources/images/ring_ding_kermit.jpeg");

        imageIds.add(imageService.uploadImage(file1).getId());
        imageIds.add(imageService.uploadImage(file2).getId());
    }

    @AfterAll
    public void deleteImages() {
        for(Long imageId : imageIds) {
            imageService.deleteImage(imageId);
        }
    }

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
    public void 게시물_생성() throws Exception {
        // given
        User user = new User("Kermit", "Kermit1234", "Kermit@gaegulgaegul.com", UserRole.USER);
        userRepository.save(user);
        UserDetailsImpl userDetails = new UserDetailsImpl(user);

        String text = "게시물 본문";
        String locationRequestDto = "{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}";
//        String tagNames = "얌얌트랜드,음식,사진,공유";
        List<String> tagNames = Arrays.asList("얌얌트랜트", "음식", "사진", "공유");
        ArticleCreateRequestDto requestDto = new ArticleCreateRequestDto(text, locationRequestDto, tagNames, imageIds);

        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(requestDto);

        // when
        mockMvc.perform(post("/articles")
                        .contentType("application/json")
                        .accept(MediaType.APPLICATION_JSON)
                        .content(payload)
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(userDetails.getUsername()))
                )
                .andExpect(status().isOk())
                .andDo(document("article/create",
                        requestFields(
                                fieldWithPath("text").description("본문 내용"),
                                fieldWithPath("location").description("위치 정보 JSON 문자열"),
                                fieldWithPath("tagNames").description("태그 이름 리스트"),
                                fieldWithPath("imageIds").description("이미지 아이디 리스트")
                        )
                ));
    }

    @Test
    @Transactional
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
    @Transactional
    public void 특정_게시물_조회() throws Exception {
        // given
        User user = new User("Kermit", "Kermit1234", "Kermit@gaegulgaegul.com", UserRole.USER);
        userRepository.save(user);

        String text = "게시물 본문";
        String location = "{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}";
        List<String> tagNames = Arrays.asList("얌얌트랜드");

        Article article = articleService.createArticle(user, new ArticleCreateRequestDto(text, location, tagNames, imageIds));

        mockMvc.perform(get("/article/{id}", article.getId()))
                .andExpect(status().isOk())
                .andDo(document("article/read-one",
                        responseFields(
                            fieldWithPath("createdAt").description("게시물이 생성된 날짜"),
                                fieldWithPath("modifiedAt").description("게시물이 마지막으로 수정된 날짜"),
                                fieldWithPath("id").description("게시물 아이디"),
                                fieldWithPath("text").description("본문 내용"),
                                fieldWithPath("location.createdAt").description("위치 정보가 생성된 날짜"),
                                fieldWithPath("location.modifiedAt").description("위치 정보가 마지막으로 수정된 날짜"),
                                fieldWithPath("location.id").description("위치 아이디"),
                                fieldWithPath("location.placeName").description("음식점 이름"),
                                fieldWithPath("location.roadAddressName").description("위치의 도로명 주소"),
                                fieldWithPath("location.categoryName").description("음식점 카테고리"),
                                fieldWithPath("location.userId").description("음식점 정보를 저장한 사용자 아이디"),
                                fieldWithPath("location.xcoordinate").description("음식점 위치 위도"),
                                fieldWithPath("location.ycoordinate").description("음식점 위치 경도"),
                                fieldWithPath("user.createdAt").description("사용자 정보가 생성된 날짜"),
                                fieldWithPath("user.modifiedAt").description("사용자 정보가 수정된 날짜"),
                                fieldWithPath("user.id").description("사용자 아이디"),
                                fieldWithPath("user.password").description("사용자 비밀번호"),
                                fieldWithPath("user.username").description("사용자 이름"),
                                fieldWithPath("user.email").description("사용자 이메일"),
                                fieldWithPath("user.role").description("사용자 권한"),
                                fieldWithPath("user.kakaoId").description("카카오 회원가입 여부"),
                                fieldWithPath("user.userProfileIntro").description("프로필 소개글"),
                                fieldWithPath("user.userProfileImageUrl").description("프로필 이미지 URL"),
                                fieldWithPath("user.userProfileImageName").description("프로필 이미지 파일 이름"),
                                fieldWithPath("tags[].createdAt").description("태그가 생성된 날짜"),
                                fieldWithPath("tags[].modifiedAt").description("태그가 수정된 날짜"),
                                fieldWithPath("tags[].id").description("태그 아이디"),
                                fieldWithPath("tags[].name").description("태그 이름"),
                                fieldWithPath("images[].createdAt").description("태그가 생성된 날짜"),
                                fieldWithPath("images[].modifiedAt").description("태그가 수정된 날짜"),
                                fieldWithPath("images[].id").description("이미지 아이디"),
                                fieldWithPath("images[].fileName").description("이미지 파일 이름"),
                                fieldWithPath("images[].url").description("이미지 경로 URL"),
                                fieldWithPath("comments[]").description("댓글 리스트")
                        )
                ));
    }

    @Test
    @Transactional
    public void 게시물_수정() throws Exception {
        // given
        User user = new User("Kermit", "Kermit1234", "Kermit@gaegulgaegul.com", UserRole.USER);
        userRepository.save(user);

        String text = "게시물 본문";
        String locationRequestDto = "{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}";
//        String tagNames = "얌얌트랜드,음식,사진,공유";
        List<String> tagNames = Arrays.asList("얌얌트랜트", "음식", "사진", "공유");
        ArticleCreateRequestDto createRequestDto = new ArticleCreateRequestDto(text, locationRequestDto, tagNames, imageIds);

        Article article = articleService.createArticle(user, createRequestDto);

        String updateText = "게시물 본문 - 수정 내용";
        String updateLocationRequestDto = "{}";
        List<String> updateTagNames = Arrays.asList("서울", "맛집");
        List<Long> updateImageIds = new ArrayList<>();

        ArticleUpdateRequestDto updateRequestDto = new ArticleUpdateRequestDto(updateText, updateLocationRequestDto, updateTagNames, updateImageIds);

        ObjectMapper objectMapper = new ObjectMapper();
        String payload = objectMapper.writeValueAsString(updateRequestDto);

        mockMvc.perform(post("/article/{id}", article.getId())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType("application/json")
                        .content(payload)
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(user.getUsername()))
                )
                .andExpect(status().isOk())
                .andDo(document("article/update",
                        requestFields(
                                fieldWithPath("text").description("수정된 본문 내용"),
                                fieldWithPath("location").description("수정된 위치 정보 JSON 문자열"),
                                fieldWithPath("tagNames").description("수정된 태그 이름 리스트"),
                                fieldWithPath("imageIds").description("추가된 이미지 아이디 리스트")
                        )
                ));
    }

    @Test
    @Transactional
    public void 게시물_삭제() throws Exception {
        // given
        User user = new User("Kermit", "Kermit1234", "Kermit@gaegulgaegul.com", UserRole.USER);
        userRepository.save(user);

        String text = "게시물 본문";
        String location = "{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}";
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");

        Article article = articleService.createArticle(user, new ArticleCreateRequestDto(text, location, tagNames, imageIds));
        imageService.deleteAllImage(article.getId());

        mockMvc.perform(delete("/article/{id}", article.getId())
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(user.getUsername())))
                .andExpect(status().isOk())
                .andDo(document("article/delete"));
    }

    private MockMultipartFile getMockMultipartFile(String fileName, String originalFilename, String contentType, String path) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(new File(path));
        return new MockMultipartFile(fileName, originalFilename, contentType, fileInputStream);
    }
}
