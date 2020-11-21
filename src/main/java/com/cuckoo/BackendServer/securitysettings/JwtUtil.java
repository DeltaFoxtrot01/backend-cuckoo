package com.cuckoo.BackendServer.securitysettings;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import com.cuckoo.BackendServer.models.usertype.UserType;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/*
Service for the JWT generation and verification
*/

@Service
public class JwtUtil{


    @Value("${jwt.key}")
    private String SECRET_KEY;
    
    public String extractId(String token){
        return this.extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token){
        return this.extractClaim(token, Claims::getExpiration);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = this.extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token){
        return Jwts.parser().setSigningKey(this.SECRET_KEY).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token){
        return this.extractExpiration(token).before(new Date());
    }

    private String createToken(Map<String, Object> claims, String subject){
        return Jwts.builder()
                   .setClaims(claims)
                   .setSubject(subject)
                   .setIssuedAt(new Date(System.currentTimeMillis()))
                   .setExpiration(new Date(System.currentTimeMillis() + 1000*60*60*10))
                   .signWith(SignatureAlgorithm.HS256, this.SECRET_KEY)
                   .compact();
    }
    
    public String generateToken(UserType userType){
        Map<String, Object> claims = new HashMap<>();
        return this.createToken(claims, userType.getId().toString());
    }

    public boolean validateToken(String token, UserType userType){
        final String id = this.extractId(token);
        return (id.equals(userType.getId().toString()) && !this.isTokenExpired(token));
    }
}