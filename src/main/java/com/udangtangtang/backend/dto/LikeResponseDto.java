package com.udangtangtang.backend.dto;

import com.udangtangtang.backend.domain.Article;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LikeResponseDto {
    private Article article;
    private Long likeCount;
    private boolean like;

    public LikeResponseDto(Article article, Long likeCount, boolean like) {
        this.article = article;
        this.likeCount = likeCount;
        this.like = like;
    }
}
