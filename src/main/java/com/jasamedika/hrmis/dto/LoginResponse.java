package com.jasamedika.hrmis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Hasil hasil;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Hasil {
        private String token;
        private UserInfoDto info;
    }
}

