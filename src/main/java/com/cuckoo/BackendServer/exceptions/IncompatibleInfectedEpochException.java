package com.cuckoo.BackendServer.exceptions;

public class IncompatibleInfectedEpochException extends ContactTracingException {
    public IncompatibleInfectedEpochException() {
        super("Different infected epochs from same patient");
    }
}
