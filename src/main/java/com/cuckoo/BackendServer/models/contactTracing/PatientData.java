package com.cuckoo.BackendServer.models.contactTracing;

import lombok.Getter;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA3-256");
            messageDigest.update(this.seed.byteValue());
            byte[] ephID = messageDigest.digest();

            messageDigest = MessageDigest.getInstance("SHA3-256");
            messageDigest.update(ephID);
            messageDigest.update(this.epoch.byteValue());
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            return null; // Will never happen
        }
    }

    /**
     *  Computes for this patient information the hash that is stored by the medic:
     *      Hash(seed, epoch, randomNumber)
     */
    public byte[] medicHash() {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA3-256");
            messageDigest.update(this.seed.byteValue());
            messageDigest.update(this.epoch.byteValue());
            messageDigest.update(this.randomNumber.byteValue());
            return messageDigest.digest();
        } catch (NoSuchAlgorithmException e) {
            return null; // Will never happen
        }
    }
}
