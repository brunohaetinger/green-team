package com.greenteam.reportapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "total_sales_by_city")
@IdClass(TotalSalesByCityId.class)
public class TotalSalesByCity {

	@Id
	@Column(name = "city_name")
	private String cityName;

	@Id
	@Column(name = "sale_date")
	private LocalDate saleDate;

	@Column(name = "total_amount")
	private BigDecimal totalAmount;

	@Column(name = "total_units")
	private Long totalUnits;

	// TODO: remove @Transient once the DB column is added
	@Transient
	private String countryName;

	public String getCityName() {
		return cityName;
	}

	public LocalDate getSaleDate() {
		return saleDate;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public Long getTotalUnits() {
		return totalUnits;
	}

	public String getCountryName() {
		return countryName;
	}
}
