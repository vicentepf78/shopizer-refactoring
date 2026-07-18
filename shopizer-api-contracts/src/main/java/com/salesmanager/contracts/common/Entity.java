package com.salesmanager.contracts.common;

import java.io.Serializable;

public class Entity implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id = 0L;

	public Entity() {
	}

	public Entity(Long id) {
		this.id = id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

}
