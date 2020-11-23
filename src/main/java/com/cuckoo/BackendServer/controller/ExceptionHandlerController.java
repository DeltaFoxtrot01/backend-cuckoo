package com.cuckoo.BackendServer.controller;

import com.cuckoo.BackendServer.exceptions.DatabaseException;
import com.cuckoo.BackendServer.exceptions.PassWordWeakException;
import com.cuckoo.BackendServer.exceptions.UnknownUserException;
import com.cuckoo.BackendServer.exceptions.UserAlreadyExistsException;
import com.cuckoo.BackendServer.exceptions.WrongPasswordException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.jsonwebtoken.SignatureException;

/*to handle the exceptions as HTTP responses*/

@RestControllerAdvice
public class ExceptionHandlerController{

    @ExceptionHandler
    public ResponseEntity<?> wrongPasswordHandler(WrongPasswordException e){
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler
    public ResponseEntity<?> databaseHandler(DatabaseException e){
        return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler
    public ResponseEntity<?> passwordWeakHandler(PassWordWeakException e){
        return new ResponseEntity<>("Level of the password is " + e.getLevel(),HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<?> unknowUserHandler(UnknownUserException e){
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler
    public ResponseEntity<?> userAlreadyExistsHandler(UserAlreadyExistsException e){
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    public ResponseEntity<?> invalidJwt(SignatureException e){
      return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }
}