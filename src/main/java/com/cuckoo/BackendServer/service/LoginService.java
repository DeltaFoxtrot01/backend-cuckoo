package com.cuckoo.BackendServer.service;

import com.cuckoo.BackendServer.exceptions.UsernameEmptyException;
import com.cuckoo.BackendServer.models.jwt.JwtHolder;
import com.cuckoo.BackendServer.models.usertype.UserType;
import com.cuckoo.BackendServer.repository.LoginRepository;
import com.cuckoo.BackendServer.securitysettings.JwtUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class LoginService implements UserDetailsService {

    @Autowired
    private JwtUtil jwtService;

    @Autowired
    private LoginRepository dbAPI;

    @Override
    public UserType loadUserByUsername(String username) throws UsernameNotFoundException {
        return dbAPI.getUserByUsername(username);
    }

    public UserType loadUserById(String id) throws UsernameNotFoundException {
        return dbAPI.getUserById(id);
    }

    /**
     * returns all the info of the user but password hash
     * @param username 
     * @return UserType
     */
    public UserType getUserInfo(String username) {
        if (username == null)
            throw new UsernameEmptyException("Username can not be null for getUserInfo at service");

        return this.dbAPI.getUserInfo(username);
    }

    public String createJwtToken(String username) {
        UserType user = this.dbAPI.getUserInfo(username);
        JwtHolder jwt = new JwtHolder(this.jwtService.generateToken(user));
        return jwt.getJwt();
    }
}