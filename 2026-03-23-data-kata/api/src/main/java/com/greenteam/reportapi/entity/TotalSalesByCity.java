package com.greenteam.reportapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "total_sales_by_city")
@IdClass(TotalSalesByCityId.class)
public class TotalSalesByCity {

	@Id
	@Column(name = "store_id")
	private Integer storeId;

	@Id
	@Column(name = "sale_date")
	private LocalDate saleDate;

	@Column(name = "city_name")
	private String cityName;

	@Column(name = "store_name")
	private String storeName;

	@Column(name = "total_amount")
	private BigDecimal totalAmount;

	@Column(name = "total_units")
	private Long totalUnits;

	@Column(name = "updated_at")
	private OffsetDateTime updatedAt;

	// TODO: remove @Transient once the DB column is added
	@Transient
	private String countryName;

	public Integer getStoreId() {
		return storeId;
	}

	public LocalDate getSaleDate() {
		return saleDate;
	}

	public String getCityName() {
		return cityName;
	}

	public String getStoreName() {
		return storeName;
	}

	public BigDecimal getTotalAmount() {
		return totalAmount;
	}

	public Long getTotalUnits() {
		return totalUnits;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}

	public String getCountryName() {
		return countryName;
	}
}
