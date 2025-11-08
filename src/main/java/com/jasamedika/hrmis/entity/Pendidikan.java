package com.jasamedika.hrmis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pendidikan")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pendidikan {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kd_pendidikan")
    private Integer kdPendidikan;

    @Column(name = "nama_pendidikan", nullable = false)
    private String namaPendidikan;
}

