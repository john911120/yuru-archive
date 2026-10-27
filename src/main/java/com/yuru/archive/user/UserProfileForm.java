package com.yuru.archive.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserProfileForm {

    @NotEmpty(message = "メールアドレスは必須項目です。")
    @Email(message = "メールアドレスの形式を確認してください。")
    private String email;

    @Pattern(regexp = "^$|\\d{3}-?\\d{4}", message = "郵便番号は7桁の数字、またはハイフン付きで入力してください。")
    private String zipcode;

    @Size(max = 50)
    private String address1;

    @Size(max = 50)
    private String address2;

    @Size(max = 50)
    private String address3;

    @Size(max = 255)
    private String addressDetail;
}
