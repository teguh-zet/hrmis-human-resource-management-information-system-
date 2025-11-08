package com.jasamedika.hrmis.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InitDataRequest {
    @NotBlank(message = "Nama admin tidak boleh kosong")
    private String namaAdmin;
    
    @NotBlank(message = "Nama perusahaan tidak boleh kosong")
    private String perusahaan;
}

