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
/**
 * 本地文件存储实现，负责保存和读取旅行计划 PDF 附件。
 */
public class LocalStorageService implements StorageService {
    private final Path root;

    public LocalStorageService(@Value("${app.storage.root}") String root) throws IOException {
        this.root = Path.of(root).toAbsolutePath().normalize();
        Files.createDirectories(this.root);
    }

    @Override
    /**
     * 保存上传的 PDF 附件，并返回系统内部存储文件名和原始文件名。
     *
     * @param file 上传文件
     * @return 存储文件信息；未上传文件时返回 null
     */
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
    /**
     * 根据存储路径读取本地附件资源。
     *
     * @param path 系统内部存储文件名
     * @return 可由 Spring 返回给浏览器的资源
     */
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
    /**
     * 返回本地附件存储根目录。
     *
     * @return 存储根目录
     */
    public Path root() {
        return root;
    }
}
