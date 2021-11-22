package com.udangtangtang.backend.service;

import com.udangtangtang.backend.domain.Article;
import com.udangtangtang.backend.dto.ArticleRequestDto;
import com.udangtangtang.backend.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;

    @Transactional
    public Article createArticle(ArticleRequestDto requestDto, Long userId) {
        Article article = new Article(requestDto, userId);
        articleRepository.save(article);
        return article;
    }

    public List<Article> getArticles() {
        return articleRepository.findAll();
    }
}
