package com.greenteam.reportapi.controller;

import com.greenteam.reportapi.dto.PageResponseDto;
import com.greenteam.reportapi.dto.TopSalesByCityDto;
import com.greenteam.reportapi.dto.TopSalesmanDto;
import com.greenteam.reportapi.service.SalesRankingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/v1/sales/rankings")
public class SalesRankingController {

	private final SalesRankingService salesRankingService;

	public SalesRankingController(SalesRankingService salesRankingService) {
		this.salesRankingService = salesRankingService;
	}

	@GetMapping("/top-sales-by-city")
	public ResponseEntity<PageResponseDto<TopSalesByCityDto>> getTopSalesByCity(
			@RequestParam(required = false) String filterBy, @RequestParam(required = false) String filterValue,
			@RequestParam(defaultValue = "totalAmount") String sortBy, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {

		return ResponseEntity.ok(salesRankingService.getTopSalesByCity(filterBy, filterValue, sortBy, page, size));
	}

	@GetMapping("/top-salesman")
	public ResponseEntity<PageResponseDto<TopSalesmanDto>> getTopSalesman(
			@RequestParam(required = false) String filterBy, @RequestParam(required = false) String filterValue,
			@RequestParam(defaultValue = "totalAmount") String sortBy, @RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "50") int size) {

		return ResponseEntity.ok(salesRankingService.getTopSalesman(filterBy, filterValue, sortBy, page, size));
	}
}
