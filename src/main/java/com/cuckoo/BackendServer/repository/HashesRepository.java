package com.cuckoo.BackendServer.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.cuckoo.BackendServer.exceptions.DatabaseException;
import com.cuckoo.BackendServer.exceptions.FieldTooLongException;
import com.cuckoo.BackendServer.exceptions.InvalidArgumentsException;
import com.cuckoo.BackendServer.exceptions.PatientNotPositiveException;
import com.cuckoo.BackendServer.exceptions.UnathorizedRequestException;
import com.cuckoo.BackendServer.models.hashes.HashDto;
import com.cuckoo.BackendServer.models.hashes.HashMapper;
import com.cuckoo.BackendServer.models.hashes.HashPositiveMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

@Repository
public class HashesRepository {
  
  @Autowired
  private JdbcTemplate jdbcTemplate;

  private long getCurrentTime(){
    Date date = new Date();
    return date.getTime();
  }

  private class DateMapper implements RowMapper<Long> {

    @Override
    public Long mapRow(ResultSet rs, int rowNum) throws SQLException {
      return rs.getLong("expiration_date");
    }
  }

  private boolean validateDate(int hashId, long date){
    String sql = "SELECT expiration_date FROM cuckoo.hashes WHERE hash_id = ?";

    try{
      long expDate = this.jdbcTemplate.query(sql,new Object[]{hashId}, new DateMapper()).get(0);
      return expDate - 14*24*60*60*1000 <= date && date <= this.getCurrentTime();
    } catch (DataAccessException e){
      throw new DatabaseException(e.getMessage());
    }
  }

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
      throw new FieldTooLongException("Note can only have 1000 characters");

    String sql = "INSERT INTO cuckoo.hashes(medic_id, hash_value, note, expiration_date) VALUES (?,?,?,?)";
    try{
      this.jdbcTemplate.update(sql, UUID.fromString(userId), hash.getHashValue(), hash.getNote(), this.getCurrentTime());
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
    String sql = "SELECT hash_value, hash_id, medic_date FROM cuckoo.hashes WHERE is_positive = true";

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
    if(hash.getId() == null || hash.getDate() == null)
      throw new InvalidArgumentsException("Hash id and date must be set");

    try{
      if(!this.validateDate(hash.getId(), hash.getDate()))
        throw new InvalidArgumentsException("Date is outside limites");
    } catch(IndexOutOfBoundsException e){
      throw new UnathorizedRequestException("Hash does not exist");
    }
    String sql = "UPDATE cuckoo.hashes SET is_positive = true, medic_date = ? WHERE medic_id = ? AND hash_id = ? AND is_positive = false";

    try{
      int res = this.jdbcTemplate.update(sql,hash.getDate(), UUID.fromString(userId), hash.getId());
      if(res == 0)
        throw new UnathorizedRequestException("Hash does not exist or belongs to another doctor");
    }
    catch (DataAccessException e){
      throw new DatabaseException("Unable to update hash in database:" + e.getMessage());
    }
  }

  /**
   * Deletes a positive patient given an Id
   * @param hash Hash to be deleted
   */
  public void deletePositivePatient(HashDto hash){
    if(hash == null) {
      throw new InvalidArgumentsException("Hash can not be null");
    }
    if(hash.getId() == null) {
      throw new InvalidArgumentsException("Hash id can not be null");
    }
    
    String sql = "DELETE FROM cuckoo.hashes WHERE is_positive = true AND hash_id = ?";

    try{
      int res = this.jdbcTemplate.update(sql,hash.getId());
      if(res == 0) {
        throw new PatientNotPositiveException("Can not delete hash from positive patient");
      }
    } catch(DataAccessException e){
      throw new DatabaseException("Unable to delete hash via Id: " + e.getMessage());
    }
    
  }

  public void clearOutdatedHashes(){
    String sql = "DELETE FROM cuckoo.hashes WHERE expiration_date < ?";

    try{
      this.jdbcTemplate.update(sql, this.getCurrentTime() - 14*24*60*60*1000);
    } catch(DataAccessException e){
      throw new DatabaseException("Unable to delete old hashes");
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
