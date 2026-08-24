package com.yuru.archive.question;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.yuru.archive.attach.service.AttachService;
import com.yuru.archive.user.SiteUser;

@ExtendWith(MockitoExtension.class)
class QuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AttachService attachService;

    @InjectMocks
    private QuestionService questionService;

    private SiteUser user;

    @BeforeEach
    void setUp() {
        user = new SiteUser();
        user.setId(1L);
        user.setUsername("tester");
        when(questionRepository.save(any(Question.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void createWithAttachmentsUsesSingleAttachServicePath() {
        MultipartFile[] files = {
                new MockMultipartFile("uploadFiles", "sample.png", "image/png", new byte[] {1})
        };

        Question saved = questionService.createWithAttachments("subject", "content", user, files);

        verify(questionRepository).save(saved);
        verify(attachService).uploadFiles(files, saved, user);
    }

    @Test
    void createWithAttachmentsSkipsEmptyFiles() {
        MultipartFile[] files = {
                new MockMultipartFile("uploadFiles", "", "image/png", new byte[0])
        };

        questionService.createWithAttachments("subject", "content", user, files);

        verify(attachService, never()).uploadFiles(any(), any(), any());
    }

    @Test
    void modifyWithAttachmentsDelegatesDeleteAndUploadToAttachService() {
        Question question = new Question();
        question.setAuthor(user);
        MultipartFile[] files = {
                new MockMultipartFile("uploadFiles", "sample.png", "image/png", new byte[] {1})
        };

        questionService.modifyWithAttachments(
                question,
                "changed",
                "changed-content",
                files,
                List.of(10L, 20L),
                user);

        verify(attachService).deleteFileById(10L);
        verify(attachService).deleteFileById(20L);
        verify(attachService).uploadFiles(files, question, user);
    }
}
