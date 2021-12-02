package com.udangtangtang.backend.dto;

import com.udangtangtang.backend.domain.Article;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LikeResponseDto {
    private Long articleId;
    private Long likeCount;
    private boolean like;

    public LikeResponseDto(Long articleId, Long likeCount, boolean like) {
        this.articleId = articleId;
        this.likeCount = likeCount;
        this.like = like;
    }
}
