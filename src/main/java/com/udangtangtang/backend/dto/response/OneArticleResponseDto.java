package com.udangtangtang.backend.dto.response;

import com.udangtangtang.backend.domain.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@Getter
@Setter
public class OneArticleResponseDto {
    private Long articleId;
    private String text;
    private Location location;

    private Long userId;
    private String username;
    private String userProfileImageUrl;

    private List<Tag> tags = new ArrayList<>();
    private List<Image> images = new ArrayList<>();
    private List<Comment> comments = new ArrayList<>();

    public OneArticleResponseDto(Article article, Location location) {
        this.articleId = article.getId();
        this.text = article.getText();
        this.location = location;

        this.userId = article.getUser().getId();
        this.username = article.getUser().getUsername();
        this.userProfileImageUrl = article.getUser().getUserProfileImageUrl();

        this.tags = article.getTags();
        this.images = article.getImages();
        this.comments = article.getComments();
    }
}
