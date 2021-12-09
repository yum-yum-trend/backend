package com.udangtangtang.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.udangtangtang.backend.domain.Article;
import com.udangtangtang.backend.domain.Image;
import com.udangtangtang.backend.domain.User;
import com.udangtangtang.backend.domain.UserRole;
import com.udangtangtang.backend.dto.request.LocationRequestDto;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
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
    public void 특정_게시물_조회() throws Exception {
        // given
        User user = new User("Kermit", "1234", "Kermit@gaegulgaegul.com", UserRole.USER);
        userRepository.save(user);

        String text = "게시물 본문";
        LocationRequestDto locationRequestDto = new LocationRequestDto("{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}");
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<MultipartFile> imageFiles = Arrays.asList(
                getMockMultipartFile("cute_chun_sik", "cute_chun_sik.jpeg", "multipart/form-data", "src/test/resources/images/cute_chun_sik.jpeg"),
                getMockMultipartFile("ring_ding_kermit", "ring_ding_kermit.jpeg", "multipart/form-data", "src/test/resources/images/ring_ding_kermit.jpeg")
        );

        Article article = articleService.createArticle(user, text, locationRequestDto, tagNames, imageFiles);

        mockMvc.perform(get("/articles/{id}", article.getId()))
                .andExpect(status().isOk())
                .andDo(document("articles/read-one"));
    }

    @Test
    public void 게시물_수정() throws Exception {
        // given
        User user = new User("Kermit", "1234", "Kermit@gaegulgaegul.com", UserRole.USER);
        userRepository.save(user);

        String text = "게시물 본문";
        LocationRequestDto locationRequestDto = new LocationRequestDto("{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}");
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<MultipartFile> imageFiles = Arrays.asList(
                getMockMultipartFile("cute_chun_sik", "cute_chun_sik.jpeg", "multipart/form-data", "src/test/resources/images/cute_chun_sik.jpeg"),
                getMockMultipartFile("ring_ding_kermit", "ring_ding_kermit.jpeg", "multipart/form-data", "src/test/resources/images/ring_ding_kermit.jpeg")
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
    public void 게시물_삭제() throws Exception {
        // given
        User user = new User("Kermit", "1234", "Kermit@gaegulgaegul.com", UserRole.USER);
        userRepository.save(user);

        String text = "게시물 본문";
        LocationRequestDto locationRequestDto = new LocationRequestDto("{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}");
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<MultipartFile> imageFiles = Arrays.asList(
                getMockMultipartFile("cute_chun_sik", "cute_chun_sik.jpeg", "multipart/form-data", "src/test/resources/images/cute_chun_sik.jpeg"),
                getMockMultipartFile("ring_ding_kermit", "ring_ding_kermit.jpeg", "multipart/form-data", "src/test/resources/images/ring_ding_kermit.jpeg")
        );

        Article article = articleService.createArticle(user, text, locationRequestDto, tagNames, imageFiles);

        mockMvc.perform(delete("/articles/{id}", article.getId())
                        .header("Authorization", "Bearer " + jwtTokenUtil.generateAccessToken(user.getUsername())))
                .andExpect(status().isOk())
                .andDo(document("articles/delete"));
    }

    private MockMultipartFile getMockMultipartFile(String fileName, String originalFilename, String contentType, String path) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(new File(path));
        return new MockMultipartFile(fileName, originalFilename, contentType, fileInputStream);
    }
}
