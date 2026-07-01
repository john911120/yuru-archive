package com.yuru.archive.attach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.multipart.MultipartFile;

import com.yuru.archive.attach.dto.AttachFileDTO;
import com.yuru.archive.attach.repository.AttachFileRepository;
import com.yuru.archive.attach.service.AttachService;
import com.yuru.archive.question.Question;
import com.yuru.archive.user.SiteUser;

@SpringBootTest
@AutoConfigureMockMvc
public class AttachServiceTest {

	@Autowired
	private AttachService attachService;
	
	@MockitoBean
	private AttachFileRepository attachFileRepository;
	
	@Test
	public void testUploadFilesWithQuestion() throws IOException {
		
		BufferedImage image = new BufferedImage(10,10, BufferedImage.TYPE_INT_RGB);
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

			ImageIO.write(image, "jpg", outputStream);

			MockMultipartFile mockFile = new MockMultipartFile(
					"uploadFiles", "test-image.jpg", "image/jpeg", outputStream.toByteArray());
			
			SiteUser dummyUser = new SiteUser();
			dummyUser.setId(1L);
			dummyUser.setUsername("test-user");
			
	        Question dummyQuestion = Question.builder()
	                .subject("Test Subject")
	                .content("Test Content")
	                .author(dummyUser)
	                .build();
			
	        List<AttachFileDTO> result = attachService.uploadFiles(new MultipartFile[]{mockFile}, dummyQuestion);
	        
	        assertFalse(result.isEmpty());
	        assertEquals("test-image.jpg", result.get(0).getFileName());
    }
	
}
