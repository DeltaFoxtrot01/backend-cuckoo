package com.cuckoo.BackendServer.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ContactTracingException extends RuntimeException {

    @Getter
    private final String errorMessage;

    public ContactTracingException(String errorMessage) {
        super(errorMessage);
        this.errorMessage = errorMessage;
    }

}
