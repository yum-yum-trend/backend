package com.udangtangtang.backend.controller;

import com.udangtangtang.backend.domain.Image;
import com.udangtangtang.backend.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/article/image")
    public Image uploadImage(@RequestParam("imageFile") MultipartFile imageFile) {
        return imageService.uploadImage(imageFile);
    }

    @DeleteMapping("/article/image/{imageId}")
    public Long deleteImage(@PathVariable Long imageId) {
        return imageService.deleteImage(imageId);
    }

    @DeleteMapping("/article/{articleId}/images")
    public void deleteAllImage(@PathVariable Long articleId) {
        imageService.deleteAllImage(articleId);
    }
}
