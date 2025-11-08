package com.jasamedika.hrmis.repository;

import com.jasamedika.hrmis.entity.StatusAbsen;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StatusAbsenRepository extends JpaRepository<StatusAbsen, Integer> {
}

