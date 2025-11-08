package com.jasamedika.hrmis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "perusahaan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Perusahaan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_perusahaan")
    private Long idPerusahaan;

    @Column(name = "nama_perusahaan", nullable = false)
    private String namaPerusahaan;
}

