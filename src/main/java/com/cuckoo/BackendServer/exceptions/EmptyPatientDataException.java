package com.cuckoo.BackendServer.exceptions;

public class EmptyPatientDataException extends ContactTracingException {
    public EmptyPatientDataException() {
        super("The data supplied by the patient cannot be empty");
    }
}
