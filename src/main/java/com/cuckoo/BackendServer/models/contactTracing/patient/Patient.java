package com.cuckoo.BackendServer.models.contactTracing.patient;

import com.cuckoo.BackendServer.exceptions.*;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class Patient {

    private final String ENCRYPTION_ALGORITHM = "SHA-256";

    @Getter
    private final Long seed;

    @Getter
    private final Long epoch;

    @Getter
    private Long randomNumber;

    @Getter
    private Long infectedEpoch;

    public Patient(Long seed, Long epoch) {
        checkSeed(seed);
        checkEpochs(epoch, epoch); // Dank yet effective
        this.seed = seed;
        this.epoch = epoch;
    }

    public Patient(Long seed, Long epoch, Long randomNumber, Long infectedEpoch) {
        checkSeed(seed);
        checkEpochs(epoch, infectedEpoch);
        checkRandomNumber(randomNumber);
        this.seed = seed;
        this.epoch = epoch;
        this.randomNumber = randomNumber;
        this.infectedEpoch = infectedEpoch;
    }

    public Patient(PatientDto data) {
        this(data.getSeed(), data.getEpoch(), data.getRandomNumber(), data.getInfectedEpoch());
    }

    /**
     *  Computes for this patient information the EphID:
     *      EphID = Hash(seed)
     */
    public byte[] ephID() {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(this.ENCRYPTION_ALGORITHM);
            byte[] seedBytes = ByteBuffer.allocate(8).putLong(this.seed).array();
            int ephIDByteSize = 16;

            messageDigest.update(seedBytes);
            return Arrays.copyOfRange(messageDigest.digest(), 0, ephIDByteSize);
        } catch (NoSuchAlgorithmException e) {
            throw new UnknownEncryptionAlgorithmException(this.ENCRYPTION_ALGORITHM);
        }
    }

    /**
     *  Computes for this patient information the hash that is stored by other patients:
     *      Hash(EphID || epoch)
     */
    public byte[] patientHash() {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(this.ENCRYPTION_ALGORITHM);
            byte[] ephID = ephID();
            byte[] epochBytes = ByteBuffer.allocate(8).putLong(this.epoch).array();

            messageDigest.update(ephID);
            messageDigest.update(epochBytes);
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new UnknownEncryptionAlgorithmException(this.ENCRYPTION_ALGORITHM);
        }
    }

    /**
     *  Computes for this patient information the hash that is stored by the medic:
     *      Hash(seed, epoch, randomNumber)
     */
    public byte[] medicHash() {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(this.ENCRYPTION_ALGORITHM);
            byte[] seedBytes = ByteBuffer.allocate(8).putLong(this.seed).array();
            byte[] epochBytes = ByteBuffer.allocate(8).putLong(this.epoch).array();
            byte[] randomNumberBytes = ByteBuffer.allocate(8).putLong(this.randomNumber).array();

            messageDigest.update(seedBytes);
            messageDigest.update(epochBytes);
            messageDigest.update(randomNumberBytes);
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new UnknownEncryptionAlgorithmException(this.ENCRYPTION_ALGORITHM);
        }
    }

    private void checkEpochs(Long epoch, Long infectedEpoch) {
        if (epoch == null || infectedEpoch == null)
            throw new EmptyEpochException();

        if (epoch < 0 || infectedEpoch < 0)
            throw new NegativeEpochException(epoch, infectedEpoch);

        if (epoch < infectedEpoch)
            throw new InvalidEpochException(epoch, infectedEpoch);

    }

    private void checkSeed(Long seed) {
        if (seed == null)
            throw new EmptySeedException();
    }

    private void checkRandomNumber(Long randomNumber) {
        if (randomNumber == null)
            throw new EmptyRandomNumberException();
    }
}
