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
	// DB無しにURLだけを受信し、カードをレンダリングする。
	private final ExternalOgService og;
	
	// 全体ページ(for debugger)
	@GetMapping("/cards/preview")
	public String preview(@RequestParam String url, Model model) {
		OgDto dto = og.fetch(url);
		model.addAttribute("og", dto);
		return "card-preview";
	}
	
	// fragment only return(form from fetch replace)
	// 登録と修正の画面では、Ajax(fetch)で、フラグメントだけを受信するためのコントローラーです。
	@GetMapping("/cards/preview/fragment")
	public String previewFragment(@RequestParam("url") String url, Model model) {
		model.addAttribute("og", og.fetch(url));
		 return "cards/_card :: linkCard(og=${og})";
	}
}
