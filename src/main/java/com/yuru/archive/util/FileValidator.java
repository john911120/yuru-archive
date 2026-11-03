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

import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

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

    /*
     * 2026-11-03: Decompression bomb対策。
     * ImageIO.read() は画像全体を展開するため、ファイルサイズが小さくても
     * 巨大な幅・高さを宣言した画像でメモリを使い切る可能性がある。
     * デコード前に ImageReader でヘッダーの幅・高さだけを読み取り、上限を超える画像は拒否する。
     */
    static final int MAX_IMAGE_SIDE = 8000;
    static final long MAX_IMAGE_PIXELS = 40_000_000L;

    private static void validateDimensions(byte[] bytes) {
        try (ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes))) {
            if (input == null) {
                throw new InvalidUploadFileException("画像ファイルを読み込めませんでした。");
            }
            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                throw new InvalidUploadFileException("画像ファイルをデコードできませんでした。");
            }
            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, true);
                checkDimensions(reader.getWidth(0), reader.getHeight(0));
            } finally {
                reader.dispose();
            }
        } catch (IOException | RuntimeException e) {
            if (e instanceof InvalidUploadFileException invalid) {
                throw invalid;
            }
            throw new InvalidUploadFileException("画像ファイルをデコードできませんでした。", e);
        }
    }

    private static void checkDimensions(long width, long height) {
        if (width <= 0 || height <= 0
                || width > MAX_IMAGE_SIDE
                || height > MAX_IMAGE_SIDE
                || width * height > MAX_IMAGE_PIXELS) {
            throw new InvalidUploadFileException(
                    "画像サイズが大きすぎます。縦横" + MAX_IMAGE_SIDE + "px以下の画像を使用してください。");
        }
    }

    private static void validateDecodableImage(byte[] bytes) {
        validateDimensions(bytes);
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

        // 2026-11-03: WebPもヘッダーの幅・高さを確認する（Decompression bomb対策）
        if (bytes.length < 30) {
            throw new InvalidUploadFileException("破損したWebPファイルです。");
        }
        long width;
        long height;
        switch (chunkType) {
            case "VP8X" -> {
                width = 1 + ((bytes[24] & 0xFF) | (bytes[25] & 0xFF) << 8 | (bytes[26] & 0xFF) << 16);
                height = 1 + ((bytes[27] & 0xFF) | (bytes[28] & 0xFF) << 8 | (bytes[29] & 0xFF) << 16);
            }
            case "VP8L" -> {
                if ((bytes[20] & 0xFF) != 0x2F) {
                    throw new InvalidUploadFileException("WebP画像の構造を確認できませんでした。");
                }
                long bits = (bytes[21] & 0xFFL) | (bytes[22] & 0xFFL) << 8
                        | (bytes[23] & 0xFFL) << 16 | (bytes[24] & 0xFFL) << 24;
                width = 1 + (bits & 0x3FFF);
                height = 1 + ((bits >> 14) & 0x3FFF);
            }
            default -> { // "VP8 "（非可逆圧縮形式）
                if ((bytes[23] & 0xFF) != 0x9D || (bytes[24] & 0xFF) != 0x01 || (bytes[25] & 0xFF) != 0x2A) {
                    throw new InvalidUploadFileException("WebP画像の構造を確認できませんでした。");
                }
                width = ((bytes[26] & 0xFF) | (bytes[27] & 0xFF) << 8) & 0x3FFF;
                height = ((bytes[28] & 0xFF) | (bytes[29] & 0xFF) << 8) & 0x3FFF;
            }
        }
        checkDimensions(width, height);
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
