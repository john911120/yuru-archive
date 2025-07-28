package com.yuru.archive;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

@Component
public class CommonUtil {
	public String markdown(String markdown) {
		
		if(markdown == null ) return "";
		
		// Convert to HTML
		Parser parser = Parser.builder().build();
		Node document = parser.parse(markdown);
		HtmlRenderer renderer = HtmlRenderer.builder().build();
		String html = renderer.render(document);
		
		// Prevent the XSS for HTML Filltering.
		return Jsoup.clean(html, Safelist.basicWithImages());
		
		//return renderer.render(document);
	}
}
