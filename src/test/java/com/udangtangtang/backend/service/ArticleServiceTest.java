package com.udangtangtang.backend.service;

import com.udangtangtang.backend.domain.Article;
import com.udangtangtang.backend.domain.Image;
import com.udangtangtang.backend.domain.Location;
import com.udangtangtang.backend.domain.User;
import com.udangtangtang.backend.dto.request.ArticleCreateRequestDto;
import com.udangtangtang.backend.dto.request.ArticleUpdateRequestDto;
import com.udangtangtang.backend.dto.request.LocationRequestDto;
import com.udangtangtang.backend.exception.ApiRequestException;
import com.udangtangtang.backend.repository.*;
import com.udangtangtang.backend.util.LocationDataPreprocess;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
    @InjectMocks
    ArticleService articleService;
    @Mock
    ArticleRepository articleRepository;
    @Mock
    LocationRepository locationRepository;
    @Mock
    ImageRepository imageRepository;
    @Mock
    LikesRepository likesRepository;
    @Mock
    LocationDataPreprocess locationDataPreprocess;
    @Mock
    ImageService imageService;


    @Nested
    @DisplayName("모든 게시물 조회")
    class GetArticles {

    }

    @Nested
    @DisplayName("특정 게시물 조회")
    class GetArticle {

    }

    @Nested
    @DisplayName("게시물 생성")
    class CreateArticle {
        private String text;
        private User user;
        private String locationJsonString;
        private Location location;
        private List<String> tagNames;
        private List<Long> imageIds;
        private Article article;

        @BeforeEach
        void setup() throws IOException {
            text = "새로운 게시물 내용";
            user = new User();
            locationJsonString = "{}";
            LocationRequestDto locationRequestDto = new LocationRequestDto(locationJsonString);
            location = new Location(locationRequestDto, user.getId());
            tagNames = new ArrayList<>();
            imageIds = new ArrayList<>();
            article =  new Article(text, location, user);

            String fileName = "article-images/eac6ee49-ffd3-4a23-81c2-55369fa17c92.jpeg";
            String url = "https://amazon.com/article-images/eac6ee49-ffd3-4a23-81c2-55369fa17c92.jpeg";

            Image image1 = new Image(fileName, url);
            Image image2 = new Image(fileName, url);
            image1.setId(1000L);
            image2.setId(2000L);

            article.setImages(Arrays.asList(image1, image2));
            image1.setArticle(article);
            image2.setArticle(article);

            when(imageRepository.findById(image1.getId())).thenReturn(Optional.of(image1));
            when(imageRepository.findById(image2.getId())).thenReturn(Optional.of(image2));

            imageIds.add(image1.getId());
            imageIds.add(image2.getId());
        }


        @Test
        @DisplayName("새로운 게시물 생성")
        void createArticleSuccess1() {
            // given
            when(articleRepository.save(any(Article.class))).thenReturn(article);
            // when
            Article result = articleService.createArticle(user, new ArticleCreateRequestDto(text, locationJsonString, tagNames, imageIds));
            // then
            assertThat(result.getText()).isEqualTo("새로운 게시물 내용");
            for(Image image : result.getImages()) {
                assertNotNull(image.getArticle());
            }
        }


        @Test
        @DisplayName("반환된 게시물이 NULL인 경우")
        void createArticleFail1() {
            when(articleRepository.save(any(Article.class))).thenReturn(null);

            Article result = articleService.createArticle(user, new ArticleCreateRequestDto(text, locationJsonString, tagNames, imageIds));

            assertThat(result).isNull();
        }

    }

    @Nested
    @DisplayName("게시물 수정")
    class ArticleUpdate {

        private Long id;
        private String text;
        private User user;
        private String locationJsonString;
        private Location location;
        private List<String> tagNames;
        private List<Long> imageIds;

        @BeforeEach
        void setup() {
            id = 100L;
            text = "게시물 내용";
            user = new User();
            locationJsonString = "{}";
            LocationRequestDto locationRequestDto = new LocationRequestDto(locationJsonString);
            location = new Location(locationRequestDto, user.getId());
            tagNames = new ArrayList<>();
            imageIds = new ArrayList<>();
        }

        @Test
        @DisplayName("기존의 게시물 본문 내용 수정")
        void updateArticleSuccess1() {
            Article article = new Article(text, location, user);
            article.setId(id); // FIXME

            when(articleRepository.save(any(Article.class))).thenReturn(article);
            when(articleRepository.findById(id)).thenReturn(Optional.of(article));

            String modifiedText = "* 수정된 게시물 내용 *";

            Article result = articleService.updateArticle(user, id, new ArticleUpdateRequestDto(modifiedText, locationJsonString, tagNames, imageIds));

            assertThat(result.getText()).isEqualTo(modifiedText);
        }

        @Test
        @DisplayName("아이디에 해당되는 게시물이 없는 경우")
        void updateArticleFail1() {
            Article article = new Article(text, location, user);
            article.setId(id); // FIXME

            Long undefinedId = 200L;

            when(articleRepository.findById(undefinedId)).thenThrow(
                    new ApiRequestException(String.format("해당되는 아이디(%d)의 게시물이 없습니다.", undefinedId)));

            String modifiedText = "* 수정된 게시물 내용 *";

            Exception exception = assertThrows(ApiRequestException.class, () -> {
                articleService.updateArticle(user, undefinedId, new ArticleUpdateRequestDto(modifiedText, locationJsonString, tagNames, imageIds));
            });

            assertThat(exception.getMessage()).isEqualTo(String.format("해당되는 아이디(%d)의 게시물이 없습니다.", undefinedId));
        }
    }

    private MockMultipartFile getMockMultipartFile(String fileName, String originalFilename, String contentType, String path) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(new File(path));
        return new MockMultipartFile(fileName, originalFilename, contentType, fileInputStream);
    }
}