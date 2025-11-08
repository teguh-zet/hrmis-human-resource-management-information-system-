package com.jasamedika.hrmis.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UbahPhotoRequest {
    private String idUser; // Optional, untuk admin ubah photo pegawai
    private String namaFile;
    private MultipartFile files;
}

