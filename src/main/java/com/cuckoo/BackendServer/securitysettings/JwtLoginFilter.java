package com.cuckoo.BackendServer.securitysettings;

import java.io.IOException;
import java.util.ArrayList;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.cuckoo.BackendServer.models.usertype.UserType;
import com.cuckoo.BackendServer.service.LoginService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Filter that allows the login to be made
 * 
 */
public class JwtLoginFilter extends UsernamePasswordAuthenticationFilter {
    private AuthenticationManager authenticationManager;
    private LoginService loginService;

    public JwtLoginFilter(AuthenticationManager authManager, String url, LoginService loginService) {
        this.authenticationManager = authManager;
        this.setFilterProcessesUrl(url);
        this.loginService = loginService;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
        throws AuthenticationException {
        UserType user;

        try {
            user = new ObjectMapper().readValue(request.getInputStream(), UserType.class);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                user.getEmail(), user.getPassword(), new ArrayList<>());
            return this.authenticationManager.authenticate(authenticationToken);
        } catch (InternalAuthenticationServiceException e) {
            response.setStatus(403);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
            Authentication authResult) throws IOException, ServletException {
        
        UserType user = (UserType) authResult.getPrincipal();
        String token = "Bearer ";
        token = token + this.loginService.createJwtToken(user.getUsername());
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Authorization", token);
        chain.doFilter(request, response);
    }
}