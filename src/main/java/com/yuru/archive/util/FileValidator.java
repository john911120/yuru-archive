package com.yuru.archive.util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import org.springframework.web.multipart.MultipartFile;

import com.yuru.archive.exception.InvalidUploadFileException;

public final class FileValidator {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Map<String, Set<String>> ALLOWED_MIME_TYPES = Map.of(
            "jpeg", Set.of("image/jpeg", "image/jpg"),
            "png", Set.of("image/png"),
            "gif", Set.of("image/gif"),
            "webp", Set.of("image/webp"));

    private FileValidator() {
    }

    public static void validateImage(MultipartFile file, String safeFileName) {
        if (file == null || file.isEmpty()) {
            throw new InvalidUploadFileException("空のファイルはアップロードできません。");
        }

        String extension = extensionOf(safeFileName);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new InvalidUploadFileException("許可されていない画像形式です。");
        }

        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (IOException e) {
            throw new InvalidUploadFileException("アップロードファイルを読み込めませんでした。", e);
        }

        String detectedType = detectType(bytes);
        if (detectedType == null) {
            throw new InvalidUploadFileException("画像ファイルの実体を確認できませんでした。");
        }

        if (!extensionMatches(extension, detectedType)) {
            throw new InvalidUploadFileException("ファイル拡張子と画像の実体が一致しません。");
        }

        String contentType = file.getContentType();
        if (contentType == null
                || !ALLOWED_MIME_TYPES.getOrDefault(detectedType, Set.of())
                        .contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new InvalidUploadFileException("Content-Typeと画像の実体が一致しません。");
        }

        if ("webp".equals(detectedType)) {
            validateWebp(bytes);
        } else {
            validateDecodableImage(bytes);
        }
    }

    private static String extensionOf(String fileName) {
        if (fileName == null) {
            return "";
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    private static boolean extensionMatches(String extension, String detectedType) {
        if ("jpeg".equals(detectedType)) {
            return "jpg".equals(extension) || "jpeg".equals(extension);
        }
        return detectedType.equals(extension);
    }

    private static String detectType(byte[] bytes) {
        if (bytes.length >= 3
                && (bytes[0] & 0xFF) == 0xFF
                && (bytes[1] & 0xFF) == 0xD8
                && (bytes[2] & 0xFF) == 0xFF) {
            return "jpeg";
        }

        byte[] pngSignature = {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
        if (startsWith(bytes, pngSignature)) {
            return "png";
        }

        if (bytes.length >= 6) {
            String gifHeader = new String(bytes, 0, 6, StandardCharsets.US_ASCII);
            if ("GIF87a".equals(gifHeader) || "GIF89a".equals(gifHeader)) {
                return "gif";
            }
        }

        if (bytes.length >= 16
                && "RIFF".equals(ascii(bytes, 0, 4))
                && "WEBP".equals(ascii(bytes, 8, 4))) {
            return "webp";
        }

        return null;
    }

    private static void validateDecodableImage(byte[] bytes) {
        try (ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
            BufferedImage image = ImageIO.read(input);
            if (image == null || image.getWidth() <= 0 || image.getHeight() <= 0) {
                throw new InvalidUploadFileException("破損した画像ファイルです。");
            }
        } catch (IOException e) {
            throw new InvalidUploadFileException("画像ファイルをデコードできませんでした。", e);
        }
    }

    private static void validateWebp(byte[] bytes) {
        if (bytes.length < 20) {
            throw new InvalidUploadFileException("破損したWebPファイルです。");
        }

        long riffSize = Integer.toUnsignedLong(
                ByteBuffer.wrap(bytes, 4, 4).order(ByteOrder.LITTLE_ENDIAN).getInt());
        if (riffSize + 8L > bytes.length) {
            throw new InvalidUploadFileException("破損したWebPファイルです。");
        }

        String chunkType = ascii(bytes, 12, 4);
        if (!Set.of("VP8 ", "VP8L", "VP8X").contains(chunkType)) {
            throw new InvalidUploadFileException("WebP画像の構造を確認できませんでした。");
        }
    }

    private static boolean startsWith(byte[] source, byte[] prefix) {
        if (source.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (source[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private static String ascii(byte[] bytes, int offset, int length) {
        return new String(bytes, offset, length, StandardCharsets.US_ASCII);
    }
}
