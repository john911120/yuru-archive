package com.yuru.archive.user;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpServletRequest;

import java.security.Principal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Controller
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final UserSessionService userSessionService;

    @GetMapping("/")
    public String mainPage() {

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


    @GetMapping("/profile")
    public String profile(Principal principal, Model model) {
        SiteUser user = userService.getUser(principal.getName());
        model.addAttribute("siteUser", user);
        return "profile";
    }

    @GetMapping("/profile/edit")
    public String editProfile(Principal principal, Model model) {
        SiteUser user = userService.getUser(principal.getName());
        model.addAttribute("siteUser", user);
        model.addAttribute("userProfileForm", userService.toProfileForm(user));
        return "profile_edit";
    }

    @PostMapping("/profile/edit")
    public String editProfile(
            Principal principal,
            @Valid UserProfileForm userProfileForm,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        SiteUser currentUser = userService.getUser(principal.getName());
        if (bindingResult.hasErrors()) {
            model.addAttribute("siteUser", currentUser);
            return "profile_edit";
        }

        try {
            userService.updateProfile(principal.getName(), userProfileForm);
        } catch (DataIntegrityViolationException e) {
            log.warn("会員情報の更新に失敗しました。メールアドレス重複の可能性があります: {}", userProfileForm.getEmail());
            bindingResult.rejectValue("email", "duplicateEmail", "既に使用されているメールアドレスです。");
            model.addAttribute("siteUser", currentUser);
            return "profile_edit";
        }

        redirectAttributes.addFlashAttribute("profileUpdated", true);
        return "redirect:/user/profile";
    }

    @GetMapping("/password")
    public String changePassword(UserPasswordForm userPasswordForm) {
        return "password_change";
    }

    @PostMapping("/password")
    public String changePassword(
            Principal principal,
            @Valid UserPasswordForm userPasswordForm,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        if (bindingResult.hasErrors()) {
            return "password_change";
        }

        if (!userPasswordForm.getNewPassword().equals(userPasswordForm.getNewPasswordConfirm())) {
            bindingResult.rejectValue(
                    "newPasswordConfirm",
                    "passwordInCorrect",
                    "新しいパスワードが一致しません。");
            return "password_change";
        }

        if (userPasswordForm.getCurrentPassword().equals(userPasswordForm.getNewPassword())) {
            bindingResult.rejectValue(
                    "newPassword",
                    "passwordUnchanged",
                    "現在のパスワードとは異なるパスワードを入力してください。");
            return "password_change";
        }

        boolean changed = userService.changePassword(
                principal.getName(),
                userPasswordForm.getCurrentPassword(),
                userPasswordForm.getNewPassword());

        if (!changed) {
            bindingResult.rejectValue(
                    "currentPassword",
                    "passwordMismatch",
                    "現在のパスワードが正しくありません。");
            return "password_change";
        }

        String currentSessionId = request.getSession(false) != null
                ? request.getSession(false).getId()
                : null;
        userSessionService.expireOtherSessions(principal.getName(), currentSessionId);

        redirectAttributes.addFlashAttribute("passwordChanged", true);
        return "redirect:/user/profile";
    }

    @GetMapping("/login")
    public String login() {
        return "login_form";
    }
}
