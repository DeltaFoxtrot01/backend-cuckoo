package com.cuckoo.BackendServer.contactTracing;

import com.cuckoo.BackendServer.exceptions.*;
import com.cuckoo.BackendServer.models.contactTracing.patient.Patient;
import com.cuckoo.BackendServer.models.contactTracing.patient.PatientDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class PatientTest {

    private static final byte[] seed = new byte[32];
    private static String encodedSeed;
    private static Long epoch;
    private static Long randomNumber;
    private static Long infectedEpoch;

    @BeforeAll
    static void setup() {
        new Random(99).nextBytes(seed);
        encodedSeed = Base64.getEncoder().encodeToString(seed);
        epoch = 976564L;
        randomNumber = 5201976L;
        infectedEpoch = 976552L;
    }

    @Test
    public void createPatientFromDto() {
        PatientDto dto = new PatientDto(encodedSeed, epoch, randomNumber, infectedEpoch);
        Patient data = new Patient(dto);

        assertArrayEquals(data.getSeed(), seed, "Should contain the correct seed");
        assertEquals(data.getEpoch(), epoch, "Should contain the correct epoch");
        assertEquals(data.getRandomNumber(), randomNumber, "Should contain the correct random number");
        assertEquals(data.getInfectedEpoch(), infectedEpoch, "Should contain the correct infected epoch");
    }
    
    @Test
    public void invalidInputs() {
        PatientDto dto = new PatientDto(encodedSeed, epoch, randomNumber, infectedEpoch);

        dto.setEncodedSeed(null);
        assertThrows(EmptySeedException.class, () -> new Patient(dto), "Should not accept a null seed");
        dto.setEncodedSeed("");
        assertThrows(EmptySeedException.class, () -> new Patient(dto), "Should not accept an empty seed");
        dto.setEncodedSeed(encodedSeed);

        dto.setEpoch(null);
        assertThrows(EmptyEpochException.class, () -> new Patient(dto), "Should not accept a null epoch");
        dto.setEpoch(-1L);
        assertThrows(NegativeEpochException.class, () -> new Patient(dto), "Should not accept a negative epoch");
        dto.setEpoch(infectedEpoch - 1);
        assertThrows(InvalidEpochException.class, () -> new Patient(dto), "Should not accept an epoch lower than infected epoch");
        dto.setEpoch(epoch);

        dto.setRandomNumber(null);
        assertThrows(EmptyRandomNumberException.class, () -> new Patient(dto), "Should not accept a null random number");
        dto.setRandomNumber(randomNumber);

        dto.setInfectedEpoch(null);
        assertThrows(EmptyEpochException.class, () -> new Patient(dto), "Should not accept a null infected epoch");
        dto.setInfectedEpoch(-2L);
        assertThrows(NegativeEpochException.class, () -> new Patient(dto), "Should not accept a negative infected epoch");
        dto.setInfectedEpoch(infectedEpoch);
    }

    /**
     *  Test methods that print out the calculated hash values to compare with the frontend values.
     */

    @Test
    public void generateEphID(){
        Patient data = new Patient(encodedSeed, epoch, randomNumber, infectedEpoch);
        byte[] id = data.ephID();
        printBytes(id, "EphID");
    }

    @Test
    public void generatePatientHash() {
        Patient data = new Patient(encodedSeed, epoch, randomNumber, infectedEpoch);
        byte[] patientHash = data.patientHash();
        printBytes(patientHash, "Patient Hash");
    }

    @Test
    public void generateMedicHash() {
        Patient data = new Patient(encodedSeed, epoch, randomNumber, infectedEpoch);
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
