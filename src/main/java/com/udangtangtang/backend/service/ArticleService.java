package com.udangtangtang.backend.service;


import com.udangtangtang.backend.domain.*;
import com.udangtangtang.backend.dto.LocationRequestDto;
import com.udangtangtang.backend.repository.ArticleRepository;
import com.udangtangtang.backend.repository.TagRepository;
import com.udangtangtang.backend.repository.ImageRepository;
import com.udangtangtang.backend.repository.LocationRepository;
import com.udangtangtang.backend.util.LocationDataPreprocess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ArticleService {

    private final ArticleRepository articleRepository;
    private final LocationRepository locationRepository;
    private final TagRepository tagRepository;
    private final ImageRepository imageRepository;
    private final FileProcessService fileProcessService;
    private final LocationDataPreprocess locationDataPreprocess;

    public List<Article> getArticles() {
        return articleRepository.findAll();
    }

    public Article getArticle(Long id) { return articleRepository.findById(id).orElseThrow(
            () -> new NullPointerException("해당되는 아이디의 게시물이 없습니다.")
        );
    }

    @Transactional
    public void createArticle(User user, String text, LocationRequestDto locationRequestDto, List<String> tagNames, List<MultipartFile> imageFiles) {
        locationDataPreprocess.categoryNamePreprocess(locationRequestDto);
        Location location = locationRepository.save(new Location(locationRequestDto, user.getId()));

        Article article = new Article(text, location, user);

        articleRepository.save(article);

        for(String name : tagNames) {
            tagRepository.save(new Tag(name, article, user.getId()));
        }

        for(MultipartFile multipartFile : imageFiles) {
            String url = fileProcessService.uploadImage(multipartFile, FileFolder.ARTICLE_IMAGES);
            imageRepository.save(new Image(url, article));
        }
    }

    @Transactional
    public void updateArticle(User user, Long id, String text, LocationRequestDto locationRequestDto, List<String> tagNames, List<MultipartFile> imageFiles) {
        Article article = articleRepository.findById(id).orElseThrow(
                () -> new NullPointerException("일치하는 게시물이 없습니다.")
        );

        locationDataPreprocess.categoryNamePreprocess(locationRequestDto);
        Location location = locationRepository.save(new Location(locationRequestDto, user.getId()));

        List<Tag> tags = new ArrayList<>();
        for(String tag : tagNames) {
            tags.add(new Tag(tag, article, user.getId()));
        }

        List<Image> images = new ArrayList<>();
        if (imageFiles != null) {
            for(MultipartFile multipartFile : imageFiles) {
                String url = fileProcessService.uploadImage(multipartFile, FileFolder.ARTICLE_IMAGES);
                images.add(new Image(url, article));
            }
        }

        article.update(text, location, tags, images);
        articleRepository.save(article);
    }

    @Transactional
    public void deleteArticleImage(Long id) {
        Image image = imageRepository.findById(id).orElseThrow(
                () -> new NullPointerException(String.format("아이디(%d)에 해당되는 이미지가 없습니다.", id))
        );
        fileProcessService.deleteImage(image.getUrl());
        imageRepository.deleteById(id);
    }
}
