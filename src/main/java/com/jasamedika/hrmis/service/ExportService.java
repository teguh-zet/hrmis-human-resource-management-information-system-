package com.jasamedika.hrmis.service;

import com.jasamedika.hrmis.dto.PresensiDto;
import com.jasamedika.hrmis.dto.UserInfoDto;
import com.jasamedika.hrmis.entity.User;
import com.jasamedika.hrmis.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PegawaiService pegawaiService;

    @Autowired
    private PresensiService presensiService;

    public byte[] exportPegawaiToExcel() throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                    "User tidak ditemukan"));

        if (!currentUser.getProfile().equals("Admin")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "Hanya Admin yang dapat mengakses");
        }

        List<UserInfoDto> pegawaiList = pegawaiService.getDaftarPegawai();

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Daftar Pegawai");

        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"ID User", "Nama Lengkap", "Email", "NIK", "Profile", 
                          "Jabatan", "Departemen", "Unit Kerja", "Jenis Kelamin", 
                          "Pendidikan", "Tempat Lahir", "Tanggal Lahir"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Create data rows
        int rowNum = 1;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        
        for (UserInfoDto pegawai : pegawaiList) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(pegawai.getIdUser() != null ? pegawai.getIdUser() : "");
            row.createCell(1).setCellValue(pegawai.getNamaLengkap() != null ? pegawai.getNamaLengkap() : "");
            row.createCell(2).setCellValue(pegawai.getEmail() != null ? pegawai.getEmail() : "");
            row.createCell(3).setCellValue(pegawai.getNikUser() != null ? pegawai.getNikUser() : "");
            row.createCell(4).setCellValue(pegawai.getProfile() != null ? pegawai.getProfile() : "");
            row.createCell(5).setCellValue(pegawai.getNamaJabatan() != null ? pegawai.getNamaJabatan() : "");
            row.createCell(6).setCellValue(pegawai.getNamaDepartemen() != null ? pegawai.getNamaDepartemen() : "");
            row.createCell(7).setCellValue(pegawai.getNamaUnitKerja() != null ? pegawai.getNamaUnitKerja() : "");
            row.createCell(8).setCellValue(pegawai.getNamaJenisKelamin() != null ? pegawai.getNamaJenisKelamin() : "");
            row.createCell(9).setCellValue(pegawai.getNamaPendidikan() != null ? pegawai.getNamaPendidikan() : "");
            row.createCell(10).setCellValue(pegawai.getTempatLahir() != null ? pegawai.getTempatLahir() : "");
            
            if (pegawai.getTanggalLahir() != null) {
                LocalDate tanggalLahir = LocalDate.ofInstant(
                    java.time.Instant.ofEpochSecond(pegawai.getTanggalLahir()),
                    ZoneId.systemDefault()
                );
                row.createCell(11).setCellValue(tanggalLahir.format(dateFormatter));
            } else {
                row.createCell(11).setCellValue("");
            }
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }

    public byte[] exportPresensiToExcel(Long tglAwal, Long tglAkhir) throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                    "User tidak ditemukan"));

        if (!currentUser.getProfile().equals("Admin")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                "Hanya Admin yang dapat mengakses");
        }

        List<PresensiDto> presensiList = presensiService.getDaftarPresensiAdmin(tglAwal, tglAkhir);

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Daftar Presensi");

        // Create header style
        CellStyle headerStyle = workbook.createCellStyle();
        Font headerFont = workbook.createFont();
        headerFont.setBold(true);
        headerFont.setFontHeightInPoints((short) 12);
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Nama Pegawai", "Tanggal", "Jam Masuk", "Jam Keluar", "Status"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Create data rows
        int rowNum = 1;
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        
        for (PresensiDto presensi : presensiList) {
            Row row = sheet.createRow(rowNum++);
            
            row.createCell(0).setCellValue(presensi.getNamaLengkap() != null ? presensi.getNamaLengkap() : "");
            
            if (presensi.getTglAbsensi() != null) {
                LocalDate tanggal = LocalDate.ofInstant(
                    java.time.Instant.ofEpochSecond(presensi.getTglAbsensi()),
                    ZoneId.systemDefault()
                );
                row.createCell(1).setCellValue(tanggal.format(dateFormatter));
            } else {
                row.createCell(1).setCellValue("");
            }
            
            row.createCell(2).setCellValue(presensi.getJamMasuk() != null ? presensi.getJamMasuk() : "");
            row.createCell(3).setCellValue(presensi.getJamKeluar() != null ? presensi.getJamKeluar() : "");
            row.createCell(4).setCellValue(presensi.getNamaStatus() != null ? presensi.getNamaStatus() : "");
        }

        // Auto-size columns
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        workbook.close();

        return outputStream.toByteArray();
    }
}

