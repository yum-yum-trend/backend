package com.udangtangtang.backend.integration;

import com.udangtangtang.backend.domain.Article;
import com.udangtangtang.backend.domain.User;
import com.udangtangtang.backend.domain.UserRole;
import com.udangtangtang.backend.dto.request.ArticleCreateRequestDto;
import com.udangtangtang.backend.dto.request.LocationRequestDto;
import com.udangtangtang.backend.dto.response.TrendResponseDto;
import com.udangtangtang.backend.repository.UserRepository;
import com.udangtangtang.backend.service.ArticleService;
import com.udangtangtang.backend.service.TrendService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TrendIntegrationTest {
    @Autowired
    ArticleService articleService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    TrendService trendService;

    User user = new User("Kermit1234", "Kermit1234", "Kermit1234@gaegulgaegul.com", UserRole.USER);
    Long userId = null;
    Article article = null;
    List<Long> imageIds = null;

    @Test
    @DisplayName("전체지도호출")
    @Transactional
    void trendMap() throws IOException {
        // given
        this.user = userRepository.save(user);
        this.userId = user.getId();

        String text = "게시물 본문1";
        String location = "{\"roadAddressName\":\"테스트 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 카페\"}";
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<MultipartFile> imageFiles = Arrays.asList(
                getMockMultipartFile("cute_chun_sik", "jpeg", "multipart/form-data", "src/test/resources/images/cute_chun_sik.jpeg"),
                getMockMultipartFile("ring_ding_kermit", "jpeg", "multipart/form-data", "src/test/resources/images/ring_ding_kermit.jpeg")
        );

        Article article1 = articleService.createArticle(this.user, new ArticleCreateRequestDto(text, location, tagNames, imageFiles));

        text = "게시물 본문2";
        location = "{\"roadAddressName\":\"테스트 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 카페\"}";
        tagNames = Arrays.asList("얌얌트랜드2", "음식2", "사진2", "공유2");
        imageFiles = Arrays.asList(
                getMockMultipartFile("cute_chun_sik", "jpeg", "multipart/form-data", "src/test/resources/images/cute_chun_sik.jpeg"),
                getMockMultipartFile("ring_ding_kermit", "jpeg", "multipart/form-data", "src/test/resources/images/ring_ding_kermit.jpeg")
        );

        Article article2 = articleService.createArticle(this.user, new ArticleCreateRequestDto(text, location, tagNames, imageFiles));

        // when
        List<TrendResponseDto> dto = trendService.getTrendData();

        for(TrendResponseDto value : dto) {
            if (value.getLocation() == "테스트") {
                assertEquals(value.getLocation(), "테스트");
                assertEquals(value.getNumberOfOrdersByRegion(), 2);
                assertEquals(value.getColor(), "BACDFF");
            }
        }
    }

    @Test
    @DisplayName("전체차트호출")
    @Transactional
    void trendChart() throws IOException {
        // given
        this.user = userRepository.save(user);
        this.userId = user.getId();

        String text = "게시물 본문1";
        String location = "{\"roadAddressName\":\"테스트 서귀포시 일주서로 968-10\",\"placeName\":\"테스트\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 음식점\"}";
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<MultipartFile> imageFiles = Arrays.asList(
                getMockMultipartFile("cute_chun_sik", "jpeg", "multipart/form-data", "src/test/resources/images/cute_chun_sik.jpeg"),
                getMockMultipartFile("ring_ding_kermit", "jpeg", "multipart/form-data", "src/test/resources/images/ring_ding_kermit.jpeg")
        );

        Article article1 = articleService.createArticle(this.user, new ArticleCreateRequestDto(text, location, tagNames, imageFiles));

        text = "게시물 본문2";
        location = "{\"roadAddressName\":\"테스트 서귀포시 일주서로 968-10\",\"placeName\":\"테스트\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 음식점\"}";
        tagNames = Arrays.asList("얌얌트랜드2", "음식2", "사진2", "공유2");
        imageFiles = Arrays.asList(
                getMockMultipartFile("cute_chun_sik", "jpeg", "multipart/form-data", "src/test/resources/images/cute_chun_sik.jpeg"),
                getMockMultipartFile("ring_ding_kermit", "jpeg", "multipart/form-data", "src/test/resources/images/ring_ding_kermit.jpeg")
        );

        Article article2 = articleService.createArticle(this.user, new ArticleCreateRequestDto(text, location, tagNames, imageFiles));

        String location1 = "테스트";
        // when
        List<TrendResponseDto> dto = trendService.getChartTrendData(location1);

        // then
        for(TrendResponseDto value : dto) {
            if (value.getLocation() == "테스트") {
                assertEquals(value.getCategoryName(), "음식점");
                assertEquals(value.getNumberOfOrderByCategoryName(), 2);
            }
        }
    }

    @Test
    @DisplayName("전체태그차트호출")
    @Transactional
    void trendTagChart() throws IOException {
        // given
        this.user = userRepository.save(user);
        this.userId = user.getId();

        String text = "게시물 본문1";
        String location = "{\"roadAddressName\":\"테스트 서귀포시 일주서로 968-10\",\"placeName\":\"테스트\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 음식점\"}";
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<MultipartFile> imageFiles = Arrays.asList(
                getMockMultipartFile("cute_chun_sik", "jpeg", "multipart/form-data", "src/test/resources/images/cute_chun_sik.jpeg"),
                getMockMultipartFile("ring_ding_kermit", "jpeg", "multipart/form-data", "src/test/resources/images/ring_ding_kermit.jpeg")
        );

        Article article1 = articleService.createArticle(this.user, new ArticleCreateRequestDto(text, location, tagNames, imageFiles));

        text = "게시물 본문2";
        location = "{\"roadAddressName\":\"테스트 서귀포시 일주서로 968-10\",\"placeName\":\"테스트\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 음식점\"}";
        tagNames = Arrays.asList("얌얌트랜드2", "음식2", "사진2", "공유2");
        imageFiles = Arrays.asList(
                getMockMultipartFile("cute_chun_sik", "jpeg", "multipart/form-data", "src/test/resources/images/cute_chun_sik.jpeg"),
                getMockMultipartFile("ring_ding_kermit", "jpeg", "multipart/form-data", "src/test/resources/images/ring_ding_kermit.jpeg")
        );

        Article article2 = articleService.createArticle(this.user, new ArticleCreateRequestDto(text, location, tagNames, imageFiles));

        String location1 = "";
        // when
        List<TrendResponseDto> dto = trendService.getTagChartTrendData(location1);

        // then
        for(TrendResponseDto value : dto) {
            if (value.getLocation() == "테스트") {
                assertEquals(value.getCategoryName(), "음식점");
                assertEquals(value.getNumberOfOrderByCategoryName(), 2);
            }
        }
    }

    private MockMultipartFile getMockMultipartFile(String fileName, String extension, String contentType, String path) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(new File(path));
        return new MockMultipartFile(fileName, fileName + "." + extension, contentType, fileInputStream);
    }
}
