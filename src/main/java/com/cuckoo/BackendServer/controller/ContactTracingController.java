package com.cuckoo.BackendServer.controller;

import com.cuckoo.BackendServer.models.contactTracing.PatientData;
import com.cuckoo.BackendServer.service.ContactTracingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
public class ContactTracingController {

    @Autowired
    ContactTracingService contactTracingService;

    @PostMapping(value="/contact-tracing/patient-data", consumes = "application/json; charset=utf-8")
    public ResponseEntity addPatientInformation(@RequestBody Set<PatientData> patientData) {
        contactTracingService.addPatientInformation(patientData);
        return ResponseEntity.ok().build();
    }
}
