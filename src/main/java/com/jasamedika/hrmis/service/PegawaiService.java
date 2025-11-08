package com.jasamedika.hrmis.service;

import com.jasamedika.hrmis.dto.*;
import com.jasamedika.hrmis.entity.*;
import com.jasamedika.hrmis.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PegawaiService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JabatanRepository jabatanRepository;

    @Autowired
    private DepartemenRepository departemenRepository;

    @Autowired
    private UnitKerjaRepository unitKerjaRepository;

    @Autowired
    private JenisKelaminRepository jenisKelaminRepository;

    @Autowired
    private PendidikanRepository pendidikanRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public List<ComboDto> getComboJabatan() {
        return jabatanRepository.findAll().stream()
                .map(j -> {
                    ComboDto dto = new ComboDto();
                    dto.setKdJabatan(j.getKdJabatan());
                    dto.setNamaJabatan(j.getNamaJabatan());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<ComboDto> getComboDepartemen() {
        return departemenRepository.findAll().stream()
                .map(d -> {
                    ComboDto dto = new ComboDto();
                    dto.setKdDepartemen(d.getKdDepartemen());
                    dto.setNamaDepartemen(d.getNamaDepartemen());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<ComboDto> getComboUnitKerja() {
        return unitKerjaRepository.findAll().stream()
                .map(u -> {
                    ComboDto dto = new ComboDto();
                    dto.setKdUnitKerja(u.getKdUnitKerja());
                    dto.setNamaUnitKerja(u.getNamaUnitKerja());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<ComboDto> getComboPendidikan() {
        return pendidikanRepository.findAll().stream()
                .map(p -> {
                    ComboDto dto = new ComboDto();
                    dto.setKdPendidikan(p.getKdPendidikan());
                    dto.setNamaPendidikan(p.getNamaPendidikan());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<ComboDto> getComboJenisKelamin() {
        return jenisKelaminRepository.findAll().stream()
                .map(jk -> {
                    ComboDto dto = new ComboDto();
                    dto.setKdJenisKelamin(jk.getKdJenisKelamin());
                    dto.setNamaJenisKelamin(jk.getNamaJenisKelamin());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<ComboDto> getComboDepartemenHrd() {
        return departemenRepository.findHrdDepartemen()
                .map(departemen -> userRepository.findAll().stream()
                        .filter(u -> u.getKdDepartemen() != null && 
                                u.getKdDepartemen().equals(departemen.getKdDepartemen()))
                        .map(u -> {
                            ComboDto dto = new ComboDto();
                            dto.setNamaLengkap(u.getNamaLengkap());
                            dto.setKdJabatan(u.getKdJabatan());
                            Jabatan jabatan = jabatanRepository.findById(u.getKdJabatan()).orElse(null);
                            if (jabatan != null) {
                                dto.setNamaJabatan(jabatan.getNamaJabatan());
                            }
                            return dto;
                        })
                        .collect(Collectors.toList()))
                .orElse(List.of());
    }

    public List<UserInfoDto> getDaftarPegawai() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                    "User tidak ditemukan"));

        // Check if admin or HRD
        if (!currentUser.getProfile().equals("Admin")) {
            Departemen hrdDept = departemenRepository.findHrdDepartemen()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, 
                        "Akses ditolak"));
            
            if (currentUser.getKdDepartemen() == null || 
                !currentUser.getKdDepartemen().equals(hrdDept.getKdDepartemen())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Hanya Admin atau pegawai HRD yang dapat mengakses");
            }
        }

        return userRepository.findAll().stream()
                .map(this::mapToUserInfoDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void tambahPegawai(TambahPegawaiRequest request) {
        checkAdminOrHrd();

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Email sudah terdaftar");
        }

        if (!request.getPassword().equals(request.getPasswordC())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Password tidak sama");
        }

        User pegawai = new User();
        pegawai.setIdUser(UUID.randomUUID().toString());
        pegawai.setProfile("Pegawai");
        pegawai.setNamaLengkap(request.getNamaLengkap());
        pegawai.setEmail(request.getEmail());
        pegawai.setTempatLahir(request.getTempatLahir());
        pegawai.setTanggalLahir(request.getTanggalLahir());
        pegawai.setKdJenisKelamin(request.getKdJenisKelamin());
        pegawai.setKdPendidikan(request.getKdPendidikan());
        pegawai.setKdJabatan(request.getKdJabatan());
        pegawai.setKdDepartemen(request.getKdDepartemen());
        pegawai.setKdUnitKerja(request.getKdUnitKerja());
        pegawai.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(pegawai);
    }

    @Transactional
    public void ubahPegawai(UbahPegawaiRequest request) {
        checkAdminOrHrd();

        User pegawai = userRepository.findById(request.getIdUser())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Pegawai tidak ditemukan"));

        if (!request.getPassword().equals(request.getPasswordC())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                "Password tidak sama");
        }

        pegawai.setNamaLengkap(request.getNamaLengkap());
        pegawai.setEmail(request.getEmail());
        pegawai.setTempatLahir(request.getTempatLahir());
        pegawai.setTanggalLahir(request.getTanggalLahir());
        pegawai.setKdJenisKelamin(request.getKdJenisKelamin());
        pegawai.setKdPendidikan(request.getKdPendidikan());
        pegawai.setKdJabatan(request.getKdJabatan());
        pegawai.setKdDepartemen(request.getKdDepartemen());
        pegawai.setKdUnitKerja(request.getKdUnitKerja());
        
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            pegawai.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        
        userRepository.save(pegawai);
    }

    @Transactional
    public void ubahPhotoPegawai(String idUser, String namaFile, MultipartFile file) {
        checkAdminOrHrd();
        
        User pegawai = userRepository.findById(idUser)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "Pegawai tidak ditemukan"));

        String fileName = saveFile(file, namaFile);
        pegawai.setPhoto(fileName);
        userRepository.save(pegawai);
    }

    @Transactional
    public void ubahPhotoSendiri(String namaFile, MultipartFile file) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, 
                    "User tidak ditemukan"));

        String fileName = saveFile(file, namaFile);
        user.setPhoto(fileName);
        userRepository.save(user);
    }

    public UserInfoDto mapToUserInfoDto(User user) {
        UserInfoDto dto = new UserInfoDto();
        dto.setProfile(user.getProfile());
        dto.setIdUser(user.getIdUser());
        dto.setNamaLengkap(user.getNamaLengkap());
        dto.setTempatLahir(user.getTempatLahir());
        dto.setTanggalLahir(user.getTanggalLahir());
        dto.setEmail(user.getEmail());
        dto.setPassword(user.getPassword());
        dto.setNikUser(user.getNikUser());
        dto.setKdJabatan(user.getKdJabatan());
        dto.setKdDepartemen(user.getKdDepartemen());
        dto.setKdUnitKerja(user.getKdUnitKerja());
        dto.setKdJenisKelamin(user.getKdJenisKelamin());
        dto.setKdPendidikan(user.getKdPendidikan());
        dto.setPhoto(user.getPhoto());

        if (user.getKdJabatan() != null) {
            jabatanRepository.findById(user.getKdJabatan())
                    .ifPresent(j -> dto.setNamaJabatan(j.getNamaJabatan()));
        }
        if (user.getKdDepartemen() != null) {
            departemenRepository.findById(user.getKdDepartemen())
                    .ifPresent(d -> dto.setNamaDepartemen(d.getNamaDepartemen()));
        }
        if (user.getKdUnitKerja() != null) {
            unitKerjaRepository.findById(user.getKdUnitKerja())
                    .ifPresent(u -> dto.setNamaUnitKerja(u.getNamaUnitKerja()));
        }
        if (user.getKdJenisKelamin() != null) {
            jenisKelaminRepository.findById(user.getKdJenisKelamin())
                    .ifPresent(jk -> dto.setNamaJenisKelamin(jk.getNamaJenisKelamin()));
        }
        if (user.getKdPendidikan() != null) {
            pendidikanRepository.findById(user.getKdPendidikan())
                    .ifPresent(p -> dto.setNamaPendidikan(p.getNamaPendidikan()));
        }

        return dto;
    }

    private void checkAdminOrHrd() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, 
                    "User tidak ditemukan"));

        if (!currentUser.getProfile().equals("Admin")) {
            Departemen hrdDept = departemenRepository.findHrdDepartemen()
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, 
                        "Akses ditolak"));
            
            if (currentUser.getKdDepartemen() == null || 
                !currentUser.getKdDepartemen().equals(hrdDept.getKdDepartemen())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, 
                    "Hanya Admin atau pegawai HRD yang dapat mengakses");
            }
        }
    }

    private String saveFile(MultipartFile file, String namaFile) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String fileExtension = "";
            String originalFilename = file.getOriginalFilename();
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String fileName = (namaFile != null && !namaFile.isEmpty()) 
                    ? namaFile + fileExtension 
                    : UUID.randomUUID().toString() + fileExtension;

            Path filePath = uploadPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, 
                "Gagal menyimpan file: " + e.getMessage());
        }
    }
}

