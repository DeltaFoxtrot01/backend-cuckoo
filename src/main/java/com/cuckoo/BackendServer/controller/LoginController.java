package com.cuckoo.BackendServer.controller;

import com.cuckoo.BackendServer.models.usertype.UserType;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {



    /* testing end point */
    @GetMapping("login/hello")
    public String hello() {
        return "hello world";
    }

    /* end point for authentication */
    @PostMapping("login/authenticate")
    public void createAuthToken(@RequestBody UserType user) {
      //endpoint for authentication
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