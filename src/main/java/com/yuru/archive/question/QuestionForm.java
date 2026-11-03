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
	// 2026-11-03: 閲覧毎にMarkdown変換を行うため、本文の長さに上限を設ける
	@Size(max = 20000, message = "本文は20000文字以内で入力してください。")
	private String content;
}
