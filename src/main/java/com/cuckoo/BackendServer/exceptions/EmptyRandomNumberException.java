package com.cuckoo.BackendServer.exceptions;

public class EmptyRandomNumberException extends ContactTracingException {
    public EmptyRandomNumberException(){
        super("A random number should not be null");
    }
}
