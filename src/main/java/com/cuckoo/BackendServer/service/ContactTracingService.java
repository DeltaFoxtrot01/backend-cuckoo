package com.cuckoo.BackendServer.service;

import com.cuckoo.BackendServer.models.contactTracing.CuckooFilter;
import com.cuckoo.BackendServer.models.contactTracing.PatientData;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class ContactTracingService {
    public void addDoctorInformation() {
        //DAVID: BAH
    }

    public void addPatientInformation(Set<PatientData> patientData) {
        // TODO Match medicHash and infectedEpoch with stored medic info
        CuckooFilter filter = new CuckooFilter();
        patientData.stream().map(PatientData::patientHash).forEach(filter::insert);
        // TODO Send cuckoo filter via push notifications
    }
}
