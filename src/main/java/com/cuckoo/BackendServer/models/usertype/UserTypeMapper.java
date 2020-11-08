package com.cuckoo.BackendServer.models.usertype;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;


/* row mapper for user*/ 
public class UserTypeMapper implements RowMapper<UserType>{
    @Override
    public UserType mapRow(ResultSet rs, int rowNum) throws SQLException {
        String username = rs.getString("email");
        String password = rs.getString("pass");
        String first = rs.getString("first_name");
        String last = rs.getString("last_name");
        return new UserType(username,password,first,last);
    }
}