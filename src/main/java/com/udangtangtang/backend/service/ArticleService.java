package com.udangtangtang.backend.service;

import com.udangtangtang.backend.domain.*;
import com.udangtangtang.backend.dto.request.ArticleCreateRequestDto;
import com.udangtangtang.backend.dto.request.ArticleUpdateRequestDto;
import com.udangtangtang.backend.dto.request.LocationRequestDto;
import com.udangtangtang.backend.exception.ApiRequestException;
import com.udangtangtang.backend.repository.*;
import com.udangtangtang.backend.util.LocationDataPreprocess;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final ImageRepository imageRepository;
    private final LikesRepository likesRepository;

    private final LocationDataPreprocess locationDataPreprocess;

    public Page<Article> getArticles(String searchTag, String location, String category, String tagName, String sortBy, boolean isAsc, int page) {
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, 32, sort);

        if (searchTag.isEmpty()) {
            if (location.isEmpty()) {
                if (category.isEmpty() && tagName.isEmpty()) {
                    return articleRepository.findAll(pageable);
                } else if(tagName.isEmpty()) {
                    return articleRepository.findAllByLocationCategoryName(pageable, category);
                } else {
                    return articleRepository.findAllByTagsName(tagName, pageable);
                }
            } else {
                if (category.isEmpty() && tagName.isEmpty()) {
                    return articleRepository.findAllByLocationRoadAddressNameStartsWith(pageable, location);
                } else if(tagName.isEmpty()) {
                    return articleRepository.findAllByLocationRoadAddressNameStartsWithAndLocationCategoryName(pageable, location, category);
                } else {
                    return articleRepository.findAllByLocationRoadAddressNameStartsWithAndTagsName(pageable, location, tagName);
                }
            }
        } else {
            return articleRepository.findAllByTagsName(searchTag, pageable);
        }
    }

    public Article getArticle(Long id) {
        return articleRepository.findById(id).orElseThrow(
                () -> new ApiRequestException(String.format("해당되는 아이디(%d)의 게시물이 없습니다.", id))
        );
    }

    @Transactional
    public Article createArticle(User user, ArticleCreateRequestDto requestDto) {
        LocationRequestDto locationRequestDto = new LocationRequestDto(requestDto.getLocation());
        locationDataPreprocess.categoryNamePreprocess(locationRequestDto);
        Location location = locationRepository.save(new Location(locationRequestDto, user.getId()));

        Article article = new Article(requestDto.getText(), location, user);

        for (String name : requestDto.getTagNames()) {
            article.addTag(new Tag(name, article));
        }

        List<Image> images = new ArrayList<>();
        for (Long imageId : requestDto.getImageIds()) {
            Image image = imageRepository.findById(imageId).orElseThrow(
                    () -> new ApiRequestException(String.format("해당되는 아이디(%d)의 이미지가 없습니다.", imageId))
            );
            image.setArticle(article);
            images.add(image);
        }

        article.setImages(images);

        return articleRepository.save(article);
    }

    @Transactional
    public Article updateArticle(User user, Long id, ArticleUpdateRequestDto requestDto) {
        Article article = articleRepository.findById(id).orElseThrow(
                () -> new ApiRequestException(String.format("해당되는 아이디(%d)의 게시물이 없습니다.", id))
        );

        // 게시물 본문 내용
        String text = requestDto.getText();

        // 위치 정보
        LocationRequestDto locationRequestDto = new LocationRequestDto(requestDto.getLocation());
        locationDataPreprocess.categoryNamePreprocess(locationRequestDto);
        Location location = locationRepository.save(new Location(locationRequestDto, user.getId()));

        // 태그 리스트
        List<Tag> tags = new ArrayList<>();
        for (String tag : requestDto.getTagNames()) {
            tags.add(new Tag(tag, article));
        }

        // 이미지 리스트
        List<Image> images = new ArrayList<>();
        for (Long imageId : requestDto.getImageIds()) {
            Image image = imageRepository.findById(imageId).orElseThrow(
                    () -> new ApiRequestException(String.format("해당되는 아이디(%d)의 이미지가 없습니다.", imageId))
            );
            image.setArticle(article);
            images.add(image);
        }

        article.update(text, location, tags, images);
        return articleRepository.save(article);
    }

    @Transactional
    public Long deleteArticle(Long id) {
        Article article = articleRepository.findById(id).orElseThrow(
                () -> new ApiRequestException(String.format("해당되는 아이디(%d)의 게시물이 없습니다.", id))
        );

        for(Image image : imageRepository.findAllByArticleId(id)) {
            image.setArticle(null);
        }

        // 게시글에 등록된 좋아요 리스트 삭제
        List<Likes> likeList = likesRepository.findAllByArticleId(article.getId());
        for (Likes like : likeList) {
            likesRepository.delete(like);
        }

        articleRepository.delete(article);
        return id;
    }
}
