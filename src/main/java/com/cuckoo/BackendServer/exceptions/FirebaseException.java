package com.cuckoo.BackendServer.exceptions;


public class FirebaseException extends RuntimeException{
  
  public FirebaseException(String message){
    super(message);
  }
}
