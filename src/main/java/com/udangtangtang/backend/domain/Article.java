package com.udangtangtang.backend.domain;

import com.udangtangtang.backend.dto.ArticleRequestDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Setter
@Getter
@NoArgsConstructor
@Entity
public class Article {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String pictureUrl;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String hashTag;

    @Column(nullable = false)
    private Long userId;

    public Article(ArticleRequestDto requestDto, Long userId) {

        this.title = requestDto.getTitle();
        this.pictureUrl = requestDto.getPictureUrl();
        this.content = requestDto.getContent();
        this.location = requestDto.getLocation();
        this.hashTag = requestDto.getHashTag();
        this.userId = userId;
    }
}
