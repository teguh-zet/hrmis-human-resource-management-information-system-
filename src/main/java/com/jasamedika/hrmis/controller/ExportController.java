package com.jasamedika.hrmis.controller;

import com.jasamedika.hrmis.service.ExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;

@RestController
@RequestMapping("/api/export")
@Tag(name = "Export", description = "API untuk Export Data ke Excel")
public class ExportController {

    @Autowired
    private ExportService exportService;

    @GetMapping("/pegawai/excel")
    @Operation(summary = "Export Daftar Pegawai ke Excel", description = "Export daftar pegawai ke format Excel. Hanya Admin yang dapat mengakses. Memerlukan autentikasi JWT.")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> exportPegawaiToExcel() {
        try {
            byte[] excelData = exportService.exportPegawaiToExcel();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "daftar_pegawai.xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    @GetMapping("/presensi/excel")
    @Operation(summary = "Export Daftar Presensi ke Excel", description = "Export daftar presensi ke format Excel berdasarkan range tanggal. Hanya Admin yang dapat mengakses. Memerlukan autentikasi JWT.")
    @SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> exportPresensiToExcel(
            @RequestParam(required = false) Long tglAwal,
            @RequestParam(required = false) Long tglAkhir) {
        try {
            // Default: bulan ini jika tidak ada parameter
            if (tglAwal == null || tglAkhir == null) {
                LocalDate today = LocalDate.now(ZoneId.systemDefault());
                LocalDate firstDay = today.withDayOfMonth(1);
                LocalDate lastDay = today.withDayOfMonth(today.lengthOfMonth());
                tglAwal = firstDay.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
                tglAkhir = lastDay.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond() - 1;
            }
            
            byte[] excelData = exportService.exportPresensiToExcel(tglAwal, tglAkhir);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "daftar_presensi.xlsx");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(excelData);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
}

