package com.udangtangtang.backend.service;

import com.udangtangtang.backend.domain.FileFolder;
import com.udangtangtang.backend.domain.Image;
import com.udangtangtang.backend.exception.ApiRequestException;
import com.udangtangtang.backend.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final  FileProcessService fileProcessService;

    @Transactional
    public Image uploadImage(MultipartFile imageFile) {
        String fileName = fileProcessService.createFileName(FileFolder.ARTICLE_IMAGES, imageFile.getOriginalFilename());
        String url = fileProcessService.uploadImage(imageFile, fileName);

        return imageRepository.save(new Image(fileName, url));
    }

    @Transactional
    public Long deleteImage(Long imageId) {
        Image image = imageRepository.findById(imageId).orElseThrow(
                () -> new ApiRequestException(String.format("해당되는 아이디(%d)의 이미지가 없습니다.", imageId))
        );

        fileProcessService.deleteImage(image.getFileName());
        imageRepository.deleteById(imageId);
        return imageId;
    }

    @Transactional
    public void deleteAllImage(Long articleId) {
        List<Image> images = imageRepository.findAllByArticleId(articleId);

        for (Image image : images) {
            // S3에 업로드된 이미지 삭제
            fileProcessService.deleteImage(image.getFileName());
        }
        imageRepository.deleteAllById(images.stream().map(Image::getId).collect(Collectors.toList()));
    }
}
