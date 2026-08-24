package com.yuru.archive;

import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

@Component
public class CommonUtil {

    private final Parser parser = Parser.builder().build();
    private final HtmlRenderer renderer = HtmlRenderer.builder().build();

    public String markdown(String markdown) {
        if (markdown == null) {
            return "";
        }

        Node document = parser.parse(markdown);
        String html = renderer.render(document);
        return Jsoup.clean(html, Safelist.basicWithImages());
    }
}
