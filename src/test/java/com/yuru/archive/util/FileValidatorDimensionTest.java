package com.yuru.archive.util;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.zip.CRC32;
import java.util.zip.Deflater;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.yuru.archive.exception.InvalidUploadFileException;

/*
 * 2026-11-03 追加: Decompression bomb（巨大な幅・高さを宣言した小さな画像）の拒否を確認
 */
class FileValidatorDimensionTest {

    @Test
    void rejectsPngDeclaringHugeDimensions() throws Exception {
        byte[] png = pngHeaderOnly(30_000, 30_000);
        MockMultipartFile file = new MockMultipartFile("uploadFiles", "bomb.png", "image/png", png);

        assertThrows(InvalidUploadFileException.class,
                () -> FileValidator.validateImage(file, "bomb.png"));
    }

    /** IHDRで指定サイズを宣言し、IDATは極小のPNGを生成する（全体で数十バイト）。 */
    private static byte[] pngHeaderOnly(int width, int height) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream data = new DataOutputStream(out);
        data.write(new byte[] {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A});

        ByteArrayOutputStream ihdr = new ByteArrayOutputStream();
        DataOutputStream ihdrData = new DataOutputStream(ihdr);
        ihdrData.writeInt(width);
        ihdrData.writeInt(height);
        ihdrData.writeByte(8); // ビット深度
        ihdrData.writeByte(2); // カラータイプ（RGB）
        ihdrData.writeByte(0); // 圧縮方式
        ihdrData.writeByte(0); // フィルター方式
        ihdrData.writeByte(0); // インターレースなし
        writeChunk(data, "IHDR", ihdr.toByteArray());

        Deflater deflater = new Deflater();
        deflater.setInput(new byte[] {0});
        deflater.finish();
        byte[] buffer = new byte[64];
        int length = deflater.deflate(buffer);
        byte[] idat = new byte[length];
        System.arraycopy(buffer, 0, idat, 0, length);
        writeChunk(data, "IDAT", idat);
        writeChunk(data, "IEND", new byte[0]);
        return out.toByteArray();
    }

    private static void writeChunk(DataOutputStream out, String type, byte[] body) throws IOException {
        byte[] typeBytes = type.getBytes(StandardCharsets.US_ASCII);
        out.writeInt(body.length);
        out.write(typeBytes);
        out.write(body);
        CRC32 crc = new CRC32();
        crc.update(typeBytes);
        crc.update(body);
        out.writeInt((int) crc.getValue());
    }
}
