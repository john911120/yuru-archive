package com.yuru.archive.attach;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import com.yuru.archive.attach.dto.AttachFileDTO;
import com.yuru.archive.attach.service.AttachService;
import com.yuru.archive.question.Question;
import com.yuru.archive.question.QuestionRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class AttachServiceTest {

	@Autowired
	private AttachService attachService;
	
	@MockBean
	private QuestionRepository questionRepository;
	
	@Test
	public void testUploadFilesWithQuestion() {
		MockMultipartFile mockFile = new MockMultipartFile(
				"uploadFiles", "test-image.jpg", "image/jpeg", "dummy-image-content".getBytes());
		
        Question dummyQuestion = Question.builder()
                .subject("Test Subject")
                .content("Test Content")
                .build();
        
        given(questionRepository.save(any(Question.class))).willReturn(dummyQuestion);

        List<AttachFileDTO> result = attachService.uploadFiles(new MultipartFile[]{mockFile}, dummyQuestion);

        assertFalse(result.isEmpty());
        assertEquals("test-image.jpg", result.get(0).getFileName());
    }
	
}
