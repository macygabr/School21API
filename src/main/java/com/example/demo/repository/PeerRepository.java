package com.example.demo.repository;

import com.example.demo.models.Peer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PeerRepository extends JpaRepository<Peer, Long> {

    boolean existsByLogin(String peerName);

    Peer findByLogin(String peerName);

    Page<Peer> findByCampusId(String campusId, Pageable pageable);
}