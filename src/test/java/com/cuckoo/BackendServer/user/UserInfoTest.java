package com.cuckoo.BackendServer.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

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
import org.springframework.test.context.junit.jupiter.SpringExtension;

/*test unit to test the creation of users*/

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class UserInfoTest{

    private String email;
    private String pass;
    private String first;
    private String last;

    @Autowired
    private LoginRepository dbAPI;

    @Autowired
    PasswordEncoder passEncoder;

    @BeforeEach
    public void initVariables(){
        this.email = "filipe.m.cunha@gmail.com";
        this.pass  = "1234";
        this.first = "Filipe";
        this.last  = "Cunha";
    }

    @Test
    public void createUserWithSuccess(){
        System.out.println("Given a user");
        UserType user = new UserType();
        user.setUsername(this.email);
        user.setPassword(this.passEncoder.encode(this.pass));
        user.setFirstName(this.first);
        user.setLastName(this.last);

        System.out.println("When a user that does not exist is created");
        try{
            this.dbAPI.createUserInDatabase(user);
        } catch (UserAlreadyExistsException e){
            fail("User should not exist");
        }
        UserType res = null;

        System.out.println("The user should exist");
        try{
            res = this.dbAPI.getUserInfo(this.email);
        } catch (UnknownUserException e){
            fail("User should exist");
        }
        System.out.println("The info should be the same");
        assertEquals(this.email, res.getUsername(), "Should return the same email");
        assertEquals(null, res.getPassword(), "Should not return a password");        
        assertEquals(this.first,res.getFirstName(), "Should return the same First Name");
        assertEquals(this.last,res.getLastName(), "Should return the same Last Name");
        user.setPassword(this.pass);
        try{
            this.dbAPI.removeUserInDatabase(user);
        } catch(UnknownUserException e){
            fail("User should exist");
        }
    }

    @Test
    public void getUserInfoFromNonExistingUser(){
      System.out.println("The user should not exist exist");
      assertThrows(UnknownUserException.class, 
                     () -> this.dbAPI.getUserInfo(this.email),"The user should not be created");
  }

}