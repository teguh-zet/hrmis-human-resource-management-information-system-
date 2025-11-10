package com.jasamedika.hrmis.service;

import com.jasamedika.hrmis.dto.DashboardStatsDto;
import com.jasamedika.hrmis.entity.Presensi;
import com.jasamedika.hrmis.entity.User;
import com.jasamedika.hrmis.repository.DepartemenRepository;
import com.jasamedika.hrmis.repository.PresensiRepository;
import com.jasamedika.hrmis.repository.StatusAbsenRepository;
import com.jasamedika.hrmis.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PresensiRepository presensiRepository;

    @Autowired
    private DepartemenRepository departemenRepository;

    @Autowired
    private StatusAbsenRepository statusAbsenRepository;

    public DashboardStatsDto getDashboardStats() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                    "User tidak ditemukan"));

        if (!currentUser.getProfile().equals("Admin")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "Hanya Admin yang dapat mengakses");
        }

        DashboardStatsDto stats = new DashboardStatsDto();

        // Total Pegawai
        stats.setTotalPegawai(userRepository.count());

        // Tanggal hari ini dan bulan ini
        LocalDate today = LocalDate.now(ZoneId.systemDefault());
        LocalDate firstDayOfMonth = today.withDayOfMonth(1);
        LocalDate lastDayOfMonth = today.withDayOfMonth(today.lengthOfMonth());
        
        Long todayEpoch = today.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        Long todayEndEpoch = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        Long monthStartEpoch = firstDayOfMonth.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
        Long monthEndEpoch = lastDayOfMonth.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();

        // Presensi hari ini
        List<Presensi> presensiHariIni = presensiRepository.findByDateRange(todayEpoch, todayEndEpoch - 1);
        stats.setTotalPresensiHariIni((long) presensiHariIni.size());
        
        // Presensi bulan ini
        List<Presensi> presensiBulanIni = presensiRepository.findByDateRange(monthStartEpoch, monthEndEpoch - 1);
        stats.setTotalPresensiBulanIni((long) presensiBulanIni.size());

        // Absen bulan ini (status absen)
        long absenBulanIni = presensiBulanIni.stream()
                .filter(p -> p.getKdStatus() != null)
                .count();
        stats.setTotalAbsenBulanIni(absenBulanIni);

        // Rata-rata jam masuk dan keluar
        List<String> jamMasukList = presensiBulanIni.stream()
                .map(Presensi::getJamMasuk)
                .filter(jam -> jam != null && !jam.trim().isEmpty())
                .collect(Collectors.toList());
        
        if (!jamMasukList.isEmpty()) {
            double totalSeconds = jamMasukList.stream()
                    .mapToDouble(jam -> {
                        String[] parts = jam.split(":");
                        return Integer.parseInt(parts[0]) * 3600 + 
                               Integer.parseInt(parts[1]) * 60 + 
                               Integer.parseInt(parts[2]);
                    })
                    .sum();
            double avgSeconds = totalSeconds / jamMasukList.size();
            int avgHours = (int) (avgSeconds / 3600);
            int avgMinutes = (int) ((avgSeconds % 3600) / 60);
            stats.setRataRataJamMasuk(Double.parseDouble(String.format("%d.%02d", avgHours, avgMinutes)));
        }

        List<String> jamKeluarList = presensiBulanIni.stream()
                .map(Presensi::getJamKeluar)
                .filter(jam -> jam != null && !jam.trim().isEmpty())
                .collect(Collectors.toList());
        
        if (!jamKeluarList.isEmpty()) {
            double totalSeconds = jamKeluarList.stream()
                    .mapToDouble(jam -> {
                        String[] parts = jam.split(":");
                        return Integer.parseInt(parts[0]) * 3600 + 
                               Integer.parseInt(parts[1]) * 60 + 
                               Integer.parseInt(parts[2]);
                    })
                    .sum();
            double avgSeconds = totalSeconds / jamKeluarList.size();
            int avgHours = (int) (avgSeconds / 3600);
            int avgMinutes = (int) ((avgSeconds % 3600) / 60);
            stats.setRataRataJamKeluar(Double.parseDouble(String.format("%d.%02d", avgHours, avgMinutes)));
        }

        // Presensi per hari (7 hari terakhir)
        List<Map<String, Object>> presensiPerHari = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Long dateEpoch = date.atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            Long dateEndEpoch = date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toEpochSecond();
            List<Presensi> presensiDate = presensiRepository.findByDateRange(dateEpoch, dateEndEpoch - 1);
            
            Map<String, Object> data = new HashMap<>();
            data.put("tanggal", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            data.put("jumlah", presensiDate.size());
            presensiPerHari.add(data);
        }
        stats.setPresensiPerHari(presensiPerHari);

        // Presensi per departemen
        Map<Integer, Long> presensiByDept = presensiBulanIni.stream()
                .filter(p -> p.getUser().getKdDepartemen() != null)
                .collect(Collectors.groupingBy(
                    p -> p.getUser().getKdDepartemen(),
                    Collectors.counting()
                ));
        
        List<Map<String, Object>> presensiPerDepartemen = new ArrayList<>();
        presensiByDept.forEach((kdDept, count) -> {
            departemenRepository.findById(kdDept).ifPresent(dept -> {
                Map<String, Object> data = new HashMap<>();
                data.put("departemen", dept.getNamaDepartemen());
                data.put("jumlah", count);
                presensiPerDepartemen.add(data);
            });
        });
        stats.setPresensiPerDepartemen(presensiPerDepartemen);

        // Statistik status absen
        Map<Integer, Long> statusCount = presensiBulanIni.stream()
                .filter(p -> p.getKdStatus() != null)
                .collect(Collectors.groupingBy(
                    Presensi::getKdStatus,
                    Collectors.counting()
                ));
        
        List<Map<String, Object>> statusAbsenStatistik = new ArrayList<>();
        statusCount.forEach((kdStatus, count) -> {
            statusAbsenRepository.findById(kdStatus).ifPresent(status -> {
                Map<String, Object> data = new HashMap<>();
                data.put("status", status.getNamaStatus());
                data.put("jumlah", count);
                statusAbsenStatistik.add(data);
            });
        });
        stats.setStatusAbsenStatistik(statusAbsenStatistik);

        // Pegawai masuk dan tidak masuk hari ini
        long pegawaiMasuk = presensiHariIni.stream()
                .filter(p -> p.getJamMasuk() != null && !p.getJamMasuk().trim().isEmpty())
                .count();
        stats.setPegawaiMasukHariIni(pegawaiMasuk);
        
        long totalPegawai = stats.getTotalPegawai();
        stats.setPegawaiTidakMasukHariIni(totalPegawai - pegawaiMasuk);

        return stats;
    }
}

