package com.jasamedika.hrmis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsDto {
    private Long totalPegawai;
    private Long totalPresensiHariIni;
    private Long totalPresensiBulanIni;
    private Long totalAbsenBulanIni;
    private Double rataRataJamMasuk;
    private Double rataRataJamKeluar;
    private List<Map<String, Object>> presensiPerHari; // [{tanggal, jumlah}]
    private List<Map<String, Object>> presensiPerDepartemen; // [{departemen, jumlah}]
    private List<Map<String, Object>> statusAbsenStatistik; // [{status, jumlah}]
    private Long pegawaiMasukHariIni;
    private Long pegawaiTidakMasukHariIni;
}

