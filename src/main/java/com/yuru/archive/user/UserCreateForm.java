package com.yuru.archive.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateForm {
	@Size(min = 3, max = 25)
	@NotEmpty(message = "ユーザーIDは必須項目です。")
	private String username;

	@NotEmpty(message = "パスワードは必須項目です。")
	private String password1;

	@NotEmpty(message = "パスワード確認は、必須項目です。")
	private String password2;

	@NotEmpty(message = "メールアドレスは、必須項目です。")
	@Email
	private String email;
}
