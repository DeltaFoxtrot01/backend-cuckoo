package com.cuckoo.BackendServer.exceptions;


public class UnknownUserException extends RuntimeException{

    private String username;
    public UnknownUserException(String username){
        super("Unknown user with username " + username);
        this.username = username;
    }

    public String getUsername(){return this.username;}
}