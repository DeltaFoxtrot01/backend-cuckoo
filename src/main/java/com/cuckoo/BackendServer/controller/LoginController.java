package com.cuckoo.BackendServer.controller;

import com.cuckoo.BackendServer.models.usertype.UserType;
import com.cuckoo.BackendServer.service.LoginService;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @Autowired
    private LoginService loginService;

    /* retrieves user info */
    @GetMapping("login/user-info")
    public UserType getUserInfo(Principal principal) {
        return this.loginService.getUserInfo(principal.getName()); 
    }

    /* end point for authentication */
    @PostMapping("login/authenticate")
    public void createAuthToken() {
      //authentication endpoint
    }

}