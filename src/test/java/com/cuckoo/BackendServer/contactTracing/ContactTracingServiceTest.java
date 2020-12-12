package com.cuckoo.BackendServer.contactTracing;

import com.cuckoo.BackendServer.exceptions.EmptyPatientDataException;
import com.cuckoo.BackendServer.exceptions.IncompatibleInfectedEpochException;
import com.cuckoo.BackendServer.exceptions.InvalidPatientDataException;
import com.cuckoo.BackendServer.models.contactTracing.patient.Patient;
import com.cuckoo.BackendServer.models.contactTracing.patient.PatientDto;
import com.cuckoo.BackendServer.models.hashes.HashDto;
import com.cuckoo.BackendServer.models.usertype.UserType;
import com.cuckoo.BackendServer.repository.HashesRepository;
import com.cuckoo.BackendServer.repository.LoginRepository;
import com.cuckoo.BackendServer.service.ContactTracingService;
import com.cuckoo.BackendServer.service.HashManagementService;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

@SpringBootTest
@TestPropertySource(locations="classpath:application-test.properties")
@ExtendWith(SpringExtension.class)
public class ContactTracingServiceTest {

    private static final int MAX_NUM_DAYS = 14;
    private static final long MILLIS_PER_DAY = 1000 * 60 * 60 * 24L;

    private String medic1_id;
    private String medic2_id;

    @Autowired
    ContactTracingService contactTracingService;

    @Autowired
    HashManagementService hashManagementService;

    @Autowired
    HashesRepository hashesRepository;

    @Autowired
    private PasswordEncoder passEncoder;

    @Autowired
    private LoginRepository dbAPI;

    @BeforeEach
    public void initUsers() {
        String email1 = "filipe.m.cunha@gmail.com";
        String email2 = "pedro.carrott@gmail.com";

        UserType user = new UserType();
        user.setEmail(email1);
        user.setPassword(this.passEncoder.encode("1234"));
        user.setFirstName("Filipe");
        user.setLastName("Cunha");

        this.dbAPI.createUserInDatabase(user);
        this.medic1_id = this.dbAPI.getUserByEmail(email1).getUsername();

        user = new UserType();
        user.setEmail(email2);
        user.setPassword(this.passEncoder.encode("1234"));
        user.setFirstName("Pedro");
        user.setLastName("Carrott");

        this.dbAPI.createUserInDatabase(user);
        this.medic2_id = this.dbAPI.getUserByEmail(email2).getUsername();
    }

    @AfterEach
    public void deleteUsersAndHashes() {
        this.hashesRepository.clearHashes();

        UserType user = new UserType();
        user.setUsername(UUID.fromString(this.medic1_id));
        user.setEmail("filipe.m.cunha@gmail.com");
        user.setPassword("1234");
        this.dbAPI.removeUserInDatabase(user);

        user.setUsername(UUID.fromString(medic2_id));
        user.setEmail("pedro.carrott@gmail.com");
        user.setPassword("1234");
        this.dbAPI.removeUserInDatabase(user);
    }

    @Test
    public void submitPatientData() {
        Set<PatientDto> data = new HashSet<>();
        Set<Patient> patients = new HashSet<>();
        Long infectedEpoch = System.currentTimeMillis() - (14 * MILLIS_PER_DAY) + (MILLIS_PER_DAY / 24);
        for (long i = 0L; i < MAX_NUM_DAYS; i++) {
            byte[] randomSeed = new byte[32];
            new Random().nextBytes(randomSeed);
            String randomEncodedSeed = Base64.getEncoder().encodeToString(randomSeed);
            PatientDto dto = new PatientDto(randomEncodedSeed, i * MILLIS_PER_DAY + infectedEpoch, 42 * i + 5, infectedEpoch / (1000 * 60 * 60));
            data.add(dto);

            Patient patient = new Patient(dto);
            patient.insertData(dto);
            patients.add(patient);
        }

        byte[] qrCode = new byte[] {};
        for (Patient p : patients)
            qrCode = ArrayUtils.addAll(qrCode, p.medicHash());

        HashDto hash = new HashDto();
        hash.setNote("a note");
        hash.setHashValue(Base64.getEncoder().encodeToString(qrCode));
        this.hashManagementService.addPatient(hash, this.medic1_id);
        HashDto res = this.hashManagementService.getHashes(medic1_id).get(0);
        res.setDate(infectedEpoch);
        this.hashManagementService.markPatientAsPositive(res, medic1_id);

        assertDoesNotThrow(() -> contactTracingService.addPatientInformation(data), "Patient data is valid, no exception should be thrown");
        assertTrue(hashManagementService.getPositiveHashes().isEmpty(), "There should be no more hashes in the database");
    }

    @Test
    public void submitEmptyData() {
        Set<PatientDto> data = new HashSet<>();
        Set<Patient> patients = new HashSet<>();
        Long infectedEpoch = System.currentTimeMillis() - (14 * MILLIS_PER_DAY) + (MILLIS_PER_DAY / 24);
        for (long i = 0L; i < MAX_NUM_DAYS; i++) {
            byte[] randomSeed = new byte[32];
            new Random().nextBytes(randomSeed);
            String randomEncodedSeed = Base64.getEncoder().encodeToString(randomSeed);
            PatientDto dto = new PatientDto(randomEncodedSeed, i * MILLIS_PER_DAY + infectedEpoch, 42 * i + 5, infectedEpoch);
            Patient patient = new Patient(dto);
            patient.insertData(dto);
            patients.add(patient);
        }

        byte[] qrCode = new byte[] {};
        for (Patient p : patients)
            qrCode = ArrayUtils.addAll(qrCode, p.medicHash());

        HashDto hash = new HashDto();
        hash.setNote("a note");
        hash.setHashValue(Base64.getEncoder().encodeToString(qrCode));
        this.hashManagementService.addPatient(hash, this.medic1_id);
        HashDto res = this.hashManagementService.getHashes(medic1_id).get(0);
        res.setDate(infectedEpoch);
        this.hashManagementService.markPatientAsPositive(res, medic1_id);

        assertThrows(EmptyPatientDataException.class, () -> contactTracingService.addPatientInformation(data));
        assertThrows(EmptyPatientDataException.class, () -> contactTracingService.addPatientInformation(null));
    }

    @Test
    public void submitIncompatibleInfectedEpochs() {
        Set<PatientDto> data = new HashSet<>();
        Set<Patient> patients = new HashSet<>();
        Long infectedEpoch = System.currentTimeMillis() - (14 * MILLIS_PER_DAY) + (MILLIS_PER_DAY / 24);
        for (long i = 0L; i < MAX_NUM_DAYS; i++) {
            byte[] randomSeed = new byte[32];
            new Random().nextBytes(randomSeed);
            String randomEncodedSeed = Base64.getEncoder().encodeToString(randomSeed);
            PatientDto dto = new PatientDto(randomEncodedSeed, i * MILLIS_PER_DAY + infectedEpoch, 42 * i + 5, infectedEpoch + i);
            data.add(dto);

            Patient patient = new Patient(dto);
            patient.insertData(dto);
            patients.add(patient);
        }

        byte[] qrCode = new byte[] {};
        for (Patient p : patients)
            qrCode = ArrayUtils.addAll(qrCode, p.medicHash());

        HashDto hash = new HashDto();
        hash.setNote("a note");
        hash.setHashValue(Base64.getEncoder().encodeToString(qrCode));
        this.hashManagementService.addPatient(hash, this.medic1_id);
        HashDto res = this.hashManagementService.getHashes(medic1_id).get(0);
        res.setDate(infectedEpoch);
        this.hashManagementService.markPatientAsPositive(res, medic1_id);

        assertThrows(IncompatibleInfectedEpochException.class, () -> contactTracingService.addPatientInformation(data));
    }

    @Test
    public void submitUnconfirmedPatientData() {
        Set<PatientDto> data = new HashSet<>();
        Long infectedEpoch = System.currentTimeMillis() - (14 * MILLIS_PER_DAY) + (MILLIS_PER_DAY / 24);
        for (long i = 0L; i < MAX_NUM_DAYS; i++) {
            byte[] randomSeed = new byte[32];
            new Random().nextBytes(randomSeed);
            String randomEncodedSeed = Base64.getEncoder().encodeToString(randomSeed);
            PatientDto dto = new PatientDto(randomEncodedSeed, i * MILLIS_PER_DAY + infectedEpoch, 42 * i + 5, infectedEpoch);
            data.add(dto);
        }

        assertThrows(InvalidPatientDataException.class, () -> contactTracingService.addPatientInformation(data));
    }

    @Test
    public void submitWrongInfectedEpoch() {
        Set<PatientDto> data = new HashSet<>();
        Set<Patient> patients = new HashSet<>();
        Long infectedEpoch = System.currentTimeMillis() - (14 * MILLIS_PER_DAY) + (MILLIS_PER_DAY / 24);
        for (long i = 0L; i < MAX_NUM_DAYS; i++) {
            byte[] randomSeed = new byte[32];
            new Random().nextBytes(randomSeed);
            String randomEncodedSeed = Base64.getEncoder().encodeToString(randomSeed);
            PatientDto dto = new PatientDto(randomEncodedSeed, i * MILLIS_PER_DAY + infectedEpoch, 42 * i + 5, infectedEpoch - MILLIS_PER_DAY);
            data.add(dto);

            Patient patient = new Patient(dto);
            patient.insertData(dto);
            patients.add(patient);
        }

        byte[] qrCode = new byte[] {};
        for (Patient p : patients)
            qrCode = ArrayUtils.addAll(qrCode, p.medicHash());

        HashDto hash = new HashDto();
        hash.setNote("a note");
        hash.setHashValue(Base64.getEncoder().encodeToString(qrCode));
        this.hashManagementService.addPatient(hash, this.medic1_id);
        HashDto res = this.hashManagementService.getHashes(medic1_id).get(0);
        res.setDate(infectedEpoch);
        this.hashManagementService.markPatientAsPositive(res, medic1_id);

        assertThrows(InvalidPatientDataException.class, () -> contactTracingService.addPatientInformation(data));
    }
}
