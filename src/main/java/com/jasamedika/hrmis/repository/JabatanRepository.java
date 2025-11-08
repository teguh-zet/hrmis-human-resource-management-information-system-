package com.jasamedika.hrmis.repository;

import com.jasamedika.hrmis.entity.Jabatan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JabatanRepository extends JpaRepository<Jabatan, Integer> {
}

