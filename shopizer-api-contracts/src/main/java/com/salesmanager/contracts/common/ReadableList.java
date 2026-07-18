package com.salesmanager.contracts.common;

import java.io.Serializable;

public abstract class ReadableList implements Serializable {

	private static final long serialVersionUID = 1L;

	private int totalPages;
	private int number;
	private long recordsTotal;
	private int recordsFiltered;

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalCount) {
		this.totalPages = totalCount;
	}

	public long getRecordsTotal() {
		return recordsTotal;
	}

	public void setRecordsTotal(long recordsTotal) {
		this.recordsTotal = recordsTotal;
	}

	public int getRecordsFiltered() {
		return recordsFiltered;
	}

	public void setRecordsFiltered(int recordsFiltered) {
		this.recordsFiltered = recordsFiltered;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

}
