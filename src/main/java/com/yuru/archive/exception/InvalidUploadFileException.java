package com.yuru.archive.exception;

public class InvalidUploadFileException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public InvalidUploadFileException(String message) {
        super(message);
    }

    public InvalidUploadFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
