package com.udangtangtang.backend.dto;

import com.udangtangtang.backend.domain.Article;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ArticleResponseDto {
    private Article article;
    private Long likeCount;
    private boolean likes;
}
