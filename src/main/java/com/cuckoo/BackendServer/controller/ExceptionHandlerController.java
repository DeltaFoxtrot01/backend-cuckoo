package com.cuckoo.BackendServer.controller;

import com.cuckoo.BackendServer.exceptions.*;

import io.jsonwebtoken.SignatureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/*to handle the exceptions as HTTP responses*/

@RestControllerAdvice
public class ExceptionHandlerController {

  @ExceptionHandler
  public ResponseEntity<Void> wrongPasswordHandler(WrongPasswordException e){
    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler
  public ResponseEntity<Void> databaseHandler(DatabaseException e){
    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler
  public ResponseEntity<String> passwordWeakHandler(PasswordweakException e){
    return new ResponseEntity<>("Level of the password is " + e.getLevel(),HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler
  public ResponseEntity<Void> unknownUserHandler(UnknownUserException e){
    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler
  public ResponseEntity<Void> userAlreadyExistsHandler(UserAlreadyExistsException e){
    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler
  public ResponseEntity<Void> invalidJwt(SignatureException e){
    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler
  public ResponseEntity<Void> unauthorizedRequest(UnathorizedRequestException e){
    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
  }

  @ExceptionHandler
  public ResponseEntity<Void> fieldLimit(FieldTooLongException e){
    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler
  public ResponseEntity<Void> invalidArguments(InvalidArgumentsException e){
    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler
  public ResponseEntity<Void> firebaseError(FirebaseException e){
    return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler
  public ResponseEntity<?> contactTracingException(EmptySeedException e) {
    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
}
}