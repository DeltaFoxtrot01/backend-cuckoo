package com.cuckoo.BackendServer.exceptions;


public class UserAlreadyExistsException extends RuntimeException{

    private String username;
    public UserAlreadyExistsException(String username){
        super("Username " + username + " already exists in database");
        this.username = username;
    }

    public String getUsername(){return this.username;}
}