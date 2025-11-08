package com.jasamedika.hrmis.service;

import com.jasamedika.hrmis.dto.PresensiDto;
import com.jasamedika.hrmis.dto.StatusAbsenDto;
import com.jasamedika.hrmis.entity.*;
import com.jasamedika.hrmis.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PresensiService {

    @Autowired
    private PresensiRepository presensiRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StatusAbsenRepository statusAbsenRepository;

    public List<StatusAbsenDto> getComboStatusAbsen(Long tglAwal, Long tglAkhir) {
        return statusAbsenRepository.findAll().stream()
                .map(s -> {
                    StatusAbsenDto dto = new StatusAbsenDto();
                    dto.setKdStatus(s.getKdStatus());
                    dto.setNamaStatus(s.getNamaStatus());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<PresensiDto> getDaftarPresensiAdmin(Long tglAwal, Long tglAkhir) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                    "User tidak ditemukan"));

        if (!currentUser.getProfile().equals("Admin")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "Hanya Admin yang dapat mengakses");
        }

        List<Presensi> presensiList = presensiRepository.findByDateRange(tglAwal, tglAkhir);
        return presensiList.stream()
                .map(this::mapToPresensiDto)
                .collect(Collectors.toList());
    }

    public List<PresensiDto> getDaftarPresensiPegawai(Long tglAwal, Long tglAkhir) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                    "User tidak ditemukan"));

        List<Presensi> presensiList = presensiRepository.findByUserAndDateRange(
                currentUser.getIdUser(), tglAwal, tglAkhir);
        return presensiList.stream()
                .map(this::mapToPresensiDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public PresensiDto checkIn() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                    "User tidak ditemukan"));

        Long todayEpoch = Instant.now().getEpochSecond();
        LocalTime now = LocalTime.now(ZoneId.systemDefault());
        String jamMasuk = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        Presensi existing = presensiRepository.findByUserAndTglAbsensi(
                user.getIdUser(), todayEpoch).orElse(null);

        if (existing != null && existing.getJamMasuk() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Anda sudah melakukan check-in hari ini");
        }

        Presensi presensi;
        if (existing != null) {
            presensi = existing;
        } else {
            presensi = new Presensi();
            presensi.setUser(user);
            presensi.setTglAbsensi(todayEpoch);
        }

        presensi.setJamMasuk(jamMasuk);
        presensi = presensiRepository.save(presensi);

        PresensiDto dto = new PresensiDto();
        dto.setIdUser(user.getIdUser());
        dto.setNamaLengkap(user.getNamaLengkap());
        dto.setTglAbsensi(presensi.getTglAbsensi());
        dto.setJamMasuk(presensi.getJamMasuk());
        dto.setJamKeluar(presensi.getJamKeluar());
        if (presensi.getKdStatus() != null) {
            statusAbsenRepository.findById(presensi.getKdStatus())
                    .ifPresent(s -> dto.setNamaStatus(s.getNamaStatus()));
        }
        return dto;
    }

    @Transactional
    public PresensiDto checkOut() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                    "User tidak ditemukan"));

        Long todayEpoch = Instant.now().getEpochSecond();
        LocalTime now = LocalTime.now(ZoneId.systemDefault());
        String jamKeluar = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        Presensi presensi = presensiRepository.findByUserAndTglAbsensi(
                user.getIdUser(), todayEpoch)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Anda belum melakukan check-in hari ini"));

        if (presensi.getJamKeluar() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Anda sudah melakukan check-out hari ini");
        }

        presensi.setJamKeluar(jamKeluar);
        presensi = presensiRepository.save(presensi);

        PresensiDto dto = new PresensiDto();
        dto.setIdUser(user.getIdUser());
        dto.setNamaLengkap(user.getNamaLengkap());
        dto.setTglAbsensi(presensi.getTglAbsensi());
        dto.setJamMasuk(presensi.getJamMasuk());
        dto.setJamKeluar(presensi.getJamKeluar());
        if (presensi.getKdStatus() != null) {
            statusAbsenRepository.findById(presensi.getKdStatus())
                    .ifPresent(s -> dto.setNamaStatus(s.getNamaStatus()));
        }
        return dto;
    }

    @Transactional
    public void absensi(Long tglAbsensi, Integer kdStatus) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                    "User tidak ditemukan"));

        Presensi presensi = presensiRepository.findByUserAndTglAbsensi(
                user.getIdUser(), tglAbsensi).orElse(null);

        if (presensi == null) {
            presensi = new Presensi();
            presensi.setUser(user);
            presensi.setTglAbsensi(tglAbsensi);
        }

        presensi.setKdStatus(kdStatus);
        presensiRepository.save(presensi);
    }

    private PresensiDto mapToPresensiDto(Presensi presensi) {
        PresensiDto dto = new PresensiDto();
        dto.setIdUser(presensi.getUser().getIdUser());
        dto.setNamaLengkap(presensi.getUser().getNamaLengkap());
        dto.setTglAbsensi(presensi.getTglAbsensi());
        dto.setJamMasuk(presensi.getJamMasuk());
        dto.setJamKeluar(presensi.getJamKeluar());
        
        if (presensi.getKdStatus() != null) {
            statusAbsenRepository.findById(presensi.getKdStatus())
                    .ifPresent(s -> dto.setNamaStatus(s.getNamaStatus()));
        }
        
        return dto;
    }
}

