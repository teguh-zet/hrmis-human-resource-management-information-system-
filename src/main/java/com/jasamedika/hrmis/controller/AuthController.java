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

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "API untuk autentikasi dan manajemen user")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/init-data")
    @Operation(summary = "Inisialisasi data awal", description = "Membuat data perusahaan dan admin pertama kali. Hanya bisa dilakukan sekali.")
    public ResponseEntity<?> initData(@ModelAttribute InitDataRequest request) {
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
    public ResponseEntity<?> login(@ModelAttribute LoginRequest request) {
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
            @ModelAttribute UbahPasswordRequest request,
            Authentication authentication) {
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
}

