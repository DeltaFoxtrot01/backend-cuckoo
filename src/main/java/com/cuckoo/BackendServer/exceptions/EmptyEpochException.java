package com.cuckoo.BackendServer.exceptions;

public class EmptyEpochException extends RuntimeException {
    public EmptyEpochException(){
        super("An epoch should not be null");
    }
}
