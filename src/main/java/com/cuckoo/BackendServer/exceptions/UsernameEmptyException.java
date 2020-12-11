package com.cuckoo.BackendServer.exceptions;

/**
 * Exception thrown if invalid arguments are passed
 */
public class UsernameEmptyException extends InvalidArgumentsException{

  public UsernameEmptyException(String description){
    super(description);
  }
}
