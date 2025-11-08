package com.jasamedika.hrmis.repository;

import com.jasamedika.hrmis.entity.Presensi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PresensiRepository extends JpaRepository<Presensi, Long> {
    @Query("SELECT p FROM Presensi p WHERE p.user.idUser = :idUser AND p.tglAbsensi = :tglAbsensi")
    Optional<Presensi> findByUserAndTglAbsensi(@Param("idUser") String idUser, @Param("tglAbsensi") Long tglAbsensi);
    
    @Query("SELECT p FROM Presensi p WHERE p.user.idUser = :idUser AND p.tglAbsensi >= :tglAwal AND p.tglAbsensi <= :tglAkhir ORDER BY p.tglAbsensi DESC")
    List<Presensi> findByUserAndDateRange(@Param("idUser") String idUser, @Param("tglAwal") Long tglAwal, @Param("tglAkhir") Long tglAkhir);
    
    @Query("SELECT p FROM Presensi p WHERE p.tglAbsensi >= :tglAwal AND p.tglAbsensi <= :tglAkhir ORDER BY p.tglAbsensi DESC")
    List<Presensi> findByDateRange(@Param("tglAwal") Long tglAwal, @Param("tglAkhir") Long tglAkhir);
}

