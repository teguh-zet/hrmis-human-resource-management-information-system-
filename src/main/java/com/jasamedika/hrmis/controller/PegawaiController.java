package com.jasamedika.hrmis.controller;

import com.jasamedika.hrmis.dto.*;
import com.jasamedika.hrmis.service.PegawaiService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/pegawai")
@Tag(name = "Pegawai", description = "API untuk manajemen data pegawai")
@SecurityRequirement(name = "Bearer Authentication")
public class PegawaiController {

    @Autowired
    private PegawaiService pegawaiService;

    @GetMapping("/combo/jabatan")
    @Operation(summary = "Combo Jabatan", description = "Mendapatkan daftar semua jabatan untuk combo box")
    public ResponseEntity<?> getComboJabatan() {
        try {
            List<ComboDto> result = pegawaiService.getComboJabatan();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Terjadi kesalahan saat mengambil data jabatan: " + e.getMessage());
        }
    }

    @GetMapping("/combo/departemen")
    public ResponseEntity<?> getComboDepartemen() {
        try {
            List<ComboDto> result = pegawaiService.getComboDepartemen();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Terjadi kesalahan saat mengambil data departemen: " + e.getMessage());
        }
    }

    @GetMapping("/combo/unit-kerja")
    public ResponseEntity<?> getComboUnitKerja() {
        try {
            List<ComboDto> result = pegawaiService.getComboUnitKerja();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Terjadi kesalahan saat mengambil data unit kerja: " + e.getMessage());
        }
    }

    @GetMapping("/combo/pendidikan")
    public ResponseEntity<?> getComboPendidikan() {
        try {
            List<ComboDto> result = pegawaiService.getComboPendidikan();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Terjadi kesalahan saat mengambil data pendidikan: " + e.getMessage());
        }
    }

    @GetMapping("/combo/jenis-kelamin")
    public ResponseEntity<?> getComboJenisKelamin() {
        try {
            List<ComboDto> result = pegawaiService.getComboJenisKelamin();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Terjadi kesalahan saat mengambil data jenis kelamin: " + e.getMessage());
        }
    }

    @GetMapping("/combo/departemen-hrd")
    public ResponseEntity<?> getComboDepartemenHrd() {
        try {
            List<ComboDto> result = pegawaiService.getComboDepartemenHrd();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Terjadi kesalahan saat mengambil data pegawai HRD: " + e.getMessage());
        }
    }

    @GetMapping("/daftar")
    @Operation(summary = "Daftar Pegawai", description = "Mendapatkan daftar semua pegawai. Hanya Admin atau pegawai HRD yang dapat mengakses.")
    public ResponseEntity<?> getDaftarPegawai() {
        try {
            List<UserInfoDto> result = pegawaiService.getDaftarPegawai();
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

    @GetMapping("/search")
    @Operation(summary = "Search dan Filter Pegawai dengan Pagination", description = "Mencari dan memfilter daftar pegawai dengan pagination. Hanya Admin atau pegawai HRD yang dapat mengakses.")
    public ResponseEntity<?> searchAndFilterPegawai(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer kdJabatan,
            @RequestParam(required = false) Integer kdDepartemen,
            @RequestParam(required = false) Integer kdUnitKerja,
            @RequestParam(required = false) Integer kdPendidikan,
            @RequestParam(required = false) Integer kdJenisKelamin,
            @RequestParam(required = false) String profile,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "namaLengkap") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        try {
            SearchFilterDto filter = new SearchFilterDto();
            filter.setKeyword(keyword);
            filter.setKdJabatan(kdJabatan);
            filter.setKdDepartemen(kdDepartemen);
            filter.setKdUnitKerja(kdUnitKerja);
            filter.setKdPendidikan(kdPendidikan);
            filter.setKdJenisKelamin(kdJenisKelamin);
            filter.setProfile(profile);
            filter.setPage(page);
            filter.setSize(size);
            filter.setSortBy(sortBy);
            filter.setSortDir(sortDir);
            
            PageResponseDto<UserInfoDto> result = pegawaiService.searchAndFilterPegawai(filter);
            return ResponseEntity.ok(result);
        } catch (org.springframework.web.server.ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    @PostMapping("/admin-tambah-pegawai")
    @Operation(summary = "Tambah Pegawai", description = "Menambahkan pegawai baru. Hanya Admin atau pegawai HRD yang dapat mengakses.")
    public ResponseEntity<?> tambahPegawai(@ModelAttribute TambahPegawaiRequest request) {
        try {
            pegawaiService.tambahPegawai(request);
            return ResponseEntity.ok("Pegawai berhasil ditambahkan");
        } catch (org.springframework.web.server.ResponseStatusException e) {
            // Error dari API akan dihandle oleh GlobalExceptionHandler dan return 501
            throw e;
        } catch (Exception e) {
            // Error dari bugs akan return 500 dengan pesan mesin
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    @PostMapping("/admin-ubah-pegawai")
    public ResponseEntity<?> ubahPegawai(@ModelAttribute UbahPegawaiRequest request) {
        try {
            pegawaiService.ubahPegawai(request);
            return ResponseEntity.ok("Pegawai berhasil diubah");
        } catch (org.springframework.web.server.ResponseStatusException e) {
            // Error dari API akan dihandle oleh GlobalExceptionHandler dan return 501
            throw e;
        } catch (Exception e) {
            // Error dari bugs akan return 500 dengan pesan mesin
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    @PostMapping("/admin-ubah-photo")
    public ResponseEntity<?> ubahPhotoPegawai(
            @RequestParam("idUser") String idUser,
            @RequestParam("namaFile") String namaFile,
            @RequestParam("files") MultipartFile files) {
        try {
            pegawaiService.ubahPhotoPegawai(idUser, namaFile, files);
            return ResponseEntity.ok("Photo pegawai berhasil diubah");
        } catch (org.springframework.web.server.ResponseStatusException e) {
            // Error dari API akan dihandle oleh GlobalExceptionHandler dan return 501
            throw e;
        } catch (Exception e) {
            // Error dari bugs akan return 500 dengan pesan mesin
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    @PostMapping("/ubah-photo")
    public ResponseEntity<?> ubahPhotoSendiri(
            @RequestParam("namaFile") String namaFile,
            @RequestParam("files") MultipartFile files) {
        try {
            pegawaiService.ubahPhotoSendiri(namaFile, files);
            return ResponseEntity.ok("Photo berhasil diubah");
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

