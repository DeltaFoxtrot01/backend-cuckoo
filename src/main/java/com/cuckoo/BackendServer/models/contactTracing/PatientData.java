package com.cuckoo.BackendServer.models.contactTracing;

import com.cuckoo.BackendServer.exceptions.UnknownEncryptionAlgorithmException;
import lombok.Getter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class PatientData {

    @Getter
    private Long seed;

    @Getter
    private Long epoch;

    @Getter
    private Long randomNumber;

    @Getter
    private Long infectedEpoch;

    public PatientData() {}

    public PatientData(Long seed, Long epoch) {
        this.seed = seed;
        this.epoch = epoch;
    }

    public PatientData(Long seed, Long epoch, Long randomNumber, Long infectedEpoch) {
        this(seed, epoch);
        this.randomNumber = randomNumber;
        this.infectedEpoch = infectedEpoch;
    }

    /**
     *  Computes for this patient information the hash that is stored by other patients:
     *      Hash(EphID || epoch) where EphID = Hash(seed)
     */
    public byte[] patientHash() {
        final String ENCRYPTION_ALGORITHM = "SHA3-256";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(ENCRYPTION_ALGORITHM);
            messageDigest.update(this.seed.byteValue());
            byte[] ephID = Arrays.copyOfRange(messageDigest.digest(), 0, 16);

            messageDigest = MessageDigest.getInstance(ENCRYPTION_ALGORITHM);
            messageDigest.update(ephID);
            messageDigest.update(this.epoch.byteValue());
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new UnknownEncryptionAlgorithmException(ENCRYPTION_ALGORITHM);
        }
    }

    /**
     *  Computes for this patient information the hash that is stored by the medic:
     *      Hash(seed, epoch, randomNumber)
     */
    public byte[] medicHash() {
        final String ENCRYPTION_ALGORITHM = "SHA3-256";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(ENCRYPTION_ALGORITHM);
            messageDigest.update(this.seed.byteValue());
            messageDigest.update(this.epoch.byteValue());
            messageDigest.update(this.randomNumber.byteValue());
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new UnknownEncryptionAlgorithmException(ENCRYPTION_ALGORITHM);
        }
    }
}
