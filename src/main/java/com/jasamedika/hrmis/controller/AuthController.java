package com.jasamedika.hrmis.controller;

import com.jasamedika.hrmis.dto.*;
import com.jasamedika.hrmis.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import com.jasamedika.hrmis.service.AuditLogService;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API untuk autentikasi dan manajemen user")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private AuditLogService auditLogService;

    @PostMapping("/init-data")
    @Operation(summary = "Inisialisasi data awal", description = "Membuat data perusahaan dan admin pertama kali. Hanya bisa dilakukan sekali.")
    public ResponseEntity<?> initData(
            @RequestParam(value = "namaAdmin", required = false) String namaAdmin,
            @RequestParam(value = "perusahaan", required = false) String perusahaan) {
        // Validasi manual untuk memastikan data tidak null
        if (namaAdmin == null || namaAdmin.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Nama admin tidak boleh kosong");
        }
        if (perusahaan == null || perusahaan.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Nama perusahaan tidak boleh kosong");
        }
        
        // Create request object
        InitDataRequest request = new InitDataRequest();
        request.setNamaAdmin(namaAdmin.trim());
        request.setPerusahaan(perusahaan.trim());
        try {
            InitDataResponse response = authService.initData(request);
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

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Login dengan email, password, dan profile. Mengembalikan JWT token untuk autentikasi request selanjutnya.")
    public ResponseEntity<?> login(
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "profile", required = false) String profile) {
        // Validasi
        if (email == null || email.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Email tidak boleh kosong");
        }
        if (password == null || password.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Password tidak boleh kosong");
        }
        if (profile == null || profile.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Profile tidak boleh kosong");
        }
        
        // Create request object
        LoginRequest request = new LoginRequest();
        request.setEmail(email.trim());
        request.setPassword(password.trim());
        request.setProfile(profile.trim());
        try {
            LoginResponse response = authService.login(request);
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

    @PostMapping("/ubah-password-sendiri")
    @Operation(summary = "Ubah password sendiri", description = "User dapat mengubah password sendiri. Memerlukan autentikasi JWT.")
    public ResponseEntity<?> ubahPasswordSendiri(
            @RequestParam(value = "passwordAsli", required = false) String passwordAsli,
            @RequestParam(value = "passwordBaru1", required = false) String passwordBaru1,
            @RequestParam(value = "passwordBaru2", required = false) String passwordBaru2,
            Authentication authentication) {
        // Validasi
        if (passwordAsli == null || passwordAsli.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Password asli tidak boleh kosong");
        }
        if (passwordBaru1 == null || passwordBaru1.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Password baru tidak boleh kosong");
        }
        if (passwordBaru2 == null || passwordBaru2.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Konfirmasi password baru tidak boleh kosong");
        }
        
        // Create request object
        UbahPasswordRequest request = new UbahPasswordRequest();
        request.setPasswordAsli(passwordAsli.trim());
        request.setPasswordBaru1(passwordBaru1.trim());
        request.setPasswordBaru2(passwordBaru2.trim());
        
        try {
            String email = authentication.getName();
            authService.ubahPasswordSendiri(email, request);
            return ResponseEntity.ok("Password berhasil diubah");
        } catch (org.springframework.web.server.ResponseStatusException e) {
            // Error dari API akan dihandle oleh GlobalExceptionHandler dan return 501
            throw e;
        } catch (Exception e) {
            // Error dari bugs akan return 500 dengan pesan mesin
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + e.getClass().getSimpleName() + " - " + e.getMessage());
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user info", description = "Mendapatkan informasi user yang sedang login. Memerlukan autentikasi JWT.")
    @io.swagger.v3.oas.annotations.security.SecurityRequirement(name = "Bearer Authentication")
    public ResponseEntity<?> getCurrentUserInfo(Authentication authentication) {
        try {
            String email = authentication.getName();
            UserInfoDto userInfo = authService.getCurrentUserInfo(email);
            return ResponseEntity.ok(userInfo);
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

