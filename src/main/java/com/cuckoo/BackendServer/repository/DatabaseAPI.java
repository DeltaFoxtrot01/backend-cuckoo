package com.cuckoo.BackendServer.repository;

import com.cuckoo.BackendServer.models.usertype.UserType;

public interface DatabaseAPI{
    /*returns the query that gets the user by password*/
    public UserType getUserByUsername(String username);

    /*creates a user in the database throwing an exception in case the user already exists*/
    public void createUserInDatabase(UserType user);

    /*remove an existing user from the database*/
    public void removeUserInDatabase(UserType user);

}