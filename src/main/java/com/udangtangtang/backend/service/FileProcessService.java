package com.udangtangtang.backend.service;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.udangtangtang.backend.domain.FileFolder;
import com.udangtangtang.backend.exception.ApiRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileProcessService {

    private final FileService amazonS3Service;

    public String uploadImage(MultipartFile file, String fileName) {
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(file.getSize());
        objectMetadata.setContentType(file.getContentType());

        try (InputStream inputStream = file.getInputStream()) {
            amazonS3Service.uploadFile(inputStream, objectMetadata, fileName);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new ApiRequestException(String.format("파일 변환 중 에러가 발생했습니다 (%s)", file.getOriginalFilename()));
        }

        return amazonS3Service.getFileUrl(fileName);
    }

    public String createFileName(FileFolder fileFolder, String originalFileName) {
        return amazonS3Service.getFileFolder(fileFolder) + UUID.randomUUID().toString().concat(getFileExtension(originalFileName));
    }

    private String getFileExtension(String fileName) {
        return fileName.substring(fileName.lastIndexOf("."));
    }

    public void deleteImage(String fileName) {
        amazonS3Service.deleteFile(fileName);
    }
}
