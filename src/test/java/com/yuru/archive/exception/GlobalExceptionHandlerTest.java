package com.yuru.archive.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import com.yuru.archive.DataNotFoundException;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void methodNotAllowedReturns405() {
        var response = handler.handleMethodNotAllowed(
                new HttpRequestMethodNotSupportedException("GET"));
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
    }

    @Test
    void accessDeniedReturns403() {
        var response = handler.handleAccessDenied(new AccessDeniedException("denied"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void dataNotFoundReturns404() {
        var response = handler.handleNotFound(new DataNotFoundException("missing"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void badRequestReturns400() {
        var response = handler.handleBadRequest(new IllegalArgumentException("bad"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
