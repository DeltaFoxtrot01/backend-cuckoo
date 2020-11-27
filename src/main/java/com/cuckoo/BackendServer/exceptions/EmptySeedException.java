package com.cuckoo.BackendServer.exceptions;

public class EmptySeedException  extends RuntimeException {
    public EmptySeedException(){
        super("A seed should not be null");
    }
}
