package com.cuckoo.BackendServer.controller;

import com.cuckoo.BackendServer.models.contactTracing.patient.PatientDto;
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
    private ContactTracingService contactTracingService;

    @PostMapping(value="/contact-tracing/patient", consumes = "application/json; charset=utf-8")
    public ResponseEntity addPatientInformation(@RequestBody Set<PatientDto> patientData) {
        contactTracingService.addPatientInformation(patientData);
        return ResponseEntity.ok().build();
    }
}
