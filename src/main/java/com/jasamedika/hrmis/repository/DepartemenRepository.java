package com.jasamedika.hrmis.repository;

import com.jasamedika.hrmis.entity.Departemen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DepartemenRepository extends JpaRepository<Departemen, Integer> {
    @Query("SELECT d FROM Departemen d WHERE d.namaDepartemen = 'HRD'")
    Optional<Departemen> findHrdDepartemen();
}

