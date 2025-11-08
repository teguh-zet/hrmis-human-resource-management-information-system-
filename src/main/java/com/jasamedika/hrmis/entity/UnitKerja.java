package com.jasamedika.hrmis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "unit_kerja")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UnitKerja {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kd_unit_kerja")
    private Integer kdUnitKerja;

    @Column(name = "nama_unit_kerja", nullable = false)
    private String namaUnitKerja;
}

