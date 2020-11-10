package com.cuckoo.BackendServer.controller;

import com.cuckoo.BackendServer.models.usertype.UserType;
import com.cuckoo.BackendServer.service.LoginService;

import java.security.Principal;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @Autowired
    private LoginService loginService;

    /* retrieves user info */
    @GetMapping("login/userInfo")
    public UserType hello(Principal principal) {
        return this.loginService.getUserInfo(principal.getName()); 
    }

    /* end point for authentication */
    @PostMapping("login/authenticate")
    public void createAuthToken() {
      //authentication endpoint
    }

    @PutMapping("login/logout")
    public void logoutAccount(HttpServletResponse response){
        Cookie cookie = new Cookie("sessionToken","");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);
    }

}