package com.yuru.archive.user;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.yuru.archive.util.GreetingUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    @GetMapping("/")
    public String mainPage(@AuthenticationPrincipal SiteUser user, Model model) {
        if (user != null) {
            model.addAttribute("username", user.getUsername());
            model.addAttribute("greeting", GreetingUtil.getGreetingMessage());
        }
        return "main";
    }

    @GetMapping("/signup")
    public String signup(UserCreateForm userCreateForm) {
        return "signup_form";
    }

    @PostMapping("/signup")
    public String signup(@Valid UserCreateForm userCreateForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "signup_form";
        }

        if (!userCreateForm.getPassword1().equals(userCreateForm.getPassword2())) {
            bindingResult.rejectValue("password2", "passwordInCorrect", "二つのパスワードが一致しません。");
            return "signup_form";
        }

        try {
            userService.create(
                    userCreateForm.getUsername(),
                    userCreateForm.getEmail(),
                    userCreateForm.getPassword1(),
                    userCreateForm.getZipcode(),
                    userCreateForm.getAddress1(),
                    userCreateForm.getAddress2(),
                    userCreateForm.getAddress3(),
                    userCreateForm.getAddressDetail());
        } catch (DataIntegrityViolationException e) {
            log.warn("ユーザ登録に失敗しました。既存ユーザの可能性があります: {}", userCreateForm.getUsername());
            bindingResult.reject("signupFailed", "既に登録されたユーザです。");
            return "signup_form";
        } catch (Exception e) {
            log.error("ユーザ登録中にエラーが発生しました: {}", userCreateForm.getUsername(), e);
            bindingResult.reject("signupFailed", "ユーザ登録に失敗しました。");
            return "signup_form";
        }

        return "redirect:/";
    }

    @GetMapping("/login")
    public String login() {
        return "login_form";
    }
}
