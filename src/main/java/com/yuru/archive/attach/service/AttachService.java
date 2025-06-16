package com.yuru.archive.attach.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.yuru.archive.attach.dto.AttachFileDTO;
import com.yuru.archive.question.Question;
import com.yuru.archive.user.SiteUser;

public interface AttachService {
	
	boolean deleteFile(String fileName);
	
	// uploadPathを照会できるように追加しました。
	String getUploadPath();
	
	// 添付ファイルを処理するためのメソッドを作成しました。
	void uploadFilesFromDTOs(List<AttachFileDTO> attachFileList, Question saved);

	// 一番シンプルなバージョン
	List<AttachFileDTO> uploadFiles(MultipartFile[] uploadFiles);
	
	// 質問オブジェクトを連結するバージョン
	List<AttachFileDTO> uploadFiles(MultipartFile[] uploadFiles, Question question);
	
	// 質問とユーザまで連結する最終バージョン
	List<AttachFileDTO> uploadFiles(MultipartFile[] uploadFiles, Question question, SiteUser user);

}
