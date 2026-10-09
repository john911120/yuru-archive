package com.yuru.archive.linkpreview.service;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.yuru.archive.CommonUtil;
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
    private final CommonUtil commonUtil;

    /**
     * 本文を安全なHTMLへ変換し、[[linkcard url="..."]] の部分だけを
     * サーバー側で生成したリンクカードHTMLへ置き換えます。
     *
     * ユーザー入力本文は必ず CommonMark + jsoup のサニタイズを通すため、
     * 生の本文が th:utext へ渡ることはありません。
     */
    public String render(String content) {
        if (content == null || content.isBlank()) {
            return "";
        }

        Matcher matcher = LINKCARD_PATTERN.matcher(content);
        StringBuilder result = new StringBuilder();
        int cursor = 0;

        while (matcher.find()) {
            appendSanitizedMarkdown(result, content.substring(cursor, matcher.start()));
            result.append(renderCard(matcher.group(1)));
            cursor = matcher.end();
        }

        appendSanitizedMarkdown(result, content.substring(cursor));
        return result.toString();
    }

    private void appendSanitizedMarkdown(StringBuilder result, String markdown) {
        if (markdown != null && !markdown.isEmpty()) {
            result.append(commonUtil.markdown(markdown));
        }
    }

    private String renderCard(String url) {
        try {
            OgDto og = ogService.fetch(url);
            Context context = new Context();
            context.setVariable("og", og);
            return templateEngine.process("card", context);
        } catch (IllegalArgumentException e) {
            log.warn("安全ではないリンクカードURLを拒否しました: {}", url);
            return "<p class=\"text-muted\">無効なリンクカードURLが指定されました。</p>";
        } catch (Exception e) {
            log.debug("リンクカード生成に失敗しました: {}", url, e);
            return "<p class=\"text-muted\">リンクカードを表示できませんでした。</p>";
        }
    }
}
