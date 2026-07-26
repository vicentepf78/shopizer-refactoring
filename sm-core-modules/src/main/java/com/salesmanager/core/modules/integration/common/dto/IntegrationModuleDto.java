package com.salesmanager.core.modules.integration.common.dto;

import java.io.Serializable;

public class IntegrationModuleDto implements Serializable {

	private static final long serialVersionUID = 1L;

	private String code;
	private String type;
	private String regions;
	private String module;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getRegions() {
		return regions;
	}

	public void setRegions(String regions) {
		this.regions = regions;
	}

	public String getModule() {
		return module;
	}

	public void setModule(String module) {
		this.module = module;
	}

}
