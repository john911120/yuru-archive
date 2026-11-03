package com.yuru.archive.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserCreateForm {
	@Size(min = 3, max = 25)
	@NotEmpty(message = "ユーザーIDは必須項目です。")
	private String username;

	// 2026-11-03: パスワード変更画面（8〜100文字）と同じ長さ制限を会員登録にも適用
	@NotEmpty(message = "パスワードは必須項目です。")
	@Size(min = 8, max = 100, message = "パスワードは8文字以上100文字以内で入力してください。")
	private String password1;

	@NotEmpty(message = "パスワード確認は、必須項目です。")
	private String password2;

	@NotEmpty(message = "メールアドレスは、必須項目です。")
	@Email
	private String email;
	
    // 🏠 住所関連
	// 郵便番号
	@Pattern(regexp = "\\d{3}-?\\d{4}", message = "郵便番号は7桁の数字、またはハイフン付きで入力してください。")
	private String zipcode;
	
	// 都道府県
	private String address1;
	
	// 市区町村
	private String address2;
	
	// 町名
	private String address3;
	
    // 詳細住所情報
	@Size(max = 255)
	private String addressDetail;
	
}
