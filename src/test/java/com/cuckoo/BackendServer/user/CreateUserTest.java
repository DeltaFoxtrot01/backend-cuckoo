package com.cuckoo.BackendServer.user;

import com.cuckoo.BackendServer.exceptions.DatabaseException;
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

import static org.junit.jupiter.api.Assertions.*;

/*test unit to test the creation of users*/

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class CreateUserTest{

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
        UserType user = new UserType();
        user.setUsername(this.email);
        user.setPassword(this.passEncoder.encode(this.pass));
        user.setFirstName(this.first);
        user.setLastName(this.last);

        try {
            this.dbAPI.createUserInDatabase(user);
        } catch (UserAlreadyExistsException e){
            fail("User should not exist");
        }
        UserType res = null;

        try {
            res = this.dbAPI.getUserByUsername(this.email);
        } catch (UnknownUserException e){
            fail("User should exist");
        }

        assertNotNull(res.getId(), "Should return an Id");
        assertEquals(this.email, res.getUsername(), "Should return the same email");
        assertTrue(this.passEncoder.matches(this.pass, res.getPassword()), "Should return the same password");
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
    void createUserThatAlreadyExists(){
        UserType user = new UserType();
        user.setUsername(this.email);
        user.setPassword(this.passEncoder.encode(this.pass));
        user.setFirstName(this.first);
        user.setLastName(this.last);

        try {
            this.dbAPI.createUserInDatabase(user);
        } catch (UserAlreadyExistsException e){
            fail("User should not exist");
        }

        user.setPassword(this.pass);
        assertThrows(UserAlreadyExistsException.class,
        () -> this.dbAPI.createUserInDatabase(user),"User should already exist");
        
        try {
            this.dbAPI.removeUserInDatabase(user);
        } catch(UnknownUserException e){
            fail("User should exist");
        }
    }

    @Test
    public void createUserWithInvalidEmail(){
        UserType user = new UserType();
        user.setUsername("123");
        user.setPassword(this.pass);
        user.setFirstName(this.first);
        user.setLastName(this.last);
        
        assertThrows(DatabaseException.class, 
                     () -> this.dbAPI.createUserInDatabase(user),"The user should not be created");
    }
}