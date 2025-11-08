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

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        
        // Normalisasi dan gabungkan presensi yang duplikat (sama user dan tanggal)
        // Group by user dan tanggal (normalized to start of day)
        Map<String, Presensi> mergedMap = new HashMap<>();
        for (Presensi presensi : presensiList) {
            // Normalisasi tglAbsensi ke start of day
            LocalDate presensiDate = LocalDate.ofInstant(
                java.time.Instant.ofEpochSecond(presensi.getTglAbsensi()), 
                ZoneId.systemDefault()
            );
            Long normalizedTglAbsensi = presensiDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            
            String key = presensi.getUser().getIdUser() + "_" + normalizedTglAbsensi;
            Presensi existing = mergedMap.get(key);
            
            if (existing == null) {
                // Set normalized epoch
                presensi.setTglAbsensi(normalizedTglAbsensi);
                mergedMap.put(key, presensi);
            } else {
                // Merge data: gabungkan semua data yang ada, prioritaskan yang lebih lengkap
                // Jam Masuk: ambil yang ada (tidak kosong)
                if (presensi.getJamMasuk() != null && !presensi.getJamMasuk().trim().isEmpty()) {
                    if (existing.getJamMasuk() == null || existing.getJamMasuk().trim().isEmpty()) {
                        existing.setJamMasuk(presensi.getJamMasuk());
                    }
                }
                // Jam Keluar: ambil yang ada (tidak kosong)
                if (presensi.getJamKeluar() != null && !presensi.getJamKeluar().trim().isEmpty()) {
                    if (existing.getJamKeluar() == null || existing.getJamKeluar().trim().isEmpty()) {
                        existing.setJamKeluar(presensi.getJamKeluar());
                    }
                }
                // Status: ambil yang ada (prioritaskan yang sudah ada, tapi jika tidak ada ambil yang baru)
                if (presensi.getKdStatus() != null) {
                    if (existing.getKdStatus() == null) {
                        existing.setKdStatus(presensi.getKdStatus());
                    }
                }
            }
        }
        
        return mergedMap.values().stream()
                .map(this::mapToPresensiDto)
                .sorted((a, b) -> {
                    // Sort by tanggal descending, then by nama
                    int dateCompare = Long.compare(b.getTglAbsensi(), a.getTglAbsensi());
                    if (dateCompare != 0) return dateCompare;
                    return a.getNamaLengkap().compareTo(b.getNamaLengkap());
                })
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
        
        // Normalisasi dan gabungkan presensi yang duplikat (sama tanggal)
        Map<Long, Presensi> mergedMap = new HashMap<>();
        for (Presensi presensi : presensiList) {
            // Normalisasi tglAbsensi ke start of day
            LocalDate presensiDate = LocalDate.ofInstant(
                java.time.Instant.ofEpochSecond(presensi.getTglAbsensi()), 
                ZoneId.systemDefault()
            );
            Long normalizedTglAbsensi = presensiDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            
            Presensi existing = mergedMap.get(normalizedTglAbsensi);
            
            if (existing == null) {
                // Set normalized epoch
                presensi.setTglAbsensi(normalizedTglAbsensi);
                mergedMap.put(normalizedTglAbsensi, presensi);
            } else {
                // Merge data: gabungkan semua data yang ada, prioritaskan yang lebih lengkap
                // Jam Masuk: ambil yang ada (tidak kosong)
                if (presensi.getJamMasuk() != null && !presensi.getJamMasuk().trim().isEmpty()) {
                    if (existing.getJamMasuk() == null || existing.getJamMasuk().trim().isEmpty()) {
                        existing.setJamMasuk(presensi.getJamMasuk());
                    }
                }
                // Jam Keluar: ambil yang ada (tidak kosong)
                if (presensi.getJamKeluar() != null && !presensi.getJamKeluar().trim().isEmpty()) {
                    if (existing.getJamKeluar() == null || existing.getJamKeluar().trim().isEmpty()) {
                        existing.setJamKeluar(presensi.getJamKeluar());
                    }
                }
                // Status: ambil yang ada (prioritaskan yang sudah ada, tapi jika tidak ada ambil yang baru)
                if (presensi.getKdStatus() != null) {
                    if (existing.getKdStatus() == null) {
                        existing.setKdStatus(presensi.getKdStatus());
                    }
                }
            }
        }
        
        return mergedMap.values().stream()
                .map(this::mapToPresensiDto)
                .sorted((a, b) -> Long.compare(b.getTglAbsensi(), a.getTglAbsensi()))
                .collect(Collectors.toList());
    }

    @Transactional
    public PresensiDto checkIn() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                    "User tidak ditemukan"));

        // Gunakan start of day untuk epoch agar konsisten
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        Long todayEpoch = today.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        LocalTime now = LocalTime.now(ZoneId.systemDefault());
        String jamMasuk = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        // Cari presensi yang sudah ada untuk hari ini
        // Coba cari dengan epoch exact dulu
        Presensi existing = presensiRepository.findByUserAndTglAbsensi(
                user.getIdUser(), todayEpoch).orElse(null);
        
        // Jika tidak ditemukan, cari dalam range hari yang sama (untuk handle data lama yang epoch tidak konsisten)
        if (existing == null) {
            Long endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            List<Presensi> sameDayPresensi = presensiRepository.findByUserAndSameDay(
                    user.getIdUser(), todayEpoch, endOfDay);
            if (!sameDayPresensi.isEmpty()) {
                // Merge semua presensi dalam hari yang sama menjadi satu
                existing = sameDayPresensi.get(0);
                existing.setTglAbsensi(todayEpoch);
                
                // Merge data dari record lain jika ada
                for (int i = 1; i < sameDayPresensi.size(); i++) {
                    Presensi other = sameDayPresensi.get(i);
                    if (other.getJamMasuk() != null && !other.getJamMasuk().trim().isEmpty() 
                        && (existing.getJamMasuk() == null || existing.getJamMasuk().trim().isEmpty())) {
                        existing.setJamMasuk(other.getJamMasuk());
                    }
                    if (other.getJamKeluar() != null && !other.getJamKeluar().trim().isEmpty() 
                        && (existing.getJamKeluar() == null || existing.getJamKeluar().trim().isEmpty())) {
                        existing.setJamKeluar(other.getJamKeluar());
                    }
                    if (other.getKdStatus() != null && existing.getKdStatus() == null) {
                        existing.setKdStatus(other.getKdStatus());
                    }
                }
                
                // Hapus record duplikat setelah merge
                for (int i = 1; i < sameDayPresensi.size(); i++) {
                    presensiRepository.delete(sameDayPresensi.get(i));
                }
            }
        }

        // Validasi: jika sudah ada jamMasuk, tidak boleh check-in lagi
        if (existing != null && existing.getJamMasuk() != null && !existing.getJamMasuk().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Anda sudah melakukan check-in hari ini");
        }

        Presensi presensi;
        if (existing != null) {
            // Update record yang sudah ada (misalnya dari absensi)
            presensi = existing;
            // Pastikan tglAbsensi menggunakan start of day
            presensi.setTglAbsensi(todayEpoch);
        } else {
            // Buat record baru
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

        // Gunakan start of day untuk epoch agar konsisten dengan check-in
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        Long todayEpoch = today.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        LocalTime now = LocalTime.now(ZoneId.systemDefault());
        String jamKeluar = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));

        // Cari presensi yang sudah ada untuk hari ini
        Presensi presensi = presensiRepository.findByUserAndTglAbsensi(
                user.getIdUser(), todayEpoch).orElse(null);
        
        // Jika tidak ditemukan, cari dalam range hari yang sama (untuk handle data lama)
        if (presensi == null) {
            Long endOfDay = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            List<Presensi> sameDayPresensi = presensiRepository.findByUserAndSameDay(
                    user.getIdUser(), todayEpoch, endOfDay);
            if (!sameDayPresensi.isEmpty()) {
                // Merge semua presensi dalam hari yang sama menjadi satu
                presensi = sameDayPresensi.get(0);
                presensi.setTglAbsensi(todayEpoch);
                
                // Merge data dari record lain jika ada
                for (int i = 1; i < sameDayPresensi.size(); i++) {
                    Presensi other = sameDayPresensi.get(i);
                    if (other.getJamMasuk() != null && !other.getJamMasuk().trim().isEmpty() 
                        && (presensi.getJamMasuk() == null || presensi.getJamMasuk().trim().isEmpty())) {
                        presensi.setJamMasuk(other.getJamMasuk());
                    }
                    if (other.getJamKeluar() != null && !other.getJamKeluar().trim().isEmpty() 
                        && (presensi.getJamKeluar() == null || presensi.getJamKeluar().trim().isEmpty())) {
                        presensi.setJamKeluar(other.getJamKeluar());
                    }
                    if (other.getKdStatus() != null && presensi.getKdStatus() == null) {
                        presensi.setKdStatus(other.getKdStatus());
                    }
                }
                
                // Hapus record duplikat setelah merge
                for (int i = 1; i < sameDayPresensi.size(); i++) {
                    presensiRepository.delete(sameDayPresensi.get(i));
                }
            }
        }
        
        if (presensi == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Anda belum melakukan check-in hari ini");
        }

        // Validasi: harus sudah check-in (jamMasuk tidak null)
        if (presensi.getJamMasuk() == null || presensi.getJamMasuk().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Anda belum melakukan check-in hari ini");
        }

        // Validasi: tidak boleh double check-out
        if (presensi.getJamKeluar() != null && !presensi.getJamKeluar().trim().isEmpty()) {
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

        // Validasi kdStatus
        if (kdStatus == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Status absensi tidak boleh kosong");
        }
        if (!statusAbsenRepository.existsById(kdStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Status absensi tidak valid");
        }

        // Normalisasi tglAbsensi ke start of day agar konsisten
        // Convert epoch seconds to LocalDate, then back to epoch at start of day
        LocalDate absensiDate = LocalDate.ofInstant(
            java.time.Instant.ofEpochSecond(tglAbsensi), 
            ZoneId.systemDefault()
        );
        Long normalizedTglAbsensi = absensiDate.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

        // Cari presensi yang sudah ada untuk tanggal tersebut
        Presensi presensi = presensiRepository.findByUserAndTglAbsensi(
                user.getIdUser(), normalizedTglAbsensi).orElse(null);
        
        // Jika tidak ditemukan, cari dalam range hari yang sama (untuk handle data lama)
        if (presensi == null) {
            Long endOfDay = absensiDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            List<Presensi> sameDayPresensi = presensiRepository.findByUserAndSameDay(
                    user.getIdUser(), normalizedTglAbsensi, endOfDay);
            if (!sameDayPresensi.isEmpty()) {
                // Merge semua presensi dalam hari yang sama menjadi satu
                presensi = sameDayPresensi.get(0);
                presensi.setTglAbsensi(normalizedTglAbsensi);
                
                // Merge data dari record lain jika ada
                for (int i = 1; i < sameDayPresensi.size(); i++) {
                    Presensi other = sameDayPresensi.get(i);
                    if (other.getJamMasuk() != null && !other.getJamMasuk().trim().isEmpty() 
                        && (presensi.getJamMasuk() == null || presensi.getJamMasuk().trim().isEmpty())) {
                        presensi.setJamMasuk(other.getJamMasuk());
                    }
                    if (other.getJamKeluar() != null && !other.getJamKeluar().trim().isEmpty() 
                        && (presensi.getJamKeluar() == null || presensi.getJamKeluar().trim().isEmpty())) {
                        presensi.setJamKeluar(other.getJamKeluar());
                    }
                    if (other.getKdStatus() != null && presensi.getKdStatus() == null) {
                        presensi.setKdStatus(other.getKdStatus());
                    }
                }
                
                // Hapus record duplikat setelah merge
                for (int i = 1; i < sameDayPresensi.size(); i++) {
                    presensiRepository.delete(sameDayPresensi.get(i));
                }
            }
        }

        if (presensi == null) {
            // Buat record baru untuk absensi
            presensi = new Presensi();
            presensi.setUser(user);
            presensi.setTglAbsensi(normalizedTglAbsensi);
        }
        // Jika sudah ada (misalnya dari check-in), update status saja
        // Pastikan tglAbsensi menggunakan start of day
        presensi.setTglAbsensi(normalizedTglAbsensi);
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

