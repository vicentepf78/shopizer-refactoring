package com.salesmanager.search.support;

public class UnsupportedSchemaVersionException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final int schemaVersion;

	public UnsupportedSchemaVersionException(int schemaVersion) {
		super("Unsupported schemaVersion: " + schemaVersion);
		this.schemaVersion = schemaVersion;
	}

	public int getSchemaVersion() {
		return schemaVersion;
	}
}
