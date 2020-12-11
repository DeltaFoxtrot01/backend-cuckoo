package com.cuckoo.BackendServer.exceptions;

/**
 * Exception thrown if there is an attemped to delete a hash from
 * a patient that was not marked as positive for COVID 19
 */
public class PatientNotPositiveException extends RuntimeException{

  public PatientNotPositiveException(String message){
    super(message);
  }
}
