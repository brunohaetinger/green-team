package com.greenteam.reportapi.repository;

import com.greenteam.reportapi.entity.TotalSalesByCity;
import com.greenteam.reportapi.entity.TotalSalesByCityId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Repository
public interface TotalSalesByCityRepository extends JpaRepository<TotalSalesByCity, TotalSalesByCityId> {

    Page<TotalSalesByCity> findByCityName(String cityName, Pageable pageable);

    Page<TotalSalesByCity> findBySaleDate(LocalDate saleDate, Pageable pageable);
}
