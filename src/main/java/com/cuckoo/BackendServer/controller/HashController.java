package com.cuckoo.BackendServer.controller;

import java.security.Principal;
import java.util.List;

import com.cuckoo.BackendServer.models.hashes.HashDto;
import com.cuckoo.BackendServer.service.HashManagementService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HashController {
  

  @Autowired
  private HashManagementService hashManagementService;


  @PostMapping("/hash")
  public void createHash(@RequestBody HashDto hash, Principal principal){
    this.hashManagementService.addPatient(hash, principal.getName());
  }

  @GetMapping("/hash")
  public List<HashDto> getHashes(Principal principal){
    return this.hashManagementService.getHashes(principal.getName());
  }

  @PutMapping("/hash/negative")
  public void markAsNegative(@RequestBody HashDto hash, Principal principal){
    this.hashManagementService.markPatientAsNegative(hash, principal.getName());
  }

  @PutMapping("/hash/positive")
  public void markAsPositive(@RequestBody HashDto hash, Principal principal){
    this.hashManagementService.markPatientAsPositive(hash, principal.getName());
  }

}
