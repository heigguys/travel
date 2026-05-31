package com.two.backend.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalStorageService implements StorageService {
    private final Path root;

    public LocalStorageService(@Value("${app.storage.root}") String root) throws IOException {
        this.root = Path.of(root).toAbsolutePath().normalize();
        Files.createDirectories(this.root);
    }

    @Override
    public StoredFile store(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }
        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "attachment.pdf" : file.getOriginalFilename());
        if (!original.toLowerCase().endsWith(".pdf")) {
            throw new BusinessException("附件只能上传 PDF 文件");
        }
        String stored = UUID.randomUUID() + "-" + original;
        Path target = root.resolve(stored).normalize();
        if (!target.startsWith(root)) {
            throw new BusinessException("文件名不合法");
        }
        file.transferTo(target);
        return new StoredFile(stored, original);
    }

    @Override
    public Resource load(String path) {
        try {
            Path file = root.resolve(path).normalize();
            if (!file.startsWith(root) || !Files.exists(file)) {
                throw new BusinessException("附件不存在");
            }
            return new UrlResource(file.toUri());
        } catch (IOException e) {
            throw new BusinessException("附件读取失败");
        }
    }

    @Override
    public Path root() {
        return root;
    }
}
