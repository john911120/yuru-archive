package com.yuru.archive.attach.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.yuru.archive.attach.dto.AttachFileDTO;
import com.yuru.archive.question.Question;

public interface AttachService {
	
	List<AttachFileDTO> uploadFiles(MultipartFile[] uploadFiles);
	boolean deleteFile(String fileName);
	
	// uploadPathを照会できるように追加しました。
	String getUploadPath();
	List<AttachFileDTO> uploadFiles(MultipartFile[] uploadFiles, Question question);

}
