package com.udangtangtang.backend.controller;


import com.udangtangtang.backend.domain.Article;
import com.udangtangtang.backend.dto.request.ArticleCreateRequestDto;
import com.udangtangtang.backend.dto.request.ArticleUpdateRequestDto;
import com.udangtangtang.backend.dto.request.LocationRequestDto;
import com.udangtangtang.backend.dto.response.ArticleResponseDto;
import com.udangtangtang.backend.security.UserDetailsImpl;
import com.udangtangtang.backend.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    public Page<ArticleResponseDto> getArticles(@RequestParam(required = false) String searchTag,
                                                @RequestParam(required = false) String location,
                                                @RequestParam(required = false) String category,
                                                @RequestParam(required = false) String tagName,
                                                @RequestParam("sortBy") String sortBy,
                                                @RequestParam("isAsc") boolean isAsc,
                                                @RequestParam("currentPage") int page) {
        return articleService.getArticles(searchTag, location, category, tagName, sortBy, isAsc, page);
    }

    @GetMapping("/article/{id}")
    public Article getArticle(@PathVariable Long id) {
        return articleService.getArticle(id);
    }

    @PostMapping("/articles")
    public Article createArticle(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                 @RequestBody ArticleCreateRequestDto articleCreateRequestDto) {

        return articleService.createArticle(userDetails.getUser(), articleCreateRequestDto);
    }

    @PostMapping("/article/{id}")
    public void updateArticle(@AuthenticationPrincipal UserDetailsImpl userDetails,
                              @PathVariable("id") Long id,
                              @RequestBody ArticleUpdateRequestDto articleUpdateRequestDto) {
        articleService.updateArticle(userDetails.getUser(), id, articleUpdateRequestDto);
    }

    @DeleteMapping("/article/{id}")
    public void deleteArticle(@AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable("id") Long id) {
        articleService.deleteArticle(id);
    }
}