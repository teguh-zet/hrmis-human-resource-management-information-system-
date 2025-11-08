package com.jasamedika.hrmis.repository;

import com.jasamedika.hrmis.entity.Perusahaan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PerusahaanRepository extends JpaRepository<Perusahaan, Long> {
}

