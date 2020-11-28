package com.cuckoo.BackendServer.contactTracing;

import com.cuckoo.BackendServer.models.contactTracing.CuckooFilter;
import com.cuckoo.BackendServer.models.contactTracing.patient.Patient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class CuckooFilterTest {

    private static CuckooFilter filter;
    private static final int MAX_NUM_ENTRIES = 42;
    private static final double MIN_LOAD_FACTOR = 0.95;

    private static final byte[] seed = new byte[32];
    private static String encodedSeed;
    private static Long epoch;

    @BeforeAll
    static void setup() {
        filter = new CuckooFilter();
        new Random(99).nextBytes(seed);
        encodedSeed = Base64.getEncoder().encodeToString(seed);
        epoch = 33L;
    }

    @Test
    public void insertOneHash() {
        Patient data = new Patient(encodedSeed, epoch);
        byte[] patientHash = data.patientHash();

        filter.insert(patientHash);

        assertTrue(filter.isPresent(patientHash), "Hash fingerprint should be present");
        assertEquals(filter.getCount(), 1, "There should be only one fingerprint in the filter");
    }

    @Test
    public void insertRepeatedHash() {
        Patient data = new Patient(encodedSeed, epoch);
        byte[] patientHash = data.patientHash();

        filter.insert(patientHash);
        filter.insert(patientHash);

        assertTrue(filter.isPresent(patientHash), "Hash fingerprint should be present");
        assertEquals(filter.getCount(), 1, "There should be only one fingerprint in the filter");
    }

    @Test
    public void insertManyHashes() {
        List<Patient> data = new ArrayList<>();
        for (long i = 0L; i < MAX_NUM_ENTRIES; i++) {
            byte[] randomSeed = new byte[32];
            new Random().nextBytes(randomSeed);
            String randomEncodedSeed = Base64.getEncoder().encodeToString(randomSeed);
            data.add(new Patient(randomEncodedSeed, i));
        }

        List<byte[]> hashes = data.stream().map(Patient::patientHash).collect(Collectors.toList());
        hashes.forEach(filter::insert);
        double count = hashes.stream().mapToInt(h -> filter.isPresent(h) ? 1 : 0).sum();

        assertEquals(filter.getCount(), MAX_NUM_ENTRIES, "Should return the maximum number of entries");
        assertTrue(count / MAX_NUM_ENTRIES > MIN_LOAD_FACTOR, "Load capacity should be at least " + MIN_LOAD_FACTOR);
    }

    @Test
    public void hashNotPresent() {
        Patient data = new Patient(encodedSeed, epoch);
        byte[] patientHash = data.patientHash();

        byte[] otherSeed = new byte[32];
        new Random(100).nextBytes(otherSeed);
        String otherEncodedSeed = Base64.getEncoder().encodeToString(otherSeed);
        Long otherEpoch = 34L;

        Patient otherSeedData = new Patient(otherEncodedSeed, epoch);
        Patient otherEpochData = new Patient(encodedSeed, otherEpoch);
        byte[] otherSeedPatientHash = otherSeedData.patientHash();
        byte[] otherEpochPatientHash = otherEpochData.patientHash();

        filter.insert(patientHash);

        assertTrue(filter.isPresent(patientHash), "Hash fingerprint should be present");
        assertEquals(filter.getCount(), 1, "There should be only one fingerprint in the filter");
        assertFalse(filter.isPresent(otherSeedPatientHash), "Hash fingerprint should not be present");
        assertFalse(filter.isPresent(otherEpochPatientHash), "Hash fingerprint should not be present");
    }

    @AfterEach
    public void clean() {
        filter.clear();
    }
}
