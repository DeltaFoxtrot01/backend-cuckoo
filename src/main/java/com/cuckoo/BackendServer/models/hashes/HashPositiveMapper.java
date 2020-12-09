package com.cuckoo.BackendServer.models.hashes;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.cuckoo.BackendServer.exceptions.DatabaseException;

import org.springframework.jdbc.core.RowMapper;

public class HashPositiveMapper implements RowMapper<HashDto> {

  @Override
  public HashDto mapRow(ResultSet rs, int rowNum) throws SQLException {
    try{
      HashDto res = new HashDto();
      res.setId(rs.getInt("hash_id"));
      res.setHashValue(rs.getString("hash_value"));
      res.setDate(rs.getLong("medic_date"));
      return res;
    }
    catch(SQLException e){
      throw new DatabaseException("Unable to map HashDto");
    }
  }
  
}
