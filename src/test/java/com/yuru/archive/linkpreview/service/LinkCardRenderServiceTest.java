package com.yuru.archive.linkpreview.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.yuru.archive.CommonUtil;
import com.yuru.archive.linkpreview.dto.OgDto;

class LinkCardRenderServiceTest {

    @Test
    void rawHtmlIsSanitizedBeforeRendering() {
        ExternalOgService ogService = mock(ExternalOgService.class);
        TemplateEngine templateEngine = mock(TemplateEngine.class);
        LinkCardRenderService service = new LinkCardRenderService(
                ogService,
                templateEngine,
                new CommonUtil());

        String html = service.render("Hello <script>alert('xss')</script><img src=\"x\" onerror=\"alert(1)\">");

        assertFalse(html.toLowerCase().contains("<script"));
        assertFalse(html.toLowerCase().contains("onerror"));
        assertTrue(html.contains("Hello"));
    }

    @Test
    void sanitizedMarkdownAndTrustedLinkCardCanBeCombined() {
        ExternalOgService ogService = mock(ExternalOgService.class);
        TemplateEngine templateEngine = mock(TemplateEngine.class);
        LinkCardRenderService service = new LinkCardRenderService(
                ogService,
                templateEngine,
                new CommonUtil());

        OgDto og = new OgDto("https://example.com", "Example", "", null);
        when(ogService.fetch("https://example.com")).thenReturn(og);
        when(templateEngine.process(eq("card"), any(Context.class)))
                .thenReturn("<div class=\"link-card\">safe-card</div>");

        String html = service.render("before <b>text</b> [[linkcard url=\"https://example.com\"]] after");

        assertTrue(html.contains("safe-card"));
        assertTrue(html.contains("before"));
        assertTrue(html.contains("after"));
    }

    @Test
    void unsafeLinkCardUrlDoesNotBecomeExecutableHtml() {
        ExternalOgService ogService = mock(ExternalOgService.class);
        TemplateEngine templateEngine = mock(TemplateEngine.class);
        LinkCardRenderService service = new LinkCardRenderService(
                ogService,
                templateEngine,
                new CommonUtil());

        when(ogService.fetch("javascript:alert(1)"))
                .thenThrow(new IllegalArgumentException("Only http/https allowed"));

        String html = service.render("[[linkcard url=\"javascript:alert(1)\"]]");

        assertFalse(html.toLowerCase().contains("javascript:"));
        assertTrue(html.contains("無効なリンクカードURL"));
    }
}
