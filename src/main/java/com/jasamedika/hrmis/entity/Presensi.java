package com.jasamedika.hrmis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "presensi")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Presensi {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_presensi")
    private Long idPresensi;

    @ManyToOne
    @JoinColumn(name = "id_user", nullable = false)
    private User user;

    @Column(name = "tgl_absensi")
    private Long tglAbsensi; // Epoch in seconds

    @Column(name = "jam_masuk")
    private String jamMasuk;

    @Column(name = "jam_keluar")
    private String jamKeluar;

    @Column(name = "kd_status")
    private Integer kdStatus;
}

