package com.yuru.archive.attach.entity;

import java.time.LocalDateTime;

import com.yuru.archive.question.Question;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "uploaded_file")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadedFile {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	// FK関係で、使うユーザID
	@Column(nullable = false)
	private Long userId;
	
	@Column(nullable = false)
	private String fileName;
	
	private String uuid;
		
	private String folderPath;
	
	@Builder.Default
	private LocalDateTime createdAt = LocalDateTime.now();
	
	// Question entityとの連関関係を追加します。
	// 質問の掲示記事一つに添付ファイルは多数追加されます。
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id")
    private Question question; 
	
}
