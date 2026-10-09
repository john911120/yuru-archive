package com.yuru.archive.util;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.yuru.archive.exception.InvalidUploadFileException;

class FileValidatorTest {

    @Test
    void acceptsValidPng() throws Exception {
        byte[] png = imageBytes("png");
        MockMultipartFile file = new MockMultipartFile("uploadFiles", "ok.png", "image/png", png);
        assertDoesNotThrow(() -> FileValidator.validateImage(file, "ok.png"));
    }

    @Test
    void acceptsValidJpeg() throws Exception {
        byte[] jpg = imageBytes("jpg");
        MockMultipartFile file = new MockMultipartFile("uploadFiles", "ok.jpg", "image/jpeg", jpg);
        assertDoesNotThrow(() -> FileValidator.validateImage(file, "ok.jpg"));
    }

    @Test
    void rejectsTextDisguisedAsPng() {
        MockMultipartFile file = new MockMultipartFile(
                "uploadFiles",
                "fake.png",
                "image/png",
                "not an image".getBytes());
        assertThrows(InvalidUploadFileException.class,
                () -> FileValidator.validateImage(file, "fake.png"));
    }

    @Test
    void rejectsCorruptedJpeg() {
        byte[] corrupted = new byte[] {
                (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
                0x00, 0x10, 'J', 'F', 'I', 'F', 0x00,
                'B', 'R', 'O', 'K', 'E', 'N'
        };
        MockMultipartFile file = new MockMultipartFile(
                "uploadFiles",
                "broken.jpg",
                "image/jpeg",
                corrupted);
        assertThrows(InvalidUploadFileException.class,
                () -> FileValidator.validateImage(file, "broken.jpg"));
    }

    @Test
    void rejectsJpegBytesWithPngExtension() throws Exception {
        byte[] jpg = imageBytes("jpg");
        MockMultipartFile file = new MockMultipartFile(
                "uploadFiles",
                "mismatch.png",
                "image/png",
                jpg);
        assertThrows(InvalidUploadFileException.class,
                () -> FileValidator.validateImage(file, "mismatch.png"));
    }

    @Test
    void rejectsWrongMimeTypeEvenWhenExtensionMatches() throws Exception {
        byte[] jpg = imageBytes("jpg");
        MockMultipartFile file = new MockMultipartFile(
                "uploadFiles",
                "mismatch.jpg",
                "image/png",
                jpg);
        assertThrows(InvalidUploadFileException.class,
                () -> FileValidator.validateImage(file, "mismatch.jpg"));
    }

    private byte[] imageBytes(String format) throws Exception {
        BufferedImage image = new BufferedImage(12, 12, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(image, format, output);
        return output.toByteArray();
    }
}
