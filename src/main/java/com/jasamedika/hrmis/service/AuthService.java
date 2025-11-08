package com.jasamedika.hrmis.service;

import com.jasamedika.hrmis.dto.*;
import com.jasamedika.hrmis.entity.*;
import com.jasamedika.hrmis.repository.*;
import com.jasamedika.hrmis.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PerusahaanRepository perusahaanRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private PegawaiService pegawaiService;

    @Transactional
    public InitDataResponse initData(InitDataRequest request) {
        // Check if already initialized
        if (userRepository.count() > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Data sudah diinisialisasi sebelumnya");
        }

        // Create perusahaan
        Perusahaan perusahaan = new Perusahaan();
        perusahaan.setNamaPerusahaan(request.getPerusahaan());
        perusahaanRepository.save(perusahaan);

        // Generate admin credentials
        String email = "admin@" + request.getPerusahaan().toLowerCase().replaceAll("\\s+", "") + ".com";
        String password = generateRandomPassword();
        String idUser = UUID.randomUUID().toString();

        // Create admin user
        User admin = new User();
        admin.setIdUser(idUser);
        admin.setProfile("Admin");
        admin.setNamaLengkap(request.getNamaAdmin());
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setKdJabatan(1); // Default admin jabatan
        admin.setKdDepartemen(1); // Default admin departemen
        userRepository.save(admin);

        InitDataResponse response = new InitDataResponse();
        response.setEmail(email);
        response.setPassword(password);
        response.setProfile("Admin");
        return response;
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                    "Email atau password salah"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                "Email atau password salah");
        }

        if (!user.getProfile().equals(request.getProfile())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                "Profile tidak sesuai");
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getIdUser(), user.getProfile());
        UserInfoDto userInfo = pegawaiService.mapToUserInfoDto(user);

        LoginResponse response = new LoginResponse();
        LoginResponse.Hasil hasil = new LoginResponse.Hasil();
        hasil.setToken(token);
        hasil.setInfo(userInfo);
        response.setHasil(hasil);

        return response;
    }

    @Transactional
    public void ubahPasswordSendiri(String email, UbahPasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "User tidak ditemukan"));

        if (!passwordEncoder.matches(request.getPasswordAsli(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Password asli tidak sesuai");
        }

        if (!request.getPasswordBaru1().equals(request.getPasswordBaru2())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Password baru tidak sama");
        }

        user.setPassword(passwordEncoder.encode(request.getPasswordBaru1()));
        userRepository.save(user);
    }

    private String generateRandomPassword() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}

