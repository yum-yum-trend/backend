package com.udangtangtang.backend.service;


import com.udangtangtang.backend.domain.*;
import com.udangtangtang.backend.dto.ArticleResponseDto;
import com.udangtangtang.backend.dto.LocationRequestDto;
import com.udangtangtang.backend.repository.*;
import com.udangtangtang.backend.util.LocationDataPreprocess;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.implementation.bytecode.Throw;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final LocationRepository locationRepository;
    private final HashtagRepository hashtagRepository;
    private final ImageRepository imageRepository;
    private final FileProcessService fileProcessService;
    private final LocationDataPreprocess locationDataPreprocess;
    private final UserRepository userRepository;
    private final LikesRepository likesRepository;

    public List<ArticleResponseDto> getArticles(Long userId) {
        List<Article> articleList = articleRepository.findAll();

        List<ArticleResponseDto> articleResponseDtoList = new ArrayList<>();
        for (Article article : articleList) {
            Long likeCount = likesRepository.countByArticleId(article.getId());

            if (likesRepository.findByUserIdAndArticleId(userId, article.getId()).isPresent()) {
                articleResponseDtoList.add(new ArticleResponseDto(article, likeCount, true));
            } else {
                articleResponseDtoList.add(new ArticleResponseDto(article, likeCount, false));
            }
        }
        return articleResponseDtoList;
    }

    public Article getArticle(Long id) { return articleRepository.findById(id).orElseThrow(
            () -> new NullPointerException("해당되는 아이디의 게시물이 없습니다.")
        );
    }

    @Transactional
    public void createArticle(User user, String text, LocationRequestDto locationRequestDto, List<String> hashtagNameList, List<MultipartFile> imageFileList) {
        locationDataPreprocess.categoryNamePreprocess(locationRequestDto);
        Location location = locationRepository.save(new Location(locationRequestDto, user.getId()));

        Article article = articleRepository.save(new Article(text, location, user));

        for(String tag : hashtagNameList) {
            hashtagRepository.save(new Hashtag(tag, article, user.getId()));
        }

        for(MultipartFile multipartFile : imageFileList) {
            String url = fileProcessService.uploadImage(multipartFile, FileFolder.ARTICLE_IMAGES);
            imageRepository.save(new Image(url, article));
        }
    }

    @Transactional
    public void increaseLikeCount(Long userId, Long articleId) {
        if(likesRepository.findByUserIdAndArticleId(userId, articleId).isPresent()) {
            throw new IllegalArgumentException("이미 좋아요를 누른 게시글입니다.");
        }
        userRepository.findById(userId).orElseThrow(
                () -> new NullPointerException("해당 유저가 존재하지 않습니다!")
        );
        articleRepository.findById(articleId).orElseThrow(
                () -> new NullPointerException("해당 글이 존재하지 않습니다!")
        );
        likesRepository.save(new Likes(userId, articleId));
    }

    @Transactional
    public void decreaseLikeCount(Long user, Long articleId) {
        Likes deleteLike = likesRepository.findByUserIdAndArticleId(user, articleId).orElseThrow(
                () -> new NullPointerException("해당 좋아요 항목이 존재하지 않습니다!")
        );
        userRepository.findById(user).orElseThrow(
                () -> new NullPointerException("해당 유저가 존재하지 않습니다!")
        );
        articleRepository.findById(articleId).orElseThrow(
                () -> new NullPointerException("해당 글이 존재하지 않습니다!")
        );
        likesRepository.delete(deleteLike);
    }
}
