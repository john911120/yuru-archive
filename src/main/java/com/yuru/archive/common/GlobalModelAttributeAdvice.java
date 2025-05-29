package com.yuru.archive.common;

import java.security.Principal;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.yuru.archive.util.GreetingUtil;

/**
 * 🌐 全てのコントローラーに共通して適用されるモデル属性を設定するクラスです。
 * 
 * 現在ログイン中のユーザー名と、時間帯に応じた挨拶メッセージを
 * すべてのテンプレートで利用可能にします。
 */
@ControllerAdvice
public class GlobalModelAttributeAdvice {
	
    /**
     * 🧭 各リクエストの処理前に、共通のModel属性（ユーザー名・挨拶）を設定します。
     *
     * @param model Thymeleafなどのテンプレートエンジンで使用するモデル
     * @param principal ログインユーザーの認証情報（nullの場合は未ログイン）
     */
	@ModelAttribute
    public void setGreeting(Model model, Principal principal) {
        if (principal != null) {
            String username = principal.getName();
            model.addAttribute("username", username);
            model.addAttribute("greeting", GreetingUtil.getGreetingMessage());
        } else {
            System.out.println("❌ global model setting: login user is noned! (principal is null)");
        }
    }
}
