package com.cuckoo.BackendServer.service;

import com.cuckoo.BackendServer.models.contactTracing.CuckooFilter;
import com.cuckoo.BackendServer.models.contactTracing.patient.Patient;
import com.cuckoo.BackendServer.models.contactTracing.patient.PatientDto;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ContactTracingService {
    public void addDoctorInformation() {
        //DAVID: BAH
    }

    public void addPatientInformation(Set<PatientDto> patientData) {
        // TODO Match medicHash and infectedEpoch with stored medic info
        CuckooFilter filter = new CuckooFilter();
        patientData.stream().map(Patient::new).map(Patient::patientHash).forEach(filter::insert);
        // TODO Send cuckoo filter via push notifications
    }
}