package com.bhakti.tms.repository;

import com.bhakti.tms.entity.Load;
import com.bhakti.tms.entity.LoadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoadRepository extends JpaRepository<Load, UUID> {

    Page<Load> findByShipperId(String shipperId, Pageable pageable);

    Page<Load> findByStatus(LoadStatus status, Pageable pageable);

    Optional<Load> findByLoadId(UUID loadId);
}
