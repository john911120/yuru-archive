package com.yuru.archive.linkpreview.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.yuru.archive.linkpreview.dto.OgDto;
import com.yuru.archive.linkpreview.service.ExternalOgService;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LinkCardController {

    private final ExternalOgService ogService;

    @GetMapping("/cards/preview")
    public String preview(@RequestParam String url, Model model) {
        OgDto dto = ogService.fetch(url);
        model.addAttribute("og", dto);
        return "card-preview";
    }

    @GetMapping("/cards/preview/fragment")
    public String previewFragment(@RequestParam("url") String url, Model model) {
        model.addAttribute("og", ogService.fetch(url));
        return "cards/_card :: linkCard(og=${og})";
    }
}
