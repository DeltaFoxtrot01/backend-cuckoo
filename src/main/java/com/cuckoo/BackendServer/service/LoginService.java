package com.cuckoo.BackendServer.service;

import com.cuckoo.BackendServer.exceptions.InvalidArgumentsException;
import com.cuckoo.BackendServer.models.jwt.JwtHolder;
import com.cuckoo.BackendServer.models.usertype.UserType;
import com.cuckoo.BackendServer.repository.LoginRepository;
import com.cuckoo.BackendServer.securitysettings.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class LoginService implements UserDetailsService{

    @Autowired
    private JwtUtil jwtService;

    @Autowired
    private LoginRepository dbAPI;
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        UserType res;
        res = dbAPI.getUserByUsername(username);
        return res;
    }

    /**
     * returns all the info of the user but password hash
     * @param username 
     * @return UserType
     */
    public UserType getUserInfo(String username){
      if(username == null)
        throw new InvalidArgumentsException("Username can not be null for getUserInfo at service");

      return this.dbAPI.getUserInfo(username);
    }

    public String createJwtToken(String username){
      String response = null;
      JwtHolder jwt;
      UserType user = this.dbAPI.getUserInfo(username);
      
      jwt = new JwtHolder(this.jwtService.generateToken(user));
      response = jwt.getJwt();

      return response;
    }
}