package com.udangtangtang.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArticleUpdateRequestDto {
    private String text;
    private String location;
    private List<String> tagNames;

//    @Nullable
    private List<MultipartFile> imageFiles;

//    @Nullable
    private List<Long> rmImageIds;
}
