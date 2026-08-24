package com.yuru.archive.linkpreview.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.yuru.archive.linkpreview.dto.OgDto;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class LinkCardRenderService {

    private static final Pattern LINKCARD_PATTERN =
            Pattern.compile("\\[\\[linkcard\\s+url=\"([^\"]+)\"\\s*]]");

    private final ExternalOgService ogService;
    private final TemplateEngine templateEngine;

    /**
     * 本文中の [[linkcard url="..."]] をリンクカードHTMLへ変換します。
     * 外部APIに失敗した場合は通常リンクへフォールバックします。
     */
    public String render(String content) {
        if (content == null || content.isBlank()) {
            return content;
        }

        Matcher matcher = LINKCARD_PATTERN.matcher(content);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String url = matcher.group(1);
            String replacement = renderCardOrFallback(url);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private String renderCardOrFallback(String url) {
        try {
            OgDto og = ogService.fetch(url);
            Context context = new Context();
            context.setVariable("og", og);
            return templateEngine.process("card", context);
        } catch (Exception e) {
            log.debug("リンクカード生成に失敗したため通常リンクへフォールバックします: {}", url, e);
            return "<a href=\"" + url + "\" target=\"_blank\" rel=\"noopener noreferrer\">" + url + "</a>";
        }
    }
}
