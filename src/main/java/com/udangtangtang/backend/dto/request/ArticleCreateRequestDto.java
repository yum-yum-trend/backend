package com.udangtangtang.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArticleCreateRequestDto {
    private String text;
    private String location;
    private List<String> tagNames;
    private List<Long> imageIds;
}
