package com.udangtangtang.backend.service;

import com.udangtangtang.backend.domain.Article;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {
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
    FileProcessService fileProcessService;

    @Nested
    @DisplayName("게시물 생성")
    class CreateArticle {
        private String text;
        private User user;
        private String locationJsonString;
        private Location location;
        private List<String> tagNames;
        private List<MultipartFile> imageFiles;

        @BeforeEach
        void setup() {
            text = "새로운 게시물 내용";
            user = new User();
            locationJsonString = "{}";
            LocationRequestDto locationRequestDto = new LocationRequestDto(locationJsonString);
            location = new Location(locationRequestDto, user.getId());
            tagNames = new ArrayList<>();
            imageFiles = new ArrayList<>();
        }

        @Nested
        @DisplayName("정상 케이스")
        class SuccessCase {
            @Test
            @DisplayName("새로운 게시물 생성")
            void createArticleSuccess1() {
                Article article = new Article(text, location, user);

                when(articleRepository.save(any(Article.class))).thenReturn(article);

                ArticleService articleService = new ArticleService(articleRepository, locationRepository, imageRepository, likesRepository, locationDataPreprocess, fileProcessService);
                Article result = articleService.createArticle(user, new ArticleCreateRequestDto(text, locationJsonString, tagNames, imageFiles));

                assertThat(result.getText()).isEqualTo("새로운 게시물 내용");
            }
        }

        @Nested
        @DisplayName("비정상 케이스")
        class FailCase {
            @Test
            @DisplayName("반환된 게시물이 NULL인 경우")
            void createArticleFail1() {
                when(articleRepository.save(any(Article.class))).thenReturn(null);

                ArticleService articleService = new ArticleService(articleRepository, locationRepository, imageRepository, likesRepository, locationDataPreprocess, fileProcessService);
                Article result = articleService.createArticle(user, new ArticleCreateRequestDto(text, locationJsonString, tagNames, imageFiles));

                assertThat(result).isNull();
            }
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
        private List<MultipartFile> imageFiles;
        private List<Long> rmImageIdList;

        @Nested
        @DisplayName("정상 케이스")
        class SuccessCase {

            @BeforeEach
            void setup() {
                id = 100L;
                text = "게시물 내용";
                user = new User();
                locationJsonString = "{}";
                LocationRequestDto locationRequestDto = new LocationRequestDto(locationJsonString);
                location = new Location(locationRequestDto, user.getId());
                tagNames = new ArrayList<>();
                imageFiles = new ArrayList<>();
                rmImageIdList = new ArrayList<>();
            }

            @Test
            @DisplayName("기존의 게시물 본문 내용 수정")
            void updateArticleSuccess1() {
                Article article = new Article(text, location, user);
                article.setId(id); // FIXME

                when(articleRepository.save(any(Article.class))).thenReturn(article);
                when(articleRepository.findById(id)).thenReturn(Optional.of(article));

                String modifiedText = "* 수정된 게시물 내용 *";

                ArticleService articleService = new ArticleService(articleRepository, locationRepository, imageRepository, likesRepository, locationDataPreprocess, fileProcessService);
                Article result = articleService.updateArticle(user, id, new ArticleUpdateRequestDto(modifiedText, locationJsonString, tagNames, imageFiles, rmImageIdList));

                assertThat(result.getText()).isEqualTo(modifiedText);
            }
        }

        @Nested
        @DisplayName("비정상 케이스")
        class FailCase {

            @BeforeEach
            void setup() {
                id = 100L;
                text = "게시물 내용";
                user = new User();
                locationJsonString = "{}";
                LocationRequestDto locationRequestDto = new LocationRequestDto();
                location = new Location(locationRequestDto, user.getId());
                tagNames = new ArrayList<>();
                imageFiles = new ArrayList<>();
                rmImageIdList = new ArrayList<>();
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

                ArticleService articleService = new ArticleService(articleRepository, locationRepository, imageRepository, likesRepository, locationDataPreprocess, fileProcessService);
                Exception exception = assertThrows(ApiRequestException.class, () -> {
                    articleService.updateArticle(user, undefinedId, new ArticleUpdateRequestDto(modifiedText, locationJsonString, tagNames, imageFiles, rmImageIdList));
                });

                assertThat(exception.getMessage()).isEqualTo(String.format("해당되는 아이디(%d)의 게시물이 없습니다.", undefinedId));
            }
        }
    }
}