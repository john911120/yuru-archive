package com.yuru.archive.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)  // 409
public class DuplicateVoteException extends RuntimeException {
	
	private static final long serialVersionUID = 1L;
	
    public DuplicateVoteException(String message) {
        super(message);
    }
}
