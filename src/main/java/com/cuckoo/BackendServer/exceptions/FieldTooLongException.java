package com.cuckoo.BackendServer.exceptions;


/**
 * Exception used if a given input is larger than its limit
 */
public class FieldTooLongException extends RuntimeException{
  
  public FieldTooLongException(String description){
    super(description);
  }
}
