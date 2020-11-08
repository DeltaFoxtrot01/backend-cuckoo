package com.cuckoo.BackendServer.service;

import com.cuckoo.BackendServer.models.usertype.UserType;
import com.cuckoo.BackendServer.repository.DatabaseAPI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class LoginService implements UserDetailsService{

    @Autowired
    private DatabaseAPI dbAPI;

    //@Autowired
    //private AuthenticationManager authManager;

    /* method to allow a login of a user */
    //public String doLogin(UserType user) {
    //    try {
    //        this.authManager
    //                .authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
    //    } catch (BadCredentialsException e) {
    //        throw new WrongPasswordException(user.getUsername());
    //    }
    //    final UserType userDetails = this.dbAPI.getUserByUsername(user.getUsername());
    //    return this.jwtTokenUtil.generateToken(userDetails);
    //}
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
            UserType res;
            res = dbAPI.getUserByUsername(username);
            return res;
    }

}