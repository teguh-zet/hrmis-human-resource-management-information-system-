package com.jasamedika.hrmis.repository;

import com.jasamedika.hrmis.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByEmailAndPassword(String email, String password);
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           "LOWER(u.namaLengkap) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.nikUser) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:kdJabatan IS NULL OR u.kdJabatan = :kdJabatan) AND " +
           "(:kdDepartemen IS NULL OR u.kdDepartemen = :kdDepartemen) AND " +
           "(:kdUnitKerja IS NULL OR u.kdUnitKerja = :kdUnitKerja) AND " +
           "(:kdPendidikan IS NULL OR u.kdPendidikan = :kdPendidikan) AND " +
           "(:kdJenisKelamin IS NULL OR u.kdJenisKelamin = :kdJenisKelamin) AND " +
           "(:profile IS NULL OR :profile = '' OR u.profile = :profile)")
    Page<User> searchAndFilter(@Param("keyword") String keyword,
                               @Param("kdJabatan") Integer kdJabatan,
                               @Param("kdDepartemen") Integer kdDepartemen,
                               @Param("kdUnitKerja") Integer kdUnitKerja,
                               @Param("kdPendidikan") Integer kdPendidikan,
                               @Param("kdJenisKelamin") Integer kdJenisKelamin,
                               @Param("profile") String profile,
                               Pageable pageable);
}

