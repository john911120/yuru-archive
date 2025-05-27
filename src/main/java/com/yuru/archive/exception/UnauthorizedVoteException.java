package com.yuru.archive.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNAUTHORIZED)  // 401
public class UnauthorizedVoteException extends RuntimeException {
	
    public UnauthorizedVoteException(String message) {
        super(message);
    }
}
