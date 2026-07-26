package com.salesmanager.contracts.tenant;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public final class MerchantStoreId implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String code;

	@JsonCreator
	public static MerchantStoreId of(String code) {
		return new MerchantStoreId(code);
	}

	public MerchantStoreId(String code) {
		if (code == null || code.isBlank()) {
			throw new IllegalArgumentException("merchant store code must not be blank");
		}
		this.code = code.trim();
	}

	@JsonValue
	public String getCode() {
		return code;
	}

	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof MerchantStoreId)) {
			return false;
		}
		MerchantStoreId that = (MerchantStoreId) other;
		return code.equals(that.code);
	}

	@Override
	public int hashCode() {
		return Objects.hash(code);
	}

	@Override
	public String toString() {
		return code;
	}

}
