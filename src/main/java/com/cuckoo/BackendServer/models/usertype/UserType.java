package com.cuckoo.BackendServer.models.usertype;

import java.util.Collection;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
/* 
User from the database
Used only for service purposes
*/

@JsonIgnoreProperties(ignoreUnknown = true)
public class UserType implements UserDetails {

    private UUID id;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    
    /*should use only this method inside and outside the class to set the password*/
    
    public UserType(){
      this.id = null;
      this.email = null;
      this.password = null;
      this.firstName = null;
      this.lastName = null;
    }
    
    public UserType(String username, String password){
        this.email = username;
        this.password = password;
    }

    public UserType(String email, String password, String firstName, String lastName){
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UserType(UUID id, String email, String password, String firstName, String lastName){
        this.id = id;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public String getUsername() {
      if(this.id == null)
        return null;
      return this.id.toString(); 
    }
    @Override
    public String getPassword() { return this.password; }
    
    @Override
    public boolean isAccountNonExpired() {
        // TODO Auto-generated method stub
        return true;
    }
    
    @Override
    public boolean isAccountNonLocked() {
        // TODO Auto-generated method stub
        return true;
    }
    
    @Override
    public boolean isCredentialsNonExpired() {
        // TODO Auto-generated method stub
        return true;
    }
    
    @Override
    public boolean isEnabled() {
        // TODO Auto-generated method stub
        return true;
    }
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getFirstName() { return this.firstName; }
    public String getLastName() { return this.lastName; }
    public String getEmail() { return this.email; }
    
    public void setUsername(UUID id) {this.id = id;}
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
}