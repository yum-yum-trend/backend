package com.udangtangtang.backend.dto.response;

import com.udangtangtang.backend.domain.Article;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@NoArgsConstructor
@Setter
@Getter
public class ArticleResponseDto {
    private Long id;
    private Long userId;
    private String username;
    private String userProfileImageUrl;
    private int commentLength;
    private String articleImageUrl;
    private LocalDateTime createdAt;

    public ArticleResponseDto(Article article) {
        this.id = article.getId();
        this.userId = article.getUser().getId();
        this.username = article.getUser().getUsername();
        this.userProfileImageUrl = article.getUser().getUserProfileImageUrl();
        this.commentLength = article.getComments().size();
        this.articleImageUrl = article.getImages().get(0).getUrl();
        this.createdAt = article.getCreatedAt();
    }
}
