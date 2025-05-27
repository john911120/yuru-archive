package com.yuru.archive.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)  // 409
public class DuplicateVoteException extends RuntimeException {
	
    public DuplicateVoteException(String message) {
        super(message);
    }
}
