package com.cuckoo.BackendServer.contactTracing;

import com.cuckoo.BackendServer.exceptions.*;
import com.cuckoo.BackendServer.models.contactTracing.patient.Patient;
import com.cuckoo.BackendServer.models.contactTracing.patient.PatientDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class PatientTest {

    private static final byte[] seed = new byte[32];
    private static String encodedSeed;
    private static Long epoch;
    private static Long randomNumber;
    private static Long infectedEpoch;

    private static final byte[] otherSeed = new byte[32];
    private static String otherEncodedSeed;
    private static Long otherEpoch;
    private static Long otherRandomNumber;

    @BeforeAll
    static void setup() {
        Random random = new Random(99);

        random.nextBytes(seed);
        encodedSeed = Base64.getEncoder().encodeToString(seed);
        epoch = 976564L;
        randomNumber = random.nextLong();
        infectedEpoch = 976552L;

        random.nextBytes(otherSeed);
        otherEncodedSeed = Base64.getEncoder().encodeToString(otherSeed);
        otherEpoch = 976565L;
        otherRandomNumber = random.nextLong();
    }

    @Test
    public void createPatientFromDto() {
        PatientDto dto = new PatientDto(encodedSeed, epoch, randomNumber, infectedEpoch);
        PatientDto otherDto = new PatientDto(otherEncodedSeed, otherEpoch, otherRandomNumber, infectedEpoch);

        Patient data = new Patient(dto);
        data.insertData(dto);
        data.insertData(otherDto);

        assertArrayEquals(data.getSeeds().get(0), seed, "Should contain the first seed");
        assertArrayEquals(data.getSeeds().get(1), otherSeed, "Should contain the second seed");
        assertEquals(data.getEpochs().get(0), epoch, "Should contain the correct epoch");
        assertEquals(data.getEpochs().get(1), otherEpoch, "Should contain the correct epoch");
        assertEquals(data.getRandomValues().get(0), randomNumber, "Should contain the correct random number");
        assertEquals(data.getRandomValues().get(1), otherRandomNumber, "Should contain the correct random number");
        assertEquals(data.getInfectedEpoch(), infectedEpoch, "Should contain the correct infected epoch");
    }
    
    @Test
    public void invalidInputs() {
        PatientDto dto = new PatientDto(encodedSeed, epoch, randomNumber, infectedEpoch);

        dto.setEncodedSeed(null);
        assertThrows(EmptySeedException.class, () -> {
            Patient data = new Patient(dto);
            data.insertData(dto);
        }, "Should not accept a null seed");
        dto.setEncodedSeed("");
        assertThrows(EmptySeedException.class, () -> {
            Patient data = new Patient(dto);
            data.insertData(dto);
        }, "Should not accept an empty seed");
        dto.setEncodedSeed(encodedSeed);

        dto.setEpoch(null);
        assertThrows(EmptyEpochException.class, () -> {
            Patient data = new Patient(dto);
            data.insertData(dto);
        }, "Should not accept a null epoch");
        dto.setEpoch(-1L);
        assertThrows(NegativeEpochException.class, () -> {
            Patient data = new Patient(dto);
            data.insertData(dto);
        }, "Should not accept a negative epoch");
        dto.setEpoch(infectedEpoch - 1);
        assertThrows(InvalidEpochException.class, () -> {
            Patient data = new Patient(dto);
            data.insertData(dto);
        }, "Should not accept an epoch lower than infected epoch");
        dto.setEpoch(epoch);

        dto.setRandomNumber(null);
        assertThrows(EmptyRandomNumberException.class, () -> {
            Patient data = new Patient(dto);
            data.insertData(dto);
        }, "Should not accept a null random number");
        dto.setRandomNumber(randomNumber);

        dto.setInfectedEpoch(null);
        assertThrows(EmptyEpochException.class, () -> {
            Patient data = new Patient(dto);
            data.insertData(dto);
        }, "Should not accept a null infected epoch");
        dto.setInfectedEpoch(-2L);
        assertThrows(NegativeEpochException.class, () -> {
            Patient data = new Patient(dto);
            data.insertData(dto);
        }, "Should not accept a negative infected epoch");
        dto.setInfectedEpoch(infectedEpoch);
    }

    /**
     *  Test methods that print out the calculated hash values to compare with the frontend values.
     */

    @Test
    public void generateEphID(){
        PatientDto dto = new PatientDto(encodedSeed, epoch, randomNumber, infectedEpoch);
        Patient data = new Patient(dto);
        data.insertData(dto);

        List<byte[]> ids = data.ephIDs();
        for (byte[] id : ids)
            printBytes(id, "EphID");
    }

    @Test
    public void generatePatientHash() {
        PatientDto dto = new PatientDto(encodedSeed, epoch, randomNumber, infectedEpoch);
        Patient data = new Patient(dto);
        data.insertData(dto);

        List<byte[]> patientHashes = data.patientHashes();
        for (byte[] h : patientHashes)
            printBytes(h, "Patient Hash");
    }

    @Test
    public void generateMedicHash() {
        PatientDto dto = new PatientDto(encodedSeed, epoch, randomNumber, infectedEpoch);
        Patient data = new Patient(dto);
        data.insertData(dto);

        byte[] medicHash = data.medicHash();
        printBytes(medicHash, "Medic Hash");
    }

    // Java reads bytes as signed ints so this function prints out all values as unsigned ints
    private void printBytes(byte[] bytes, String name) {
        System.out.print(name + ": [");
        for (int b : bytes) {
            System.out.print(b & 0xff);
            System.out.print(", ");
        }
        System.out.println("\b\b]");
    }
}
