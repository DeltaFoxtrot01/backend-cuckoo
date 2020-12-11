package com.cuckoo.BackendServer.service;

import java.security.Principal;
import java.util.List;

import com.cuckoo.BackendServer.models.hashes.HashDto;
import com.cuckoo.BackendServer.repository.HashesRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Configuration
@EnableAsync
@EnableScheduling
@Service
public class HashManagementService {

  @Autowired
  private HashesRepository hashesRepository;

  /**
   * Adds a new hash of a patient to the database
   * @param principal
   * @param hash
   */
  public void addPatient(HashDto hash, String userId){
    this.hashesRepository.addPatient(hash, userId);
  }

  /**
   * Marks a patient as negative for COVID19
   * @param principal
   * @param hashDto hash id
   */
  public void markPatientAsNegative(HashDto hash, String userId){
    this.hashesRepository.markPatientAsNegative(hash, userId);
  }

  /**
   * Marks patient as positive for COVID 19
   * @param principal
   * @param hashDto hash id
   */
  public void markPatientAsPositive(HashDto hash, String userId){
    this.hashesRepository.markPatientAsPositive(hash, userId);
  }

  /**
   * Returns all the patient hashes for a specific doctor
   * @param principal
   */
  public List<HashDto> getHashes(String userId){
    return this.hashesRepository.getHashes(userId);
  }

  /**
   * Returns all the hashes from positive patients
   * @return List of hashes
   */
  public List<HashDto> getPositiveHashes(){
    return this.hashesRepository.getPositiveHashes();
  }

  /**
   * Deletes a hash from a positive patient
   * @param hashDto
   */
  public void deleteHashFromPositivePatient(HashDto hash){
    this.hashesRepository.deletePositivePatient(hash);
  }

  /**
   * Deletes old hashes from the database periodically
   */
  @Scheduled(fixedDelay = 1000*60*60)
  public void deleteOldHashes(){
    this.hashesRepository.clearOutdatedHashes();
  }
  
}
