package com.udangtangtang.backend.integration;

import com.udangtangtang.backend.domain.Article;
import com.udangtangtang.backend.domain.User;
import com.udangtangtang.backend.domain.UserRole;
import com.udangtangtang.backend.dto.request.LocationRequestDto;
import com.udangtangtang.backend.exception.ApiRequestException;
import com.udangtangtang.backend.repository.UserRepository;
import com.udangtangtang.backend.service.ArticleService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ArticleIntegrationTest {

    @Autowired
    ArticleService articleService;

    @Autowired
    UserRepository userRepository;

    User user = new User("Kermit", "1234", "Kermit@gaegulgaegul.com", UserRole.USER);
    Long userId = null;
    Article createdArticle = null;
    List<Long> imageIds = null;

    @AfterAll
    void clearDatabase() {
        userRepository.deleteById(userId);
    }

    @Test
    @Order(1)
    @DisplayName("새로운 게시물 등록")
    void createArticle() throws IOException {
        // given
        this.user = userRepository.save(user);
        this.userId = user.getId();

        String text = "게시물 본문";
        LocationRequestDto locationRequestDto = new LocationRequestDto("{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}");
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<MultipartFile> imageFiles = Arrays.asList(
                getMockMultipartFile("cute_chun_sik", "jpeg", "multipart/form-data", "src/test/resources/images/cute_chun_sik.jpeg"),
                getMockMultipartFile("ring_ding_kermit", "jpeg", "multipart/form-data", "src/test/resources/images/ring_ding_kermit.jpeg")
        );

        // when
        Article article = articleService.createArticle(this.user, text, locationRequestDto, tagNames, imageFiles);

        // then
        this.createdArticle = article;
        this.imageIds = new ArrayList<>();
        article.getImages().forEach(image -> this.imageIds.add(image.getId()));

        // assertEquals(article.getId(), articleId); // 불가능한 비교 why? Service 에서 객체 저장하면 아이디가 새로 부여되기 때문에
        assertNotNull(article.getId()); // 게시글이 데이터베이스에 저장되지 않은 경우 ID = NULL
        assertEquals(article.getUser().getId(), this.userId);
        assertEquals(article.getText(), text);
        assertEquals(article.getLocation().getPlaceName(), "연돈");

        // S3 File Find Function
        assertEquals(article.getImages().size(), imageFiles.size());

        for(int i = 0; i < tagNames.size(); i++) {
            assertEquals(article.getTags().get(i).getName(), tagNames.get(i));
        }
    }

    @Test
    @Order(2)
    @DisplayName("새로 등록한 게시물 수정")
    void updateArticle() throws IOException {
        String text = "게시물 수정된 내용";
        LocationRequestDto locationRequestDto = new LocationRequestDto("{}");
        List<String> tagNames = Arrays.asList("맛있는", "간식");
        List<MultipartFile> imageFiles = Arrays.asList(
                getMockMultipartFile("thinking_before_talking", "jpeg", "multipart/form-data", "src/test/resources/images/thinking_before_talking.jpeg")
        );

        Article article = articleService.updateArticle(this.user, this.createdArticle.getId(), text, locationRequestDto, tagNames, imageFiles, this.imageIds);

        this.createdArticle = article;

        assertNotNull(article.getId());
        assertEquals(article.getUser().getId(), this.userId);
        assertEquals(article.getText(), text);
        assertEquals(article.getLocation().getPlaceName(), "집");
        assertEquals(article.getImages().size(), 1);
        for(int i = 0; i < tagNames.size(); i++) {
            assertEquals(article.getTags().get(i).getName(), tagNames.get(i));
        }
    }

    @Test
    @Order(3)
    @DisplayName("상품 검색")
    void getArticlesThroughSearch() {
        String searchTag = "간식";
        String sortBy = "createdAt";
        boolean isAsc = false;
        int page = 1;

        Page<Article> articles = articleService.getArticles(searchTag, sortBy, isAsc, page);

        for(Article article : articles.getContent()) {
            assertTrue(article.getTags().contains(searchTag));
        }
    }

    @Test
    @Order(4)
    @DisplayName("모든 상품 조회 후 새로 등록한 상품 확인하기")
    void getArticles() {
        String searchTag = "";
        String sortBy = "createdAt";
        boolean isAsc = false;
        int page = 0;

        Page<Article> articles = articleService.getArticles(searchTag, sortBy, isAsc, page);

        Article foundArticle = articles.getContent().stream()
                                .filter(article -> article.getId().equals(this.createdArticle.getId()))
                                .findFirst().orElse(null);

        assertNotNull(foundArticle);
        assertEquals(foundArticle.getUser().getId(), this.userId);
        assertEquals(foundArticle.getId(), this.createdArticle.getId());
        assertEquals(foundArticle.getText(), this.createdArticle.getText());
    }

    @Test
    @Order(5)
    @DisplayName("특정 상품 조회하기")
    void getArticle() {
        Article article = articleService.getArticle(this.createdArticle.getId());

        assertEquals(article.getId(), this.createdArticle.getId());
    }

    @Test
    @Order(6)
    @DisplayName("게시물 삭제")
    void deleteArticle() throws ApiRequestException {
        Long deletedArticleId = articleService.deleteArticle(this.createdArticle.getId());

        assertEquals(deletedArticleId, this.createdArticle.getId());
        // 예외 '메시지' 와 비교하기
        Exception exception = assertThrows(ApiRequestException.class, () -> {
            articleService.getArticle(deletedArticleId);
        });
        assertThat(exception.getMessage()).isEqualTo(String.format("해당되는 아이디(%d)의 게시물이 없습니다.", deletedArticleId));
    }

    private MockMultipartFile getMockMultipartFile(String fileName, String extension, String contentType, String path) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(new File(path));
        return new MockMultipartFile(fileName, fileName + "." + extension, contentType, fileInputStream);
    }
}
