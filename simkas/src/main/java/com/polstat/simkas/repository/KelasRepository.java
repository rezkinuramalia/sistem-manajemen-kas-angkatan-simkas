package com.polstat.simkas.repository;

import com.polstat.simkas.entity.Kelas;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface KelasRepository extends JpaRepository<Kelas, Long> {
    List<Kelas> findByAngkatanId(Long angkatanId);
}
