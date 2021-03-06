package com.udangtangtang.backend.service;

import com.udangtangtang.backend.domain.Article;
import com.udangtangtang.backend.domain.Likes;
import com.udangtangtang.backend.dto.response.LikeResponseDto;
import com.udangtangtang.backend.exception.ApiRequestException;
import com.udangtangtang.backend.repository.ArticleRepository;
import com.udangtangtang.backend.repository.LikesRepository;
import com.udangtangtang.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class LikeService {

    private final LikesRepository likesRepository;
    private final UserRepository userRepository;
    private final ArticleRepository articleRepository;

    public List<LikeResponseDto> getLikes(Long userId) {
        List<Article> articleList = articleRepository.findAll();

        List<LikeResponseDto> likeResponseDtoList = new ArrayList<>();
        for (Article article : articleList) {
            Long likeCount = likesRepository.countByArticleId(article.getId());

            if (likesRepository.findByUserIdAndArticleId(userId, article.getId()).isPresent()) {
                likeResponseDtoList.add(new LikeResponseDto(article.getId(), likeCount, true));
            } else {
                likeResponseDtoList.add(new LikeResponseDto(article.getId(), likeCount, false));
            }
        }
        return likeResponseDtoList;
    }

    public List<LikeResponseDto> getLikesUser(Long userId) {
        List<Article> articleList = articleRepository.findAllByUserId(userId);

        List<LikeResponseDto> likeResponseDtoList = new ArrayList<>();
        for (Article article : articleList) {
            Long likeCount = likesRepository.countByArticleId(article.getId());

            if (likesRepository.findByUserIdAndArticleId(userId, article.getId()).isPresent()) {
                likeResponseDtoList.add(new LikeResponseDto(article.getId(), likeCount, true));
            } else {
                likeResponseDtoList.add(new LikeResponseDto(article.getId(), likeCount, false));
            }
        }
        return likeResponseDtoList;
    }


    public List<LikeResponseDto> getLikesGuest() {
        List<Article> articleList = articleRepository.findAll();

        List<LikeResponseDto> likeResponseDtoList = new ArrayList<>();
        for (Article article : articleList) {
            Long likeCount = likesRepository.countByArticleId(article.getId());
            likeResponseDtoList.add(new LikeResponseDto(article.getId(), likeCount, false));
        }
        return likeResponseDtoList;
    }


    public LikeResponseDto getLike(Long id, Long userId) {
        Article article = articleRepository.findById(id).orElseThrow(
                () -> new ApiRequestException("???????????? ???????????? ???????????? ????????????.")
        );
        Long likeCount = likesRepository.countByArticleId(id);

        if (likesRepository.findByUserIdAndArticleId(userId, id).isPresent()) {
            LikeResponseDto likeResponseDto = new LikeResponseDto(article.getId(), likeCount, true);
            return likeResponseDto;
        } else {
            LikeResponseDto likeResponseDto = new LikeResponseDto(article.getId(), likeCount, false);
            return likeResponseDto;
        }
    }

    public LikeResponseDto getLikeGuest(Long id) {
        Article article = articleRepository.findById(id).orElseThrow(
                () -> new ApiRequestException("???????????? ???????????? ???????????? ????????????.")
        );
        Long likeCount = likesRepository.countByArticleId(id);
        LikeResponseDto likeResponseDto = new LikeResponseDto(article.getId(), likeCount, false);

        return likeResponseDto;
    }

    @Transactional
    public Long increaseLikeCount(Long userId, Long articleId) {
        if(likesRepository.findByUserIdAndArticleId(userId, articleId).isPresent()) {
            throw new ApiRequestException("?????? ???????????? ?????? ??????????????????.");
        }
        userRepository.findById(userId).orElseThrow(
                () -> new ApiRequestException("?????? ????????? ???????????? ????????????!")
        );
        articleRepository.findById(articleId).orElseThrow(
                () -> new ApiRequestException("?????? ?????? ???????????? ????????????!")
        );
        likesRepository.save(new Likes(userId, articleId));
        return articleId;
    }

    @Transactional
    public Long decreaseLikeCount(Long user, Long articleId) {
        Likes deleteLike = likesRepository.findByUserIdAndArticleId(user, articleId).orElseThrow(
                () -> new ApiRequestException("?????? ????????? ????????? ???????????? ????????????!")
        );
        userRepository.findById(user).orElseThrow(
                () -> new ApiRequestException("?????? ????????? ???????????? ????????????!")
        );
        articleRepository.findById(articleId).orElseThrow(
                () -> new ApiRequestException("?????? ?????? ???????????? ????????????!")
        );
        likesRepository.delete(deleteLike);
        return articleId;
    }
}
