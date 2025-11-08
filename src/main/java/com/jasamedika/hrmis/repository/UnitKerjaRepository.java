package com.jasamedika.hrmis.repository;

import com.jasamedika.hrmis.entity.UnitKerja;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UnitKerjaRepository extends JpaRepository<UnitKerja, Integer> {
}

