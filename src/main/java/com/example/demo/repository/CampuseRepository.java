package com.example.demo.repository;

import com.example.demo.models.Campus;
import com.example.demo.models.Peer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CampuseRepository extends JpaRepository<Campus, Long> {
    Boolean existsById(String id);
}
