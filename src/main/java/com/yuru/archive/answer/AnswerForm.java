package com.yuru.archive.answer;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AnswerForm {
	@NotEmpty(message = "コンテンツは必須項目です。")
	// 2026-11-03: 閲覧毎にMarkdown変換を行うため、本文の長さに上限を設ける
	@Size(max = 10000, message = "コメントは10000文字以内で入力してください。")
	private String content;
}
