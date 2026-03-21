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
@Table(name = "top_salesman")
@IdClass(TopSalesmanId.class)
public class TopSalesman {

	@Id
	@Column(name = "salesman_id")
	private Integer salesmanId;

	@Id
	@Column(name = "sale_date")
	private LocalDate saleDate;

	@Column(name = "salesman_name")
	private String salesmanName;

	@Column(name = "total_amount")
	private BigDecimal totalAmount;

	@Column(name = "total_units")
	private Long totalUnits;

	// TODO: remove @Transient once the DB columns are added
	@Transient
	private String cityName;

	@Transient
	private String countryName;

	public Integer getSalesmanId() {
		return salesmanId;
	}

	public LocalDate getSaleDate() {
		return saleDate;
	}

	public String getSalesmanName() {
		return salesmanName;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public Long getTotalUnits() {
		return totalUnits;
	}

	public String getCityName() {
		return cityName;
	}

	public String getCountryName() {
		return countryName;
	}
}
