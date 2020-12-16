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
import java.util.stream.Collectors;

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
        for (Patient p : patients.values()) {
            positive = validateHash(p.medicHash());
            if (positive != null)
                break;
        }

        if (positive == null || !Objects.equals(infectedEpoch, positive.getDate() / (1000 * 60 * 60)))
            throw new InvalidPatientDataException();

        Long submittedEpoch = positive.getExpirationDate() / (1000 * 60 * 60);
        Long submittedDay = submittedEpoch / 24;

        Set<Patient> validPatients = new HashSet<>();
        for (Patient p : patients.values()) {
            if (p.getDay() > submittedDay)
                continue;

            if (p.getDay().equals(submittedDay)){
                List<byte[]> validSeeds = new ArrayList<>();
                List<Long> validEpochs = new ArrayList<>();
                List<Long> validRandomValues = new ArrayList<>();

                for (int i = 0; i < p.getSeeds().size(); i++) {
                    if (p.getEpochs().get(i) <= submittedEpoch) {
                        validSeeds.add(p.getSeeds().get(i));
                        validEpochs.add(p.getEpochs().get(i));
                        validRandomValues.add(p.getRandomValues().get(i));
                    }
                }

                p.setSeeds(validSeeds);
                p.setEpochs(validEpochs);
                p.setRandomValues(validRandomValues);
            }

            validPatients.add(p);
        }

        hashesService.deleteHashFromPositivePatient(positive);

        CuckooFilter filter = new CuckooFilter();
        validPatients.stream()
                .flatMap(p -> p.patientHashes().stream())
                .forEach(filter::insert);

        notificationsRemoteService.sendDataNotification(Map.of("filter", filter.toString()));
    }

    private void checkData(Set<PatientDto> data) {
        if (data == null || data.isEmpty())
            throw new EmptyPatientDataException();
    }

    private Long checkInfectedEpoch(Set<PatientDto> data) {
        Long infectedEpoch = null;
        for (PatientDto dto : data) {
            Long newValue = dto.getInfectedEpoch();
            if (newValue == null || (infectedEpoch != null && !infectedEpoch.equals(newValue)))
                throw new IncompatibleInfectedEpochException();
            infectedEpoch = newValue;
        }
        return infectedEpoch;
    }

    private boolean compareHashes(byte[] provided, byte[] saved) {
        if (provided.length != saved.length)
            return false;

        for (int i = 0; i < provided.length; i++)
            if (provided[i] != saved[i]) {
                return false;
            }

        return true;
    }

    private boolean containsHash(byte[] provided, byte[] allSaved) {
        int hashSize = 32;
        for (int offset = 0; offset < allSaved.length; offset += hashSize) {
            byte[] hash = Arrays.copyOfRange(allSaved, offset, offset + hashSize);
            if (compareHashes(provided, hash))
                return true;
        }
        return false;
    }

    private HashDto validateHash(byte[] provided) {
        List<HashDto> positiveHashes = hashesService.getPositiveHashes();
        for (HashDto positive : positiveHashes) {
            byte[] bytes = Base64.getDecoder().decode(positive.getHashValue());
            if (containsHash(provided, bytes))
                return positive;
        }
        return null;
    }
}
