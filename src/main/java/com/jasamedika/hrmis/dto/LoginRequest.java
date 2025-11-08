package com.jasamedika.hrmis.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String email;
    private String password;
    private String profile;
}

