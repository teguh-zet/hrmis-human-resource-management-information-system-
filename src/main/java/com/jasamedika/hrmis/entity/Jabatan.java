package com.jasamedika.hrmis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "jabatan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Jabatan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kd_jabatan")
    private Integer kdJabatan;

    @Column(name = "nama_jabatan", nullable = false)
    private String namaJabatan;
}

