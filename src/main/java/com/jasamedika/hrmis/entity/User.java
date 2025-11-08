package com.jasamedika.hrmis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @Column(name = "id_user")
    private String idUser;

    @Column(name = "profile")
    private String profile;

    @Column(name = "nama_lengkap", nullable = false)
    private String namaLengkap;

    @Column(name = "tempat_lahir")
    private String tempatLahir;

    @Column(name = "tanggal_lahir")
    private Long tanggalLahir; // Epoch in seconds

    @Column(name = "email", unique = true, nullable = false)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "nik_user")
    private String nikUser;

    @Column(name = "kd_jabatan")
    private Integer kdJabatan;

    @Column(name = "kd_departemen")
    private Integer kdDepartemen;

    @Column(name = "kd_unit_kerja")
    private Integer kdUnitKerja;

    @Column(name = "kd_jenis_kelamin")
    private Integer kdJenisKelamin;

    @Column(name = "kd_pendidikan")
    private Integer kdPendidikan;

    @Column(name = "photo")
    private String photo;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Presensi> presensiList;
}

