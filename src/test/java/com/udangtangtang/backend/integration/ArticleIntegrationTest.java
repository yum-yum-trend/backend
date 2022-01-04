package com.udangtangtang.backend.integration;

import com.udangtangtang.backend.domain.Article;
import com.udangtangtang.backend.domain.User;
import com.udangtangtang.backend.domain.UserRole;
import com.udangtangtang.backend.dto.request.ArticleCreateRequestDto;
import com.udangtangtang.backend.dto.request.ArticleUpdateRequestDto;
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

    User user = new User("Kermit", "Kermit1234", "Kermit@gaegulgaegul.com", UserRole.USER);
    Article createdArticle = null;
    Long userId = null;

    @Test
    @DisplayName("새로운 게시물 등록")
    @Transactional
    void createArticle() {
        // given
        this.user = userRepository.save(user);
        Long userId = user.getId();

        String text = "게시물 본문";
        String location = "{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}";
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<Long> imageIds = new ArrayList<>();

        // when
        Article article = articleService.createArticle(this.user, new ArticleCreateRequestDto(text, location, tagNames, imageIds));

        // then
        // assertEquals(article.getId(), articleId); // 불가능한 비교 why? Service 에서 객체 저장하면 아이디가 새로 부여되기 때문에
        assertNotNull(article.getId()); // 게시글이 데이터베이스에 저장되지 않은 경우 ID = NULL
        assertEquals(article.getUser().getId(), userId);
        assertEquals(article.getText(), text);
        assertEquals(article.getLocation().getPlaceName(), "연돈");

        for(int i = 0; i < tagNames.size(); i++) {
            assertEquals(article.getTags().get(i).getName(), tagNames.get(i));
        }
    }

    @Test
    @DisplayName("새로 등록한 게시물 수정")
    @Transactional
    void updateArticle() throws IOException {
        this.user = userRepository.save(user);
        Long userId = user.getId();

        String text = "게시물 본문";
        String location = "{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}";
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<Long> imageIds = new ArrayList<>();

        Article createdArticle = articleService.createArticle(this.user, new ArticleCreateRequestDto(text, location, tagNames, imageIds));

        String modifiedText = "게시물 수정된 내용";
        String modifiedLocation = "{}";
        List<String> modifiedTagNames = Arrays.asList("맛있는", "간식");
        List<Long> modifiedImageIds = new ArrayList<>();

        Article updatedArticle = articleService.updateArticle(this.user, createdArticle.getId(), new ArticleUpdateRequestDto(modifiedText, modifiedLocation, modifiedTagNames, modifiedImageIds));

        assertNotNull(updatedArticle.getId());
        assertEquals(updatedArticle.getUser().getId(), userId);
        assertEquals(updatedArticle.getText(), modifiedText);
        assertEquals(updatedArticle.getLocation().getPlaceName(), "집");
        assertEquals(updatedArticle.getImages().size(), 0);
        for(int i = 0; i < modifiedTagNames.size(); i++) {
            assertEquals(updatedArticle.getTags().get(i).getName(), modifiedTagNames.get(i));
        }
    }

    @Test
    @DisplayName("상품 검색")
    @Transactional
    void getArticlesThroughSearch() throws IOException {
        this.user = userRepository.save(user);

        String searchTag = "간식";
        String sortBy = "createdAt";
        boolean isAsc = false;
        int page = 1;
        String text = "게시물 본문";
        String category = "";
        String tagName = "";
        Long lastArticleId = 0L;

        String location = "{}";
        List<String> tagNames = Arrays.asList("맛있는", "간식");
        List<Long> imageIds = new ArrayList<>();

        articleService.createArticle(this.user, new ArticleCreateRequestDto(text, location, tagNames, imageIds));

        // then
        Page<Article> articles = articleService.getArticles(searchTag, location, category, tagName, sortBy, isAsc, page, lastArticleId);

        for(Article article : articles.getContent()) {
            assertTrue(article.getTags().contains(searchTag));
        }
    }

    @Test
    @DisplayName("모든 상품 조회 후 새로 등록한 상품 확인하기")
    @Transactional
    void getArticles() throws IOException {
        this.user = userRepository.save(user);
        Long userId = user.getId();

        String searchTag = "";
        String searchLocation = "";
        String sortBy = "createdAt";
        boolean isAsc = false;
        int page = 0;
        String location = "{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}";
        String category = "";
        String tagName = "";
        Long lastArticleId = 0L;

        String text = "게시물 본문";
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<Long> imageIds = new ArrayList<>();

        Article createdArticle = articleService.createArticle(this.user, new ArticleCreateRequestDto(text, location, tagNames, imageIds));

        Page<Article> articles = articleService.getArticles(searchTag, searchLocation, category, tagName, sortBy, isAsc, page, lastArticleId);

        Article foundArticle = articles.getContent().stream()
                                .filter(article -> article.getId().equals(createdArticle.getId()))
                                .findFirst().orElse(null);

        assertNotNull(foundArticle);
        assertEquals(foundArticle.getUser().getId(), userId);
        assertEquals(foundArticle.getId(), createdArticle.getId());
        assertEquals(foundArticle.getText(), createdArticle.getText());
    }

    @Test
    @DisplayName("특정 상품 조회하기")
    @Transactional
    void getArticle() throws IOException {
        this.user = userRepository.save(user);

        String text = "게시물 본문";
        String location = "{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}";
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<Long> imageIds = new ArrayList<>();

        Article createdArticle = articleService.createArticle(this.user, new ArticleCreateRequestDto(text, location, tagNames, imageIds));

        Article article = articleService.getArticle(createdArticle.getId());

        assertEquals(article.getId(), createdArticle.getId());
    }

    @Test
    @Transactional
    @DisplayName("트랜드 지역 상품 조회하기")
    void getTrendArticles() {
        String searchTag = "";
        String sortBy = "createdAt";
        boolean isAsc = false;
        int page = 0;
        String location1 = "제주";
        String category = "";
        String tagName = "";
        Long lastArticleId = 0L;

        this.user = userRepository.save(user);
        this.userId = user.getId();

        String text = "게시물 본문";
        String location = "{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}";
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<Long> imageIds = new ArrayList<>();

        Article article1 = articleService.createArticle(this.user, new ArticleCreateRequestDto(text, location, tagNames, imageIds));

        this.createdArticle = article1;

        Page<Article> articles = articleService.getArticles(searchTag, location1, category, tagName, sortBy, isAsc, page, lastArticleId);

        Article foundArticle = articles.getContent().stream()
                .filter(article -> article.getId().equals(this.createdArticle.getId()))
                .findFirst().orElse(null);

        assertNotNull(foundArticle);
        assertEquals(foundArticle.getUser().getId(), this.userId);
        assertEquals(foundArticle.getId(), this.createdArticle.getId());
        assertEquals(foundArticle.getText(), this.createdArticle.getText());
    }

    @Test
    @Transactional
    @DisplayName("트랜드 카테고리 상품 조회하기")
    void getTrendCategoryArticles() throws IOException {
        String searchTag = "";
        String sortBy = "createdAt";
        boolean isAsc = false;
        int page = 0;
        String location1 = "";
        String category = "돈까스,우동";
        String tagName = "";
        Long lastArticleId = 0L;

        this.user = userRepository.save(user);
        this.userId = user.getId();

        String text = "게시물 본문";
        String location = "{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}";
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<Long> imageIds = new ArrayList<>();

        Article article1 = articleService.createArticle(this.user, new ArticleCreateRequestDto(text, location, tagNames, imageIds));

        this.createdArticle = article1;

        Page<Article> articles = articleService.getArticles(searchTag, location1, category, tagName, sortBy, isAsc, page, lastArticleId);

        Article foundArticle = articles.getContent().stream()
                .filter(article -> article.getId().equals(this.createdArticle.getId()))
                .findFirst().orElse(null);

        assertNotNull(foundArticle);
        assertEquals(foundArticle.getUser().getId(), this.userId);
        assertEquals(foundArticle.getId(), this.createdArticle.getId());
        assertEquals(foundArticle.getText(), this.createdArticle.getText());
    }

    @Test
    @Transactional
    @DisplayName("트랜드 태그 상품 조회하기")
    void getTrendTagArticles() throws IOException {
        String searchTag = "";
        String sortBy = "createdAt";
        boolean isAsc = false;
        int page = 0;
        String location1 = "";
        String category = "";
        String tagName = "얌얌트랜드";
        Long lastArticleId = 0L;

        this.user = userRepository.save(user);
        this.userId = user.getId();

        String text = "게시물 본문";
        String location = "{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}";
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<Long> imageIds = new ArrayList<>();

        Article article1 = articleService.createArticle(this.user, new ArticleCreateRequestDto(text, location, tagNames, imageIds));

        this.createdArticle = article1;

        Page<Article> articles = articleService.getArticles(searchTag, location1, category, tagName, sortBy, isAsc, page, lastArticleId);

        Article foundArticle = articles.getContent().stream()
                .filter(article -> article.getId().equals(this.createdArticle.getId()))
                .findFirst().orElse(null);

        assertNotNull(foundArticle);
        assertEquals(foundArticle.getUser().getId(), this.userId);
        assertEquals(foundArticle.getId(), this.createdArticle.getId());
        assertEquals(foundArticle.getText(), this.createdArticle.getText());
    }

    @Test
    @Order(6)
    @DisplayName("게시물 삭제")
    @Transactional
    void deleteArticle() throws IOException, ApiRequestException {
        this.user = userRepository.save(user);
        Long userId = user.getId();

        String text = "게시물 본문";
        String location = "{\"roadAddressName\":\"제주특별자치도 서귀포시 일주서로 968-10\",\"placeName\":\"연돈\",\"xCoordinate\":\"126.40715814631936\",\"yCoordinate\":\"33.258895288625645\",\"categoryName\":\"음식점 > 일식 > 돈까스,우동\"}";
        List<String> tagNames = Arrays.asList("얌얌트랜드", "음식", "사진", "공유");
        List<Long> imageIds = new ArrayList<>();

        Article createdArticle = articleService.createArticle(this.user, new ArticleCreateRequestDto(text, location, tagNames, imageIds));


        Long deletedArticleId = articleService.deleteArticle(createdArticle.getId());

        assertEquals(deletedArticleId, createdArticle.getId());
        // 예외 '메시지' 와 비교하기
        Exception exception = assertThrows(ApiRequestException.class, () -> {
            articleService.getArticle(deletedArticleId);
        });
        assertThat(exception.getMessage()).isEqualTo(String.format("해당되는 아이디(%d)의 게시물이 없습니다.", deletedArticleId));
    }
}
