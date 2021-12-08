package com.udangtangtang.backend.service;

import com.udangtangtang.backend.domain.*;
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
    private final TagRepository tagRepository;
    private final ImageRepository imageRepository;

    private final LocationDataPreprocess locationDataPreprocess;
    private final FileProcessService fileProcessService;

    public Page<Article> getArticles(String searchTag, String sortBy, boolean isAsc, int page) {
        Sort.Direction direction = isAsc ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sort = Sort.by(direction, sortBy);
        Pageable pageable = PageRequest.of(page, 32, sort);

        if (searchTag.isEmpty()) {
            return articleRepository.findAll(pageable);
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
    public Article createArticle(User user, String text, LocationRequestDto locationRequestDto, List<String> tagNames, List<MultipartFile> imageFiles) {
        locationDataPreprocess.categoryNamePreprocess(locationRequestDto);
        Location location = locationRepository.save(new Location(locationRequestDto, user.getId()));

        Article article = new Article(text, location, user);

        for (String name : tagNames) {
            article.addTag(new Tag(name, article, user.getId()));
        }

        for (MultipartFile multipartFile : imageFiles) {
            String url = fileProcessService.uploadImage(multipartFile, FileFolder.ARTICLE_IMAGES);
            article.addImage(new Image(url, article));
        }

        return articleRepository.save(article);
    }

    @Transactional
    public Article updateArticle(User user, Long id, String text, LocationRequestDto locationRequestDto, List<String> tagNames, List<MultipartFile> imageFiles, List<Long> rmImageIds) {
        Article article = articleRepository.findById(id).orElseThrow(
                () -> new ApiRequestException(String.format("해당되는 아이디(%d)의 게시물이 없습니다.", id))
        );

        // 기존에 저장된 이미지 삭제
        if(rmImageIds != null) {
            for (Long imageId : rmImageIds) {
                Image image = imageRepository.findById(imageId).orElseThrow(
                        () -> new ApiRequestException(String.format("해당되는 아이디(%d)의 이미지가 없습니다.", imageId))
                );
                fileProcessService.deleteImage(image.getUrl());
                article.removeImage(imageId);
            }
            imageRepository.deleteAllById(rmImageIds);
        }

        locationDataPreprocess.categoryNamePreprocess(locationRequestDto);
        Location location = locationRepository.save(new Location(locationRequestDto, user.getId()));

        List<Tag> tags = new ArrayList<>();
        for (String tag : tagNames) {
            tags.add(new Tag(tag, article, user.getId()));
        }

        List<Image> images = new ArrayList<>();
        if (imageFiles != null) {
            for (MultipartFile multipartFile : imageFiles) {
                String url = fileProcessService.uploadImage(multipartFile, FileFolder.ARTICLE_IMAGES);
                images.add(new Image(url, article));
            }
        }

        article.update(text, location, tags, images);
        return articleRepository.save(article);
    }

    @Transactional
    public Long deleteArticle(Long id) {
        Article article = articleRepository.findById(id).orElseThrow(
                () -> new ApiRequestException(String.format("아이디(%d)에 해당되는 게시물이 없습니다.", id))
        );

        // S3에 업로드된 이미지 삭제
        for (Image image : article.getImages()) {
            fileProcessService.deleteImage(image.getUrl());
        }

        articleRepository.delete(article);
        return id;
    }
}
