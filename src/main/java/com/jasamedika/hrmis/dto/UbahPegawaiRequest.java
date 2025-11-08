package com.jasamedika.hrmis.dto;

import lombok.Data;

@Data
public class UbahPegawaiRequest {
    private String idUser;
    private String namaLengkap;
    private String email;
    private String tempatLahir;
    private Long tanggalLahir; // Epoch in seconds
    private Integer kdJenisKelamin;
    private Integer kdPendidikan;
    private Integer kdJabatan;
    private Integer kdDepartemen;
    private Integer kdUnitKerja;
    private String password;
    private String passwordC;
}

