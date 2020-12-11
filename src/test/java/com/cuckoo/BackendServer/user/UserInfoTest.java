package com.cuckoo.BackendServer.user;

import com.cuckoo.BackendServer.exceptions.UnknownUserException;
import com.cuckoo.BackendServer.exceptions.UserAlreadyExistsException;
import com.cuckoo.BackendServer.models.usertype.UserType;
import com.cuckoo.BackendServer.repository.LoginRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

/*test unit to test the creation of users*/

@SpringBootTest
@TestPropertySource(locations="classpath:application-test.properties")
@ExtendWith(SpringExtension.class)
public class UserInfoTest{

    private String email;
    private String pass;
    private String first;
    private String last;

    @Autowired
    private LoginRepository dbAPI;

    @Autowired
    private PasswordEncoder passEncoder;

    @BeforeEach
    public void initVariables(){
        this.email = "filipe.m.cunha@gmail.com";
        this.pass  = "1234";
        this.first = "Filipe";
        this.last  = "Cunha";
    }

    @Test
    public void createUserWithSuccess(){
        UserType user = new UserType();
        String userId = null;
        user.setEmail(this.email);
        user.setPassword(this.passEncoder.encode(this.pass));
        user.setFirstName(this.first);
        user.setLastName(this.last);

        try {
            this.dbAPI.createUserInDatabase(user);
        } catch (UserAlreadyExistsException e){
            fail("User should not exist");
        }

        UserType res = null;

        userId = this.dbAPI.getUserByEmail(this.email).getUsername().toString();

        try {
            res = this.dbAPI.getUserInfo(userId);
        } catch (UnknownUserException e){
            fail("User should exist");
        }

        assertEquals(this.email, res.getEmail(), "Should return the same email");
        assertNull(res.getPassword(), "Should not return a password");
        assertEquals(this.first,res.getFirstName(), "Should return the same First Name");
        assertEquals(this.last,res.getLastName(), "Should return the same Last Name");
        user.setPassword(this.pass);

        try {
            this.dbAPI.removeUserInDatabase(user);
        } catch(UnknownUserException e){
            fail("User should exist");
        }
    }

    @Test
    public void getUserInfoFromNonExistingUser(){
      assertThrows(UnknownUserException.class, 
                     () -> this.dbAPI.getUserInfo(this.email),"The user should not be created");
  }

}