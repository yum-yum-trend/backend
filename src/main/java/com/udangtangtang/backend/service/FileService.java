package com.udangtangtang.backend.service;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.udangtangtang.backend.domain.FileFolder;

import java.io.InputStream;

public interface FileService {
    void uploadFile(InputStream inputStream, ObjectMetadata objectMetadata, String fileName);

    void deleteFile(String fileName);

    boolean getFile(String fileName);

    String getFileUrl(String fileName);

    String getFileFolder(FileFolder fileFolder);
}
