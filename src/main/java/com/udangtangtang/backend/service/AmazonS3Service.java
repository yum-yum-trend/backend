package com.udangtangtang.backend.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import com.udangtangtang.backend.config.AmazonS3Component;
import com.udangtangtang.backend.domain.FileFolder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.InputStream;


@Component
@RequiredArgsConstructor
public class AmazonS3Service implements FileService {

    private final AmazonS3 amazonS3;
    private final AmazonS3Component amazonS3Component;

    public void uploadFile(InputStream inputStream, ObjectMetadata objectMetadata, String fileName) {
        amazonS3.putObject(new PutObjectRequest(amazonS3Component.getBucket(), fileName, inputStream, objectMetadata).withCannedAcl(CannedAccessControlList.PublicReadWrite));
    }

    public void deleteFile(String fileName) {
        amazonS3.deleteObject(new DeleteObjectRequest(amazonS3Component.getBucket(), fileName));
    }

    public boolean getFile(String fileName) {
        S3Object object = null;
        try {
            object = amazonS3.getObject(new GetObjectRequest(amazonS3Component.getBucket(), fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return object != null;
    }


    // FIXME: Cloud Front URL
    public String getFileUrl(String fileName) {
        return amazonS3.getUrl(amazonS3Component.getBucket(), fileName).toString();
    }

    // FIXME: Key - Value
    public String getFileFolder(FileFolder fileFolder) {
        String folder = "";
        if (fileFolder == FileFolder.ARTICLE_IMAGES) {
            folder = amazonS3Component.getArticleImagesFolder();
        } else if (fileFolder == FileFolder.PROFILE_IMAGES) {
            folder = amazonS3Component.getProfileImagesFolder();
        }
        return folder;
    }
}