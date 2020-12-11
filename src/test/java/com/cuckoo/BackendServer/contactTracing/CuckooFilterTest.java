package com.cuckoo.BackendServer.contactTracing;

import com.cuckoo.BackendServer.models.contactTracing.CuckooFilter;
import com.cuckoo.BackendServer.models.contactTracing.patient.Patient;
import com.cuckoo.BackendServer.models.contactTracing.patient.PatientDto;
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

    private static final byte[] seed = new byte[32];
    private static String encodedSeed;
    private static Long epoch;
    private static Long randomNumber;
    private static Long infectedEpoch;

    @BeforeAll
    static void setup() {
        filter = new CuckooFilter();
        Random random = new Random(99);
        random.nextBytes(seed);
        encodedSeed = Base64.getEncoder().encodeToString(seed);
        epoch = 33L;
        randomNumber = random.nextLong();
        infectedEpoch = 30L;
    }

    @Test
    public void insertOneHash() {
        PatientDto dto = new PatientDto(encodedSeed, epoch, randomNumber, infectedEpoch);
        Patient data = new Patient(dto);
        data.insertData(dto);

        List<byte[]> hashes = data.patientHashes();
        hashes.forEach(filter::insert);

        assertTrue(filter.isPresent(hashes.get(0)), "Hash fingerprint should be present");
        assertEquals(filter.getCount(), 1, "There should be only one fingerprint in the filter");
    }

    @Test
    public void insertRepeatedHash() {
        PatientDto dto = new PatientDto(encodedSeed, epoch, randomNumber, infectedEpoch);
        Patient data = new Patient(dto);
        data.insertData(dto);

        List<byte[]> hashes = data.patientHashes();
        hashes.forEach(filter::insert);
        hashes.forEach(filter::insert);

        assertTrue(filter.isPresent(hashes.get(0)), "Hash fingerprint should be present");
        assertEquals(filter.getCount(), 1, "There should be only one fingerprint in the filter");
    }

    @Test
    public void insertManyHashes() {
        List<Patient> data = new ArrayList<>();
        for (long i = 0L; i < MAX_NUM_ENTRIES; i++) {
            byte[] randomSeed = new byte[32];
            new Random().nextBytes(randomSeed);
            String randomEncodedSeed = Base64.getEncoder().encodeToString(randomSeed);
            PatientDto dto = new PatientDto(randomEncodedSeed, i + 1, 42 * i + 5, i);
            Patient patient = new Patient(dto);
            patient.insertData(dto);
            data.add(patient);
        }

        List<byte[]> hashes = data.stream()
                .flatMap(patient -> patient.patientHashes().stream())
                .collect(Collectors.toList());
        hashes.forEach(filter::insert);
        double count = hashes.stream().mapToInt(h -> filter.isPresent(h) ? 1 : 0).sum();

        assertEquals(filter.getCount(), MAX_NUM_ENTRIES, "Should return the maximum number of entries");
        assertTrue(count / MAX_NUM_ENTRIES > MIN_LOAD_FACTOR, "Load capacity should be at least " + MIN_LOAD_FACTOR);
    }

    @Test
    public void hashNotPresent() {
        PatientDto dto = new PatientDto(encodedSeed, epoch, randomNumber, infectedEpoch);
        Patient data = new Patient(dto);
        data.insertData(dto);
        List<byte[]> hashes = data.patientHashes();
        filter.insert(hashes.get(0));

        byte[] otherSeed = new byte[32];
        new Random(100).nextBytes(otherSeed);
        String otherEncodedSeed = Base64.getEncoder().encodeToString(otherSeed);
        Long otherEpoch = 34L;

        PatientDto otherSeedDto = new PatientDto(otherEncodedSeed, epoch, randomNumber, infectedEpoch);
        Patient otherSeedData = new Patient(otherSeedDto);
        otherSeedData.insertData(otherSeedDto);
        List<byte[]> otherSeedHashes = otherSeedData.patientHashes();

        PatientDto otherEpochDto = new PatientDto(encodedSeed, otherEpoch, randomNumber, infectedEpoch);
        Patient otherEpochData = new Patient(otherEpochDto);
        otherEpochData.insertData(otherEpochDto);
        List<byte[]> otherEpochHashes = otherEpochData.patientHashes();

        assertTrue(filter.isPresent(hashes.get(0)), "Hash fingerprint should be present");
        assertEquals(filter.getCount(), 1, "There should be only one fingerprint in the filter");
        assertFalse(filter.isPresent(otherSeedHashes.get(0)), "Hash fingerprint should not be present");
        assertFalse(filter.isPresent(otherEpochHashes.get(0)), "Hash fingerprint should not be present");
    }

    @AfterEach
    public void clean() {
        filter.clear();
    }
}
