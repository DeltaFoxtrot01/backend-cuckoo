package com.cuckoo.BackendServer.models.usertype;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.UUID;

import org.springframework.jdbc.core.RowMapper;


/* row mapper for user*/ 
public class UserTypeMapper implements RowMapper<UserType>{
    @Override
    public UserType mapRow(ResultSet rs, int rowNum) throws SQLException {

        UUID id = null;
        String username = null;
        String password = null;
        String first = null;
        String last = null;

        try {
            username = rs.getString("email");
        } catch (SQLException e){
            //continue
        }

        try {
            id = rs.getObject("id", UUID.class);
        } catch (SQLException e){
            //continue
        }

        try {
            password = rs.getString("pass");
        } catch (SQLException e){
            //continue
        }

        try {
            first = rs.getString("first_name");
        } catch (SQLException e){
            //continue
        }

        try {
            last = rs.getString("last_name");
        } catch (SQLException e){
            //continue
        }

        return new UserType(id, username, password, first, last);
    }
}