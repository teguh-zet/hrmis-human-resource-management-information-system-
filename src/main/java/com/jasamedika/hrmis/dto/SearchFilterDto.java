package com.jasamedika.hrmis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchFilterDto {
    private String keyword; // Untuk pencarian nama, email, dll
    private Integer kdJabatan;
    private Integer kdDepartemen;
    private Integer kdUnitKerja;
    private Integer kdPendidikan;
    private Integer kdJenisKelamin;
    private String profile;
    private Long tglAwal;
    private Long tglAkhir;
    private Integer page = 0;
    private Integer size = 10;
    private String sortBy = "namaLengkap";
    private String sortDir = "ASC";
}

