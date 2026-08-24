package com.yuru.archive.common;

import java.security.Principal;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.yuru.archive.util.GreetingUtil;

@ControllerAdvice
public class GlobalModelAttributeAdvice {

    @ModelAttribute
    public void setGreeting(Model model, Principal principal) {
        if (principal != null) {
            model.addAttribute("username", principal.getName());
            model.addAttribute("greeting", GreetingUtil.getGreetingMessage());
        }
    }
}
