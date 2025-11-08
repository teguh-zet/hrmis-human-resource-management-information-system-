package com.jasamedika.hrmis.controller;

import com.jasamedika.hrmis.dto.PresensiAbsensiRequest;
import com.jasamedika.hrmis.dto.PresensiDto;
import com.jasamedika.hrmis.dto.StatusAbsenDto;
import com.jasamedika.hrmis.service.PresensiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/presensi")
@Tag(name = "Presensi", description = "API untuk presensi dan absensi pegawai")
@SecurityRequirement(name = "Bearer Authentication")
public class PresensiController {

    @Autowired
    private PresensiService presensiService;

    @GetMapping("/combo/status-absen")
    public ResponseEntity<?> getComboStatusAbsen(
            @RequestParam("tglAwal") Long tglAwal,
            @RequestParam("tglAkhir") Long tglAkhir) {
        try {
            List<StatusAbsenDto> result = presensiService.getComboStatusAbsen(tglAwal, tglAkhir);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Terjadi kesalahan saat mengambil data status absen: " + e.getMessage());
        }
    }

    @GetMapping("/daftar/admin")
    public ResponseEntity<?> getDaftarPresensiAdmin(
            @RequestParam("tglAwal") Long tglAwal,
            @RequestParam("tglAkhir") Long tglAkhir) {
        try {
            List<PresensiDto> result = presensiService.getDaftarPresensiAdmin(tglAwal, tglAkhir);
            return ResponseEntity.ok(result);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            // Error dari API akan dihandle oleh GlobalExceptionHandler dan return 501
            throw e;
        } catch (Exception e) {
            // Error dari bugs akan return 500 dengan pesan mesin
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    @GetMapping("/daftar/pegawai")
    public ResponseEntity<?> getDaftarPresensiPegawai(
            @RequestParam("tglAwal") Long tglAwal,
            @RequestParam("tglAkhir") Long tglAkhir) {
        try {
            List<PresensiDto> result = presensiService.getDaftarPresensiPegawai(tglAwal, tglAkhir);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Terjadi kesalahan saat mengambil daftar presensi: " + e.getMessage());
        }
    }

    @GetMapping("/in")
    @Operation(summary = "Check In", description = "Melakukan check-in. Waktu diambil dari server. Tidak bisa check-in 2 kali dalam sehari.")
    public ResponseEntity<?> checkIn() {
        try {
            PresensiDto result = presensiService.checkIn();
            Map<String, String> response = new HashMap<>();
            response.put("jamMasuk", result.getJamMasuk());
            return ResponseEntity.ok(response);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            // Error dari API akan dihandle oleh GlobalExceptionHandler dan return 501
            throw e;
        } catch (Exception e) {
            // Error dari bugs akan return 500 dengan pesan mesin
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    @GetMapping("/out")
    @Operation(summary = "Check Out", description = "Melakukan check-out. Harus sudah check-in terlebih dahulu. Waktu diambil dari server.")
    public ResponseEntity<?> checkOut() {
        try {
            PresensiDto result = presensiService.checkOut();
            Map<String, String> response = new HashMap<>();
            response.put("jamKeluar", result.getJamKeluar());
            return ResponseEntity.ok(response);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            // Error dari API akan dihandle oleh GlobalExceptionHandler dan return 501
            throw e;
        } catch (Exception e) {
            // Error dari bugs akan return 500 dengan pesan mesin
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    @PostMapping("/abseni")
    @Operation(summary = "Lapor Absensi/Izin", description = "Mencatat absensi dengan status (Izin, Sakit, Cuti, dll). Tanggal dalam format Epoch (detik).")
    public ResponseEntity<?> absensi(@ModelAttribute PresensiAbsensiRequest request) {
        try {
            presensiService.absensi(request.getTglAbsensi(), request.getKdStatus());
            return ResponseEntity.ok("Absensi berhasil direkam");
        } catch (org.springframework.web.server.ResponseStatusException e) {
            // Error dari API akan dihandle oleh GlobalExceptionHandler dan return 501
            throw e;
        } catch (Exception e) {
            // Error dari bugs akan return 500 dengan pesan mesin
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }
}

