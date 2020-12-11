package com.cuckoo.BackendServer.exceptions;

public class EmptySeedException  extends ContactTracingException {
    public EmptySeedException(){
        super("A seed should not be null");
    }
}
