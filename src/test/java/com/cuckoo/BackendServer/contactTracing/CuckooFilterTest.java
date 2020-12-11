package com.cuckoo.BackendServer.contactTracing;

import com.cuckoo.BackendServer.models.contactTracing.CuckooFilter;
import com.cuckoo.BackendServer.models.contactTracing.patient.Patient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations="classpath:application-test.properties")
@ExtendWith(SpringExtension.class)
public class CuckooFilterTest {

    private static CuckooFilter filter;
    private static final int MAX_NUM_ENTRIES = 42;
    private static final double MIN_LOAD_FACTOR = 0.95;

    @BeforeAll
    static void setup() {
        filter = new CuckooFilter();
    }

    @Test
    public void insertOneHash() {
        Long seed = new Random(11).nextLong();
        Long epoch = 1L;
        Patient data = new Patient(seed, epoch);
        byte[] patientHash = data.patientHash();

        filter.insert(patientHash);

        assertTrue(filter.isPresent(patientHash), "Hash fingerprint should be present");
        assertEquals(filter.getCount(), 1, "There should be only one fingerprint in the filter");
    }

    @Test
    public void insertRepeatedHash() {
        Long seed = new Random(22).nextLong();
        Long epoch = 1L;
        Patient data = new Patient(seed, epoch);
        byte[] patientHash = data.patientHash();

        filter.insert(patientHash);
        filter.insert(patientHash);

        assertTrue(filter.isPresent(patientHash), "Hash fingerprint should be present");
        assertEquals(filter.getCount(), 1, "There should be only one fingerprint in the filter");
    }

    @Test
    public void insertManyHashes() {
        List<Long[]> seedsAndEpochs = new ArrayList<>();
        for (Long i = 0L; i < MAX_NUM_ENTRIES; i++) {
            seedsAndEpochs.add(new Long[] { new Random(69).nextLong(), i });
        }

        List<Patient> data = seedsAndEpochs.stream()
                .map(seedAndEpoch ->
                        new Patient(seedAndEpoch[0], seedAndEpoch[1]))
                .collect(Collectors.toList());

        List<byte[]> hashes = data.stream().map(Patient::patientHash).collect(Collectors.toList());
        hashes.forEach(filter::insert);
        double count = hashes.stream().mapToInt(h -> filter.isPresent(h) ? 1 : 0).sum();

        assertEquals(filter.getCount(), MAX_NUM_ENTRIES, "Should return the maximum number of entries");
        assertTrue(count / MAX_NUM_ENTRIES > MIN_LOAD_FACTOR, "Load capacity should be at least " + MIN_LOAD_FACTOR);
    }

    @Test
    public void hashNotPresent() {
        Long seed = new Random(33).nextLong();
        Long epoch = 3L;
        Patient data = new Patient(seed, epoch);
        byte[] patientHash = data.patientHash();

        Long otherSeed = new Random(44).nextLong();
        Long otherEpoch = 4L;
        Patient otherSeedData = new Patient(otherSeed, epoch);
        Patient otherEpochData = new Patient(seed, otherEpoch);
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
