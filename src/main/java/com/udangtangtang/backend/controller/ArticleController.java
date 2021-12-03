package com.udangtangtang.backend.controller;


import com.udangtangtang.backend.domain.Article;
import com.udangtangtang.backend.dto.LocationRequestDto;
import com.udangtangtang.backend.security.UserDetailsImpl;
import com.udangtangtang.backend.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/articles")
    public List<Article> getArticles() {
        return articleService.getArticles();
    }

    @GetMapping("/articles/{id}")
    public Article getArticle(@PathVariable Long id) {
        return articleService.getArticle(id);
    }

    @PostMapping("/articles")
    public void createArticle(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                @RequestParam("text") String text,
                                @RequestParam("location") String locationJsonString,
                                @RequestParam("tagNames") List<String> tagNames,
                                @RequestParam("imageFiles") List<MultipartFile> imageFiles) {

        articleService.createArticle(userDetails.getUser(), text, new LocationRequestDto(locationJsonString), tagNames, imageFiles);
    }

    @PostMapping("/articles/{id}")
    public void updateArticle(@AuthenticationPrincipal UserDetailsImpl userDetails,
                              @PathVariable("id") Long id,
                              @RequestParam("text") String text,
                              @RequestParam("location") String locationJsonString,
                              @RequestParam("tagNames") List<String> tagNames,
                              @Nullable @RequestParam("imageFiles") List<MultipartFile> imageFiles,
                              @Nullable @RequestParam("rmImageIdList") List<Long> rmImageIds) {
        articleService.updateArticle(userDetails.getUser(), id, text, new LocationRequestDto(locationJsonString), tagNames, imageFiles, rmImageIds);
    }

    @DeleteMapping("/articles/{id}")
    public void deleteArticle(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable("id") Long id) {
        articleService.deleteArticle(id);
    }
}
