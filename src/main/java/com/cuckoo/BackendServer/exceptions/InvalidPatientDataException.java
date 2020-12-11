package com.cuckoo.BackendServer.exceptions;

public class InvalidPatientDataException extends ContactTracingException {
    public InvalidPatientDataException() {
        super("Data provided by patient is not backed the any medic");
    }
}
