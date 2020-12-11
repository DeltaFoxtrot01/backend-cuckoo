package com.cuckoo.BackendServer.exceptions;

public class EmptyEpochException extends ContactTracingException {
    public EmptyEpochException(){
        super("An epoch should not be null");
    }
}
