package com.udangtangtang.backend.controller;

import com.udangtangtang.backend.domain.Article;
import com.udangtangtang.backend.dto.ArticleRequestDto;
import com.udangtangtang.backend.service.ArticleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ArticleController {
    private final ArticleService articleService;

    @GetMapping("/articles")
    public List<Article> getAllArticles () {
        return articleService.getArticles();
    }

    @PostMapping("/article/save")
    public Article createArticle(@RequestBody ArticleRequestDto requestDto) {
        return articleService.createArticle(requestDto, 0L);
    }
}
