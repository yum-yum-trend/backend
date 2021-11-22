package com.udangtangtang.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class ArticleRequestDto {
    private String title;
    private String pictureUrl;
    private String content;
    private String location;
    private String hashTag;
}
