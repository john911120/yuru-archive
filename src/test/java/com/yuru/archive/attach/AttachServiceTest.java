package com.yuru.archive.attach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.verify;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import com.yuru.archive.attach.dto.AttachFileDTO;
import com.yuru.archive.attach.repository.AttachFileRepository;
import com.yuru.archive.attach.service.AttachServiceImpl;
import com.yuru.archive.question.Question;
import com.yuru.archive.user.SiteUser;

@ExtendWith(MockitoExtension.class)
class AttachServiceTest {

    @Mock
    private AttachFileRepository attachFileRepository;

    @TempDir
    Path tempDir;

    @Test
    void uploadFilesWithQuestionStoresImageAndMetadata() throws Exception {
        AttachServiceImpl attachService = new AttachServiceImpl(attachFileRepository);
        ReflectionTestUtils.setField(attachService, "uploadPath", tempDir.toString());

        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", outputStream);

        MockMultipartFile mockFile = new MockMultipartFile(
                "uploadFiles",
                "test-image.jpg",
                "image/jpeg",
                outputStream.toByteArray());

        SiteUser user = new SiteUser();
        user.setId(1L);
        user.setUsername("test-user");

        Question question = Question.builder()
                .subject("Test Subject")
                .content("Test Content")
                .author(user)
                .build();

        List<AttachFileDTO> result = attachService.uploadFiles(
                new MultipartFile[] {mockFile},
                question,
                user);

        assertFalse(result.isEmpty());
        assertEquals("test-image.jpg", result.get(0).getFileName());
        verify(attachFileRepository).save(org.mockito.ArgumentMatchers.any());
    }
}
