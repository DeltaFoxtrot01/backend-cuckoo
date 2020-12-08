package com.cuckoo.BackendServer.repository;

import java.util.List;
import java.util.UUID;

import com.cuckoo.BackendServer.exceptions.DatabaseException;
import com.cuckoo.BackendServer.exceptions.FieldTooLongException;
import com.cuckoo.BackendServer.exceptions.InvalidArgumentsException;
import com.cuckoo.BackendServer.exceptions.UnathorizedRequestException;
import com.cuckoo.BackendServer.models.hashes.HashDto;
import com.cuckoo.BackendServer.models.hashes.HashMapper;
import com.cuckoo.BackendServer.models.hashes.HashPositiveMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class HashesRepository {
  
  @Autowired
  private JdbcTemplate jdbcTemplate;


  /**
   * Creates a hash for a patient inside the database
   * @param hash
   * @param principal
   */
  public void addPatient(HashDto hash, String userId){
    if(hash == null || userId == null)
      throw new InvalidArgumentsException("Hash and userId can not be null");
    if(hash.getHashValue() == null || hash.getNote() == null)
      throw new InvalidArgumentsException("Hash value or Hash description can not be null for Hash");
    if(hash.getNote().length() > 100)
      throw new FieldTooLongException("Note can only have 100 characters");
    if(hash.getHashValue().length() > 1000)
      throw new FieldTooLongException("Note can only have 100 characters");

    String sql = "INSERT INTO cuckoo.hashes(medic_id, hash_value, note) VALUES (?,?,?)";

    try{
      this.jdbcTemplate.update(sql, UUID.fromString(userId), hash.getHashValue(), hash.getNote());
    }
    catch (DataAccessException e){
      throw new DatabaseException("Unable to create hash in database:" + e.getMessage());
    }
  }

  /**
   * Returns all hashes marked as positive
   * @return
   */
  public List<HashDto> getPositiveHashes(){
    String sql = "SELECT hash_value, hash_id FROM cuckoo.hashes WHERE is_positive = true";

    try{
      return this.jdbcTemplate.query(sql, new HashPositiveMapper());
    } catch (DataAccessException e){
      throw new DatabaseException("Unable to retrive positive hashes: " + e.getMessage());
    }
  }

  /**
   * Returns all available hashes from a medic
   * @param principal
   */
  public List<HashDto> getHashes(String userId){
    if(userId == null)
      throw new InvalidArgumentsException("userId must be set");

    String sql = "SELECT note, hash_id FROM cuckoo.hashes WHERE medic_id = ? AND is_positive = false";

    return this.jdbcTemplate.query(sql, new Object[] {UUID.fromString(userId)}, new HashMapper());
  }

  /**
   * Deletes the specific hash of that patient, since a hash of a negative patient
   * is not usefull
   * @param hash
   * @param userId
   */
  public void markPatientAsNegative(HashDto hash, String userId){
    if(hash == null || userId == null)
      throw new InvalidArgumentsException("Hash and userId can not be null");
    if(hash.getId() == null)
      throw new InvalidArgumentsException("Hash id must be set");
    String sql = "DELETE FROM cuckoo.hashes WHERE medic_id = ? AND hash_id = ? AND is_positive = false";

    try{
      int res = this.jdbcTemplate.update(sql, UUID.fromString(userId), hash.getId());
      if(res == 0)
        throw new UnathorizedRequestException("Hash does not exist or belongs to another doctor");
    }
    catch (DataAccessException e){
      throw new DatabaseException("Unable to delete hash in database:" + e.getMessage());
    }
  }

  /**
   * Marks a patient as a positive for COVID 19
   * @param hash
   * @param userId
   */
  public void markPatientAsPositive(HashDto hash, String userId){
    if(hash == null || userId == null)
      throw new InvalidArgumentsException("Hash and userId can not be null");
    if(hash.getId() == null)
      throw new InvalidArgumentsException("Hash id must be set");
    String sql = "UPDATE cuckoo.hashes SET is_positive = true WHERE medic_id = ? AND hash_id = ? AND is_positive = false";

    try{
      int res = this.jdbcTemplate.update(sql, UUID.fromString(userId), hash.getId());
      if(res == 0)
        throw new UnathorizedRequestException("Hash does not exist or belongs to another doctor");
    }
    catch (DataAccessException e){
      throw new DatabaseException("Unable to update hash in database:" + e.getMessage());
    }
  }

  /**
   * auxiliar functions for tests
   */
  public void clearHashes(){
    String sql = "DELETE FROM cuckoo.hashes";
    try {
      this.jdbcTemplate.update(sql);
    } catch (DataAccessException e){
      throw new DatabaseException("Unable to delete all hashes: " + e.getMessage());
    }
  }

}
