package com.cuckoo.BackendServer.exceptions;

/**
 * Exception thrown if invalid arguments are passed
 */
public class InvalidArgumentsException extends RuntimeException{

  public InvalidArgumentsException(String name){
    super(name);
  }
}
