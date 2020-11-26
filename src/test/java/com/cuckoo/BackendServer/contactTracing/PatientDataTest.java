package com.cuckoo.BackendServer.contactTracing;

import com.cuckoo.BackendServer.models.contactTracing.PatientData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 *  Test unit that only prints out the calculated hash values to compare with the frontend values.
 */
public class PatientDataTest {

    private static PatientData data;

    @BeforeAll
    static void setup() {
        Long seed = 345239L;
        Long epoch = 976564L;
        Long randomNumber = 5201976L;
        Long infectedEpoch = 976552L;
        data = new PatientData(seed, epoch, randomNumber, infectedEpoch);
    }

    @Test
    public void generateEphID(){
        byte[] id = data.ephID();
        printBytes(id, "EphID");
    }

    @Test
    public void generatePatientHash() {
        byte[] patientHash = data.patientHash();
        printBytes(patientHash, "Patient Hash");
    }

    @Test
    public void generateMedicHash() {
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
