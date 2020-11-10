package com.cuckoo.BackendServer.securitysettings;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cuckoo.BackendServer.service.LoginService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;


/*
Extra spring security filter to add a verification filter for the JWT
*/

public class JwtRequestFilter extends BasicAuthenticationFilter {
    
    private LoginService loginService;
    
    private JwtUtil jwtUtil;
    
    public JwtRequestFilter(AuthenticationManager authenticationManager,LoginService loginService, JwtUtil jwtUtil) {
        super(authenticationManager);
        this.loginService = loginService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String username = null;
        String jwt = null;
        String authContent = null;
        

        authContent = request.getHeader("Authorization");
        if(authContent == null){
          filterChain.doFilter(request, response);
          return ;
        }
        if(authContent.startsWith("Bearer ")){
          jwt = authContent.substring(7);
        }
        else{
          response.setStatus(400);
        }
        if(jwt == null){
          filterChain.doFilter(request, response);
          return ;
        }

        username = jwtUtil.extractUsername(jwt);

        if(username != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails = this.loginService.loadUserByUsername(username);
            if(this.jwtUtil.validateToken(jwt, userDetails)){
                UsernamePasswordAuthenticationToken usernamePassAuthToken = 
                    new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
                usernamePassAuthToken
                    .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePassAuthToken); 
            }
        }

        
        filterChain.doFilter(request,response);
    }
}