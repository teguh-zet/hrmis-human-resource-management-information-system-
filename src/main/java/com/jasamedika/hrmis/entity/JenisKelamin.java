package com.jasamedika.hrmis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "jenis_kelamin")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JenisKelamin {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kd_jenis_kelamin")
    private Integer kdJenisKelamin;

    @Column(name = "nama_jenis_kelamin", nullable = false)
    private String namaJenisKelamin;
}

