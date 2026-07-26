package com.salesmanager.contracts.tenant;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public final class LanguageCode implements Serializable {

	private static final long serialVersionUID = 1L;

	private final String code;

	@JsonCreator
	public static LanguageCode of(String code) {
		return new LanguageCode(code);
	}

	public LanguageCode(String code) {
		if (code == null || code.isBlank()) {
			throw new IllegalArgumentException("language code must not be blank");
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
		if (!(other instanceof LanguageCode)) {
			return false;
		}
		LanguageCode that = (LanguageCode) other;
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
