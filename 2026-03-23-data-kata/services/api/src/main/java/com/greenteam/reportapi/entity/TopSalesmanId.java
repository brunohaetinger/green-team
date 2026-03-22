package com.greenteam.reportapi.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

public class TopSalesmanId implements Serializable {

	private Integer salesmanId;
	private LocalDate saleDate;

	public TopSalesmanId() {
	}

	public TopSalesmanId(Integer salesmanId, LocalDate saleDate) {
		this.salesmanId = salesmanId;
		this.saleDate = saleDate;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof TopSalesmanId that))
			return false;
		return Objects.equals(salesmanId, that.salesmanId) && Objects.equals(saleDate, that.saleDate);
	}

	@Override
	public int hashCode() {
		return Objects.hash(salesmanId, saleDate);
	}
}
