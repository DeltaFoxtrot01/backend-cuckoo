package com.cuckoo.BackendServer.exceptions;

public class NegativeEpochException extends ContactTracingException {
    public NegativeEpochException(Long epoch, Long infectedEpoch){
        super("Epoch " + epoch + " and infected epoch" + infectedEpoch + " should have positive values");
    }
}
