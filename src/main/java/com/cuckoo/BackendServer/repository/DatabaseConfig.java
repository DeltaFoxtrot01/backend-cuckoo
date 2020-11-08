package com.cuckoo.BackendServer.repository;

import com.cuckoo.BackendServer.repository.postgresdb.PostgresDBAPI;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DatabaseConfig{

    @Bean
    @Primary
    public DatabaseAPI databaseApi(){
        return new PostgresDBAPI();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

}