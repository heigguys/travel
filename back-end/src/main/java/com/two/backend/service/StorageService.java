package com.two.backend.service;

import java.io.IOException;
import java.nio.file.Path;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * 附件存储接口，抽象旅行计划 PDF 的保存、读取和根目录访问。
 */
public interface StorageService {
    /**
     * 保存上传文件。
     *
     * @param file 上传文件
     * @return 存储文件信息；无文件时可返回 null
     */
    StoredFile store(MultipartFile file) throws IOException;

    /**
     * 读取已保存的文件资源。
     *
     * @param path 存储路径或文件名
     * @return 文件资源
     */
    Resource load(String path);

    /**
     * 获取存储根目录。
     *
     * @return 存储根目录
     */
    Path root();

    /**
     * 已保存文件的信息，包含系统内部路径和上传时的原始文件名。
     */
    record StoredFile(String path, String originalName) {
    }
}
