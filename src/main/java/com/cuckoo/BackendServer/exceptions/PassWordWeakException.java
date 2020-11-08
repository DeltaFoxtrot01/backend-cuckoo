package com.cuckoo.BackendServer.exceptions;


public class PassWordWeakException extends RuntimeException{

    private static final long serialVersionUID = 1L;
    private int level;
    public PassWordWeakException(String password, int level) {
        super("Sorry! Your password must have at least 1 cap, 1 low and 1 number");
        this.level = level;
    }

    public int getLevel(){
        return this.level;
    }
}