package com.jasamedika.hrmis.repository;

import com.jasamedika.hrmis.entity.JenisKelamin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JenisKelaminRepository extends JpaRepository<JenisKelamin, Integer> {
}

