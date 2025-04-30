package com.yuru.archive;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class MainController {

	@GetMapping("/sbb")
	@ResponseBody
	public String index() {
		return "こんにちは！ ゆるアーカイブへようこそ。";
	}

	@GetMapping("/")
	public String root() {
		return "redirect:/question/list";
	}
}
