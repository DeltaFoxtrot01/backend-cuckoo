package com.cuckoo.BackendServer.models.contactTracing.patient;

import com.cuckoo.BackendServer.exceptions.*;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

public class Patient {

    private final String ENCRYPTION_ALGORITHM = "SHA-256";

    @Getter
    private final Long day;

    @Getter
    private final List<byte[]> seeds = new ArrayList<>();

    @Getter
    private final List<Long> epochs = new ArrayList<>();

    @Getter
    private final List<Long> randomValues = new ArrayList<>();

    @Getter
    private final Long infectedEpoch;

    public Patient(PatientDto data) {
        checkEpochs(data.getEpoch(), data.getInfectedEpoch());
        this.infectedEpoch = data.getInfectedEpoch();
        this.day = data.getEpoch() / (1000 * 60 * 60 * 24);

        System.out.println("\nPatient constructor");
        System.out.println();
        System.out.println(this.infectedEpoch);
        System.out.println();
        System.out.println(this.day);
        System.out.println();
    }

    public void insertData(PatientDto data) {
        checkEncodedSeed(data.getEncodedSeed());
        checkEpochs(data.getEpoch(), data.getInfectedEpoch());
        checkRandomNumber(data.getRandomNumber());
        this.seeds.add(Base64.getDecoder().decode(data.getEncodedSeed()));
        this.epochs.add(data.getEpoch());
        this.randomValues.add(data.getRandomNumber());

        System.out.println("\nPatient insert data");
        System.out.println();
        System.out.println(this.seeds);
        System.out.println();
        System.out.println(this.epochs);
        System.out.println();
        System.out.println(this.randomValues);
        System.out.println();
    }

    /**
     *  Computes for this patient information the EphIDs:
     *      EphID = Hash(seed)
     */
    public List<byte[]> ephIDs() {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(this.ENCRYPTION_ALGORITHM);
            int ephIDByteSize = 16;

            List<byte[]> ephIDs = new ArrayList<>();
            for (byte[] s : this.seeds) {
                messageDigest.update(s);
                ephIDs.add(Arrays.copyOfRange(messageDigest.digest(), 0, ephIDByteSize));
                messageDigest.reset();
            }

            System.out.println();
            for (byte[] id : ephIDs) {
                System.out.println(Arrays.toString(id));
            }
            System.out.println();
            return ephIDs;
        } catch (NoSuchAlgorithmException e) {
            throw new UnknownEncryptionAlgorithmException(this.ENCRYPTION_ALGORITHM);
        }
    }

    /**
     *  Computes for this patient information the hashes that are stored by other patients:
     *      Hash(EphID || epoch)
     */
    public List<byte[]> patientHashes() {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(this.ENCRYPTION_ALGORITHM);
            List<byte[]> ephIDs = ephIDs();
            List<byte[]> epochs = this.epochs
                    .stream()
                    .map(e -> ByteBuffer.allocate(8).putLong(e).array())
                    .collect(Collectors.toList());

            List<byte[]> hashes = new ArrayList<>();
            for (int i = 0; i < ephIDs.size(); i++) {
                messageDigest.update(ephIDs.get(i));
                messageDigest.update(epochs.get(i));
                hashes.add(messageDigest.digest());
                messageDigest.reset();
            }

            System.out.println();
            for (byte[] id : hashes) {
                System.out.println(Arrays.toString(id));
            }
            System.out.println();

            return hashes;
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
            List<byte[]> epochs = this.epochs
                    .stream()
                    .map(e -> ByteBuffer.allocate(8).putLong(e).array())
                    .collect(Collectors.toList());
            List<byte[]> randomValues = this.randomValues
                    .stream()
                    .map(v -> ByteBuffer.allocate(8).putLong(v).array())
                    .collect(Collectors.toList());

            for (int i = 0; i < this.seeds.size(); i++) {
                messageDigest.update(this.seeds.get(i));
                messageDigest.update(epochs.get(i));
                messageDigest.update(randomValues.get(i));
            }

            byte[] hash = messageDigest.digest();
            System.out.println();
            System.out.println(Arrays.toString(hash));
            System.out.println();
            return hash;
        } catch (NoSuchAlgorithmException e) {
            throw new UnknownEncryptionAlgorithmException(this.ENCRYPTION_ALGORITHM);
        }
    }

    private void checkEpochs(Long epoch, Long infectedEpoch) {
        System.out.println("\ncheckEpochs");
        if (epoch == null || infectedEpoch == null) {
            System.out.println("\nEmpty epoch");
            System.out.println();
            System.out.println(epoch == null);
            System.out.println();
            throw new EmptyEpochException();
        }

        if (epoch < 0 || infectedEpoch < 0) {
            System.out.println("\nNegative epoch");
            System.out.println();
            System.out.println(epoch < 0);
            System.out.println();
            throw new NegativeEpochException(epoch, infectedEpoch);
        }

        if (epoch < infectedEpoch) {
            System.out.println("\nInvalid epoch");
            System.out.println();
            System.out.println(epoch);
            System.out.println();
            System.out.println(infectedEpoch);
            System.out.println();
            throw new InvalidEpochException(epoch, infectedEpoch);
        }

    }

    private void checkEncodedSeed(String encodedSeed) {
        System.out.println("\ncheckEncodedSeed");
        System.out.println();
        if (encodedSeed == null || encodedSeed.isEmpty()) {
            System.out.println();
            System.out.println(encodedSeed == null);
            System.out.println();
            throw new EmptySeedException();
        }
    }

    private void checkRandomNumber(Long randomNumber) {
        System.out.println("\ncheckRandomNumber");
        if (randomNumber == null)
            throw new EmptyRandomNumberException();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return this.day.equals(patient.day);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.day);
    }
}
