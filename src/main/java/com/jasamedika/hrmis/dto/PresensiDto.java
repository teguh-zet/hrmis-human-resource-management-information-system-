package com.jasamedika.hrmis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresensiDto {
    private String idUser;
    private String namaLengkap;
    private Long tglAbsensi; // Epoch in seconds
    private String jamMasuk;
    private String jamKeluar;
    private String namaStatus;
}

