package com.yuru.archive.attach.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.yuru.archive.attach.entity.UploadedFile;

public interface AttachFileRepository extends JpaRepository<UploadedFile, Long> {
	
	// 特定のユーザを基準にファイルを照会します。
	List<UploadedFile> findByUserId(Long userId);
	
	// 特定の質問の掲示記事に属するファイルのリスト
	List<UploadedFile> findByQuestion_Id(Long questionId);
	
	// 特定の質問の掲示記事に属するファイルを全部削除するときのロジックです。
	void deleteByQuestion_Id(Long questionId);

	List<UploadedFile> findByQuestionId(Long questionId);
}
