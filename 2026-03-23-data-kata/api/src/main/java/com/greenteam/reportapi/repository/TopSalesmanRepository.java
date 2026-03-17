package com.greenteam.reportapi.repository;

import com.greenteam.reportapi.entity.TopSalesman;
import com.greenteam.reportapi.entity.TopSalesmanId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface TopSalesmanRepository extends JpaRepository<TopSalesman, TopSalesmanId> {

    Page<TopSalesman> findBySalesmanName(String salesmanName, Pageable pageable);

    Page<TopSalesman> findBySaleDate(LocalDate saleDate, Pageable pageable);
}
