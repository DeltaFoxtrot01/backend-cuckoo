package com.cuckoo.BackendServer.models.contactTracing;

import com.cuckoo.BackendServer.exceptions.UnknownEncryptionAlgorithmException;
import lombok.Getter;

import java.nio.ByteBuffer;
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
     *  Computes for this patient information the EphID:
     *      EphID = Hash(seed)
     */
    public byte[] ephID() {
        final String ENCRYPTION_ALGORITHM = "SHA-256";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(ENCRYPTION_ALGORITHM);
            byte[] seedBytes = ByteBuffer.allocate(8).putLong(this.seed).array();

            messageDigest.update(seedBytes);
            return Arrays.copyOfRange(messageDigest.digest(), 0, 16);
        } catch (NoSuchAlgorithmException e) {
            throw new UnknownEncryptionAlgorithmException(ENCRYPTION_ALGORITHM);
        }
    }

    /**
     *  Computes for this patient information the hash that is stored by other patients:
     *      Hash(EphID || epoch)
     */
    public byte[] patientHash() {
        final String ENCRYPTION_ALGORITHM = "SHA-256";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(ENCRYPTION_ALGORITHM);
            byte[] ephID = ephID();
            byte[] epochBytes = ByteBuffer.allocate(8).putLong(this.epoch).array();

            messageDigest.update(ephID);
            messageDigest.update(epochBytes);
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
        final String ENCRYPTION_ALGORITHM = "SHA-256";
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(ENCRYPTION_ALGORITHM);
            byte[] seedBytes = ByteBuffer.allocate(8).putLong(this.seed).array();
            byte[] epochBytes = ByteBuffer.allocate(8).putLong(this.epoch).array();
            byte[] randomNumberBytes = ByteBuffer.allocate(8).putLong(this.randomNumber).array();

            messageDigest.update(seedBytes);
            messageDigest.update(epochBytes);
            messageDigest.update(randomNumberBytes);
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new UnknownEncryptionAlgorithmException(ENCRYPTION_ALGORITHM);
        }
    }
}
