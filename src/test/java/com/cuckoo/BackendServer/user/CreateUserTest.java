package com.cuckoo.BackendServer.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import com.cuckoo.BackendServer.exceptions.DatabaseException;
import com.cuckoo.BackendServer.exceptions.UnknownUserException;
import com.cuckoo.BackendServer.exceptions.UserAlreadyExistsException;
import com.cuckoo.BackendServer.models.usertype.UserType;
import com.cuckoo.BackendServer.repository.DatabaseAPI;

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
public class CreateUserTest{

    private String email;
    private String pass;
    private String first;
    private String last;

    @Autowired
    private DatabaseAPI dbAPI;

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
            res = this.dbAPI.getUserByUsername(this.email);
        } catch (UnknownUserException e){
            fail("User should exist");
        }
        System.out.println("The info should be the same");
        assertEquals(this.email, res.getUsername(), "Should return the same email");
        assertEquals(this.passEncoder.matches(this.pass, res.getPassword()),true, "Should return the same password");        
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
    void createrUserThatAlreadyExists(){
        System.out.println("Given a user");
        UserType user = new UserType();
        user.setUsername(this.email);
        user.setPassword(this.passEncoder.encode(this.pass));
        user.setFirstName(this.first);
        user.setLastName(this.last);

        System.out.println("When a user is created");
        try{
            this.dbAPI.createUserInDatabase(user);
        } catch (UserAlreadyExistsException e){
            fail("User should not exist");
        }

        System.out.println("The same user is created again");
        user.setPassword(this.pass);
        assertThrows(UserAlreadyExistsException.class,
        () -> this.dbAPI.createUserInDatabase(user),"User should already exist");
        
        try{
            this.dbAPI.removeUserInDatabase(user);
        } catch(UnknownUserException e){
            fail("User should exist");
        }
    }

    @Test
    public void createUserWithInvalidEmail(){
        System.out.println("Given a user");
        UserType user = new UserType();
        user.setUsername("123");
        user.setPassword(this.pass);
        user.setFirstName(this.first);
        user.setLastName(this.last);

        System.out.println("When a user is created");
        
        assertThrows(DatabaseException.class, 
                     () -> this.dbAPI.createUserInDatabase(user),"The user should not be created");
    }
}