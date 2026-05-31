package com.two.backend.service;

import java.io.IOException;
import java.nio.file.Path;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface StorageService {
    StoredFile store(MultipartFile file) throws IOException;

    Resource load(String path);

    Path root();

    record StoredFile(String path, String originalName) {
    }
}
