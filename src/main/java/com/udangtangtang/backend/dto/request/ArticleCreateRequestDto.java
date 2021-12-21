package com.udangtangtang.backend.dto.request;

import com.udangtangtang.backend.domain.Location;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ArticleCreateRequestDto {
    private String text;
    private String location;
    private List<String> tagNames;
    private List<MultipartFile> imageFiles;
}
