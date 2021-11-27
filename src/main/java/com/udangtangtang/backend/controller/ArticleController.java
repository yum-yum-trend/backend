package com.udangtangtang.backend.controller;

import com.udangtangtang.backend.domain.Article;
import com.udangtangtang.backend.domain.Likes;
import com.udangtangtang.backend.dto.ArticleResponseDto;
import com.udangtangtang.backend.dto.LocationRequestDto;
import com.udangtangtang.backend.security.UserDetailsImpl;
import com.udangtangtang.backend.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/articles")
    public List<ArticleResponseDto> getArticles(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return articleService.getArticles(userDetails.getUser().getId());
    }

    @GetMapping("/articles/{id}")
    public ArticleResponseDto getArticle(@PathVariable Long id, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return articleService.getArticle(id, userDetails.getUser().getId());
    }

    @PostMapping("/articles")
    public void createArticle(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                @RequestParam("text") String text,
                                @RequestParam("location") String locationJsonString,
                                @RequestParam("hashtagNameList") List<String> hashtagNameList,
                                @RequestParam("imageFileList") List<MultipartFile> imageFileList) {

        articleService.createArticle(userDetails.getUser(), text, new LocationRequestDto(locationJsonString), hashtagNameList, imageFileList);
    }

    @PutMapping("/articles/like")
    public void increaseLikeCount(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                  @RequestParam("articleId") Long articleId) {
        articleService.increaseLikeCount(userDetails.getId(), articleId);
    }

    @PutMapping("/articles/unlike")
    public void decreaseLikeCount(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                  @RequestParam("articleId") Long articleId) {
        articleService.decreaseLikeCount(userDetails.getId(), articleId);
    }
}
