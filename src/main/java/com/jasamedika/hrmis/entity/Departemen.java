package com.jasamedika.hrmis.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "departemen")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Departemen {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "kd_departemen")
    private Integer kdDepartemen;

    @Column(name = "nama_departemen", nullable = false)
    private String namaDepartemen;
}

