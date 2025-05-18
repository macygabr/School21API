package com.example.demo.repository;

import com.example.demo.models.dto.Campus;

import io.micrometer.common.lang.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampusRepository extends JpaRepository<Campus, String> {
    boolean existsById(@NonNull String id);
}
