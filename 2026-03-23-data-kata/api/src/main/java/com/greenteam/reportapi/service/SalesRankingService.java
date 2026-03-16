package com.greenteam.reportapi.service;

import com.greenteam.reportapi.dto.PageResponseDto;
import com.greenteam.reportapi.dto.TopSalesByCityDto;
import com.greenteam.reportapi.dto.TopSalesmanDto;
import com.greenteam.reportapi.entity.TopSalesman;
import com.greenteam.reportapi.entity.TotalSalesByCity;
import com.greenteam.reportapi.repository.TopSalesmanRepository;
import com.greenteam.reportapi.repository.TotalSalesByCityRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@Transactional(readOnly = true)
public class SalesRankingService {

    private final TotalSalesByCityRepository salesByCityRepository;
    private final TopSalesmanRepository topSalesmanRepository;

    public SalesRankingService(TotalSalesByCityRepository salesByCityRepository,
                               TopSalesmanRepository topSalesmanRepository) {
        this.salesByCityRepository = salesByCityRepository;
        this.topSalesmanRepository = topSalesmanRepository;
    }

    public PageResponseDto<TopSalesByCityDto> getTopSalesByCity(
            String filterBy, String filterValue, String sortBy, int page, int size) {

        Sort sort = Sort.by(Sort.Order.desc(mapSalesByCitySortBy(sortBy)));
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TotalSalesByCity> result;
        if (filterBy != null && filterValue != null) {
            result = switch (filterBy) {
                case "saleDate" -> salesByCityRepository.findBySaleDate(LocalDate.parse(filterValue), pageable);
                default -> salesByCityRepository.findByCityName(filterValue, pageable);
            };
        } else {
            result = salesByCityRepository.findAll(pageable);
        }

        return PageResponseDto.from(result.map(this::toTopSalesByCityDto));
    }

    public PageResponseDto<TopSalesmanDto> getTopSalesman(
            String filterBy, String filterValue, String sortBy, int page, int size) {

        Sort sort = Sort.by(Sort.Order.desc(mapTopSalesmanSortBy(sortBy)));
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<TopSalesman> result;
        if (filterBy != null && filterValue != null) {
            result = switch (filterBy) {
                case "saleDate" -> topSalesmanRepository.findBySaleDate(LocalDate.parse(filterValue), pageable);
                default -> topSalesmanRepository.findBySalesmanName(filterValue, pageable);
            };
        } else {
            result = topSalesmanRepository.findAll(pageable);
        }

        return PageResponseDto.from(result.map(this::toTopSalesmanDto));
    }

    private String mapSalesByCitySortBy(String sortBy) {
        return switch (sortBy) {
            case "totalUnits" -> "totalUnits";
            default -> "totalAmount";
        };
    }

    private String mapTopSalesmanSortBy(String sortBy) {
        return switch (sortBy) {
            case "totalUnits" -> "totalUnits";
            case "salesmanName" -> "salesmanName";
            default -> "totalAmount";
        };
    }

    private TopSalesByCityDto toTopSalesByCityDto(TotalSalesByCity entity) {
        return new TopSalesByCityDto(
                entity.getCityName(),
                entity.getCountryName(),
                entity.getSaleDate(),
                entity.getTotalAmount(),
                entity.getTotalUnits()
        );
    }

    private TopSalesmanDto toTopSalesmanDto(TopSalesman entity) {
        return new TopSalesmanDto(
                entity.getSalesmanName(),
                entity.getCityName(),
                entity.getCountryName(),
                entity.getSaleDate(),
                entity.getTotalAmount(),
                entity.getTotalUnits()
        );
    }
}
