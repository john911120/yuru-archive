package com.yuru.archive.question;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionForm {
	
	// 修正用のIDField追加
	private Long id;
	
	@NotEmpty(message = "タイトルは必須項目です。")
	@Size(max = 200)
	private String subject;

	@NotEmpty(message = "コンテンツは必須項目です。")
	private String content;
}
