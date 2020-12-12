package com.cuckoo.BackendServer.service;

import com.cuckoo.BackendServer.exceptions.EmptyPatientDataException;
import com.cuckoo.BackendServer.exceptions.IncompatibleInfectedEpochException;
import com.cuckoo.BackendServer.exceptions.InvalidPatientDataException;
import com.cuckoo.BackendServer.models.contactTracing.CuckooFilter;
import com.cuckoo.BackendServer.models.contactTracing.patient.Patient;
import com.cuckoo.BackendServer.models.contactTracing.patient.PatientDto;
import com.cuckoo.BackendServer.models.hashes.HashDto;
import com.cuckoo.BackendServer.service.remoteServices.NotificationsRemoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ContactTracingService {

    @Autowired
    NotificationsRemoteService notificationsRemoteService;

    @Autowired
    HashManagementService hashesService;

    public void addPatientInformation(Set<PatientDto> patientData) {
        checkData(patientData);
        Long infectedEpoch = checkInfectedEpoch(patientData);

        Map<Patient, Patient> patients = new HashMap<>();
        patientData.forEach(dto -> {
            Patient patient = new Patient(dto);
            Patient value = patients.get(patient);
            if (value == null) {
                patients.put(patient, patient);
                value = patient;
            }
            value.insertData(dto);
        });

        HashDto positive = null;
        System.out.println("\nFor loop patients for hashes");
        for (Patient p : patients.values()) {
            System.out.println();
            System.out.println("\nMedic hash\n");
            System.out.println(Arrays.toString(p.medicHash()));
            System.out.println();
            positive = validateHash(p.medicHash());
            System.out.println(positive);
            if (positive != null)
                break;
        }

        System.out.println("\nInvalid Patient data");
        System.out.println(positive);
        if (positive != null)
            System.out.println(!Objects.equals(infectedEpoch, positive.getDate() / (1000 * 60 * 60 * 24)));
        System.out.println(infectedEpoch);
        if (positive != null)
            System.out.println(positive.getDate());
        if (positive == null || !Objects.equals(infectedEpoch, positive.getDate() / (1000 * 60 * 60 * 24)))
            throw new InvalidPatientDataException();

        hashesService.deleteHashFromPositivePatient(positive);

        System.out.println("\nCuckoo Filter");
        CuckooFilter filter = new CuckooFilter();
        patients.values().stream()
                .flatMap(p -> p.patientHashes().stream())
                .forEach(filter::insert);

        System.out.println("\nPush notifications");
        notificationsRemoteService.sendDataNotification(Map.of("filter", filter.toString()));
    }

    private void checkData(Set<PatientDto> data) {
        if (data == null || data.isEmpty()) {
            System.out.println("\nEmpty data");
            System.out.println();
            System.out.println(data == null);
            System.out.println();
            throw new EmptyPatientDataException();
        }
    }

    private Long checkInfectedEpoch(Set<PatientDto> data) {
        Long infectedEpoch = null;
        System.out.println("\ncheckInfectedEpoch");
        for (PatientDto dto : data) {
            Long newValue = dto.getInfectedEpoch();
            if (newValue == null || (infectedEpoch != null && !infectedEpoch.equals(newValue))) {
                System.out.println("\nIncompatible infected epoch");
                System.out.println();
                System.out.println(newValue == null);
                System.out.println();
                System.out.println(infectedEpoch != null);
                System.out.println();
                throw new IncompatibleInfectedEpochException();
            }
            infectedEpoch = newValue;
            System.out.println();
            System.out.println(infectedEpoch);
            System.out.println();
        }
        return infectedEpoch;
    }

    private boolean compareHashes(byte[] provided, byte[] saved) {
        System.out.println("\nCompare hashes");
        if (provided.length != saved.length) {
            System.out.println("\nDifferent array length");
            System.out.println();
            System.out.println(provided.length);
            System.out.println();
            System.out.println(saved.length);
            System.out.println();
            return false;
        }

        for (int i = 0; i < provided.length; i++)
            if (provided[i] != saved[i]) {
                System.out.println("\nDifferent array entry");
                return false;
            }

        return true;
    }

    private boolean containsHash(byte[] provided, byte[] allSaved) {
        System.out.println("\nContains hash");
        int hashSize = 32;
        for (int offset = 0; offset < allSaved.length; offset += hashSize) {
            byte[] hash = Arrays.copyOfRange(allSaved, offset, offset + hashSize);
            System.out.println();
            System.out.println(Arrays.toString(hash));
            System.out.println();
            if (compareHashes(provided, hash))
                return true;
        }
        return false;
    }

    private HashDto validateHash(byte[] provided) {
        System.out.println("\nValidate hash");
        List<HashDto> positiveHashes = hashesService.getPositiveHashes();
        System.out.println();
        System.out.println(positiveHashes);
        System.out.println();
        for (HashDto positive : positiveHashes) {
            byte[] bytes = Base64.getDecoder().decode(positive.getHashValue());
            System.out.println();
            System.out.println(Arrays.toString(bytes));
            System.out.println();
            if (containsHash(provided, bytes))
                return positive;
        }
        return null;
    }
}
