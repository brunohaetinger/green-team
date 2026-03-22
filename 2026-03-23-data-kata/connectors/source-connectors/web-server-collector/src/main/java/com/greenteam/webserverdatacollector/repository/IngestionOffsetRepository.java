package com.greenteam.webserverdatacollector.repository;

import com.greenteam.webserverdatacollector.entity.IngestionOffset;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngestionOffsetRepository extends JpaRepository<IngestionOffset, String> {
}