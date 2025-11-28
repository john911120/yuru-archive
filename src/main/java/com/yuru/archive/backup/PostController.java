package com.yuru.archive.backup;

import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PostController {
	/*
	 * ※現在は使われておりません。
	 * ただ、リンクカード機能の掲示板、コメント、Answer等々にも拡張できる際に、参考用として保存させていただきます。
	 * PostController（旧リンクカード例）
	 * 
	 * shortCode → HTML 変換ロジック
	 * TemplateEngineの使い方の例え
	 * Matcher Regex pattern
	 * 
	 * 必要な際には、いつでも復元可能。
	 */
	/*
	// for Short code replacement and detail page rendering
	
	private final ExternalOgService ogService;
	private final TemplateEngine templateEngine;
	private final QuestionService questionService;
	
	private static final Pattern LINKCARD = Pattern.compile("\\[\\[linkcard\\s+url=\"([^\"]+)\"\\s*]]");
	
	@GetMapping("/posts/{id}")
	public String detail(@PathVariable Long id, Model model) {
		Question question = questionService.getQuestion(id);
		if(question == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		String content = question.getContent();
		String htmlBody = expandLinkCards(content);
		model.addAttribute("question",question);
		model.addAttribute("htmlBody", htmlBody);
		
		return "posts/detail";
	}
	
	private String expandLinkCards(String content) {
		if(content == null || content.isBlank()) return content;
		
		Matcher m = LINKCARD.matcher(content);
		StringBuffer sb = new StringBuffer();
		
		while(m.find()) {
			String url = m.group(1);
			// cache/検証/Cloudinary JPG 強制を含む
			OgDto og = ogService.fetch(url);
			
			Context ctx = new Context();
			ctx.setVariable("og", og);
			
			// thymeleaf fragment → html 文字列で、レンダリングする。
			String html = templateEngine.process("cards/_card :: linkCard(og=${og})", ctx);
			
			m.appendReplacement(sb, Matcher.quoteReplacement(html));
		}
		m.appendTail(sb);
		return sb.toString();
	}
	*
	*/
}
