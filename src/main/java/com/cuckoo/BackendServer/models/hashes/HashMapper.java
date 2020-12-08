package com.cuckoo.BackendServer.models.hashes;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.cuckoo.BackendServer.exceptions.DatabaseException;

import org.springframework.jdbc.core.RowMapper;

public class HashMapper implements RowMapper<HashDto>{

  @Override
  public HashDto mapRow(ResultSet rs, int rowNum) throws SQLException {
    try{
      return new HashDto(rs.getInt("hash_id"),rs.getString("note"));
    }
    catch(SQLException e){
      throw new DatabaseException("Unable to map HashDto");
    }
  }
  
}
