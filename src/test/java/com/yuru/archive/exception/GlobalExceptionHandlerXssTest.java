package com.yuru.archive.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

/*
 * 2026-11-03 追加: 例外メッセージ経由の反射型XSS対策の確認
 */
class GlobalExceptionHandlerXssTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void badRequestDoesNotEchoUserInput() {
        var response = handler.handleBadRequest(
                new IllegalArgumentException("For input string: \"<img src=x onerror=alert(1)>\""));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertFalse(response.getBody().contains("<img"));
        assertEquals(GlobalExceptionHandler.BAD_REQUEST_MESSAGE, response.getBody());
    }

    @Test
    void responsesAreAlwaysPlainText() {
        var response = handler.handleBadRequest(new IllegalArgumentException("x"));
        MediaType contentType = response.getHeaders().getContentType();

        assertEquals(MediaType.TEXT_PLAIN.getType(), contentType.getType());
        assertEquals(MediaType.TEXT_PLAIN.getSubtype(), contentType.getSubtype());
    }
}
