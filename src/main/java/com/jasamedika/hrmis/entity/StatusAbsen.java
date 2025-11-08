package com.jasamedika.hrmis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "status_absen")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusAbsen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kd_status")
    private Integer kdStatus;

    @Column(name = "nama_status", nullable = false)
    private String namaStatus;
}

