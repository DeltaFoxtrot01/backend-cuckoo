package com.cuckoo.BackendServer.exceptions;

public class EmptyRandomNumberException extends RuntimeException {
    public EmptyRandomNumberException(){
        super("A random number should not be null");
    }
}
