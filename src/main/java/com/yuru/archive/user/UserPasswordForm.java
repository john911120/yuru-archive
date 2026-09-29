package com.yuru.archive.user;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPasswordForm {

    @NotEmpty(message = "現在のパスワードは必須項目です。")
    private String currentPassword;

    @NotEmpty(message = "新しいパスワードは必須項目です。")
    @Size(min = 8, max = 100, message = "新しいパスワードは8文字以上100文字以内で入力してください。")
    private String newPassword;

    @NotEmpty(message = "新しいパスワード確認は必須項目です。")
    private String newPasswordConfirm;
}
