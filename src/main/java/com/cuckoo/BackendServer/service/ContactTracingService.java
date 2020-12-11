package com.cuckoo.BackendServer.service;

import com.cuckoo.BackendServer.models.contactTracing.CuckooFilter;
import com.cuckoo.BackendServer.models.contactTracing.patient.Patient;
import com.cuckoo.BackendServer.models.contactTracing.patient.PatientDto;
import com.cuckoo.BackendServer.service.remoteServices.NotificationsRemoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Service
public class ContactTracingService {

    @Autowired
    NotificationsRemoteService notificationsRemoteService;

    public void addDoctorInformation() {
        //DAVID: BAH
    }

    public void addPatientInformation(Set<PatientDto> patientData) {
        // TODO Match medicHash and infectedEpoch with stored medic info
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

        CuckooFilter filter = new CuckooFilter();
        patients.values().stream()
                .flatMap(p -> p.patientHashes().stream())
                .forEach(filter::insert);

        notificationsRemoteService.sendDataNotification(Map.of("filter", filter.toString()));
    }
}
