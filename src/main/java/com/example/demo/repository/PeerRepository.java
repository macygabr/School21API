package com.example.demo.repository;

import com.example.demo.models.Campus;
import com.example.demo.models.Peer;
import com.example.demo.models.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

@Repository
public interface PeerRepository extends JpaRepository<Peer, Long> {

    boolean existsByLogin(String peerName);

    Optional<Peer> findByLogin(String peerName);

    Page<Peer> findByCampusId(String campusId, Pageable pageable);


    @Query("SELECT p FROM Peer p WHERE p.campus.id = :campusId " +
                "AND (:statuses IS NULL OR p.status IN :statuses)")
    Page<Peer> findByCampusIdAndStatusIn(@Param("campusId") String campusId,
                                             @Param("statuses") List<Status> statuses,
                                             Pageable pageable);

    @Query("SELECT COUNT(p) FROM Peer p WHERE p.campus.id = :campusId " +
            "AND (:statuses IS NULL OR p.status IN :statuses)")
    long countByCampusIdAndStatusIn(@Param("campusId") String campusId,
                                    @Param("statuses") List<Status> statuses);

}

