package com.cuckoo.BackendServer.securitysettings;

import java.io.IOException;
import io.jsonwebtoken.SignatureException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cuckoo.BackendServer.models.usertype.UserType;
import com.cuckoo.BackendServer.service.LoginService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
        String id;
        String jwt = null;
        String authContent;

        authContent = request.getHeader("Authorization");
        if (authContent == null) {
            filterChain.doFilter(request, response);
            return ;
        }

        if (authContent.startsWith("Bearer ")) {
            jwt = authContent.substring(7);
        } else {
            response.setStatus(400);
        }

        if (jwt == null) {
            filterChain.doFilter(request, response);
            return ;
        }

        try {
            id = jwtUtil.extractId(jwt);
        } catch (SignatureException e) {
            filterChain.doFilter(request, response);
            return ;
        }

        if (id != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserType userType = this.loginService.loadUserById(id);

            if (this.jwtUtil.validateToken(jwt, userType)) {
                UsernamePasswordAuthenticationToken usernamePassAuthToken =
                        new UsernamePasswordAuthenticationToken(userType, null, userType.getAuthorities());

                usernamePassAuthToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(usernamePassAuthToken); 
            }
        }
        response.setHeader("token", authContent);
        filterChain.doFilter(request,response);
    }
}