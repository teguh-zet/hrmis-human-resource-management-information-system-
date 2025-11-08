package com.jasamedika.hrmis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComboDto {
    private Integer kdJabatan;
    private String namaJabatan;
    private Integer kdDepartemen;
    private String namaDepartemen;
    private Integer kdUnitKerja;
    private String namaUnitKerja;
    private Integer kdJenisKelamin;
    private String namaJenisKelamin;
    private Integer kdPendidikan;
    private String namaPendidikan;
    private String namaLengkap; // Untuk combo departemen HRD
}

