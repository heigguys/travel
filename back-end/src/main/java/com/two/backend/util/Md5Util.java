package com.two.backend.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 摘要工具类，用于对登录和修改密码时的明文密码做一致性转换。
 */
public final class Md5Util {
    private Md5Util() {
    }

    /**
     * 将传入字符串按 UTF-8 编码计算为 32 位十六进制 MD5 字符串。
     *
     * @param value 原始字符串
     * @return MD5 十六进制摘要
     */
    public static String md5(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte b : bytes) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 algorithm not available", e);
        }
    }
}
