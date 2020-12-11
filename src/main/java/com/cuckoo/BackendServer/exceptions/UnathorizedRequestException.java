package com.cuckoo.BackendServer.exceptions;

public class UnathorizedRequestException extends RuntimeException{
  
  public UnathorizedRequestException(String message){
    super(message);
  }
}
