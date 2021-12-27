package com.udangtangtang.backend.service;

import com.udangtangtang.backend.domain.FileFolder;
import com.udangtangtang.backend.domain.Image;
import com.udangtangtang.backend.dto.request.ArticleUpdateRequestDto;
import com.udangtangtang.backend.exception.ApiRequestException;
import com.udangtangtang.backend.repository.ImageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ImageServiceTest {
    @InjectMocks
    ImageService imageService;

    @Mock
    ImageRepository imageRepository;

    @Mock
    FileProcessService fileProcessService;

    @Test
    @DisplayName("이미지 업로드")
    void uploadImageSuccess() throws IOException {
        // given
        MockMultipartFile imageFile = getMockMultipartFile("cute_chun_sik", "cute_chun_sik.jpeg", "multipart/form-data", "src/test/resources/images/cute_chun_sik.jpeg");
        String originalFileName = "cute_chun_sik.jpeg";
        String fileName = "article-images/eac6ee49-ffd3-4a23-81c2-55369fa17c92.jpeg";
        String url = "https://amazon.com/article-images/eac6ee49-ffd3-4a23-81c2-55369fa17c92.jpeg";
        Image image = new Image(fileName, url);

        when(fileProcessService.createFileName(FileFolder.ARTICLE_IMAGES, originalFileName)).thenReturn(fileName);
        when(fileProcessService.uploadImage(imageFile, fileName)).thenReturn(url);

        when(imageRepository.save(any(Image.class))).thenReturn(image);

        // when
        Image createdImage = imageService.uploadImage(imageFile);

        // then
        assertEquals(createdImage.getUrl(), image.getUrl());
        assertEquals(createdImage.getFileName(), image.getFileName());
    }

    @Test
    @DisplayName("이미지 삭제")
    void deleteImageSuccess() {
        // given
        String fileName = "article-images/eac6ee49-ffd3-4a23-81c2-55369fa17c92.jpeg";
        String url = "https://amazon.com/article-images/eac6ee49-ffd3-4a23-81c2-55369fa17c92.jpeg";

        Image image = new Image(fileName, url);
        Long imageId = image.getId();

        when(imageRepository.findById(imageId)).thenReturn(Optional.of(image));

        // when
        Long deletedId = imageService.deleteImage(imageId);

        // then
        assertEquals(deletedId, imageId);
    }

    @Test
    @DisplayName("이미지 삭제 시 해당되는 이미지가 없는 경우")
    void deleteImageFail() {
        // given
        String fileName = "article-images/eac6ee49-ffd3-4a23-81c2-55369fa17c92.jpeg";
        String url = "https://amazon.com/article-images/eac6ee49-ffd3-4a23-81c2-55369fa17c92.jpeg";

        Image image = new Image(fileName, url);
        Long imageId = image.getId();

        when(imageRepository.findById(imageId)).thenThrow(
                new ApiRequestException(String.format("해당되는 아이디(%d)의 이미지가 없습니다.", imageId)));

        // when
        Exception exception = assertThrows(ApiRequestException.class, () -> {
            imageService.deleteImage(imageId);
        });

        // then
        assertEquals(exception.getMessage(), String.format("해당되는 아이디(%d)의 이미지가 없습니다.", imageId));
    }

    @Test
    @DisplayName("게시글의 모든 이미지 삭제")
    void deleteAllImageSuccess() {
        // given
        String fileName = "article-images/eac6ee49-ffd3-4a23-81c2-55369fa17c92.jpeg";
        String url = "https://amazon.com/article-images/eac6ee49-ffd3-4a23-81c2-55369fa17c92.jpeg";

        Image image1 = new Image(fileName, url);
        Image image2 = new Image(fileName, url);
        when(imageRepository.findAllByArticleId(any())).thenReturn(Arrays.asList(image1, image2));

        // when
        imageService.deleteAllImage(1000L);

    }

    private MockMultipartFile getMockMultipartFile(String fileName, String originalFilename, String contentType, String path) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(new File(path));
        return new MockMultipartFile(fileName, originalFilename, contentType, fileInputStream);
    }
}
