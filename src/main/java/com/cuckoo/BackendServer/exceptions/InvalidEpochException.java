package com.cuckoo.BackendServer.exceptions;

public class InvalidEpochException extends RuntimeException {
    public InvalidEpochException(Long epoch, Long infectedEpoch){
        super("Epoch " + epoch + " should be a positive value and should not be lower than infectedEpoch " + infectedEpoch);
    }
}
