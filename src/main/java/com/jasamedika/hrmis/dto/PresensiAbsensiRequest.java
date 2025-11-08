package com.jasamedika.hrmis.dto;

import lombok.Data;

@Data
public class PresensiAbsensiRequest {
    private Long tglAbsensi; // Epoch in seconds
    private Integer kdStatus;
}

