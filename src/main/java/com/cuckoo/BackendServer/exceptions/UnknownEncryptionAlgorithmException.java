package com.cuckoo.BackendServer.exceptions;

import lombok.Getter;

public class UnknownEncryptionAlgorithmException extends RuntimeException {
    @Getter
    private final String algorithm;

    public UnknownEncryptionAlgorithmException(String algorithm) {
        super("Wrong or outdated algorithm " + algorithm);
        this.algorithm = algorithm;
    }
}
