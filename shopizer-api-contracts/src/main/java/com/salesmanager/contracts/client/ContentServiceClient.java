package com.salesmanager.contracts.client;

public interface ContentServiceClient {

	byte[] getStaticFile(String storeCode, String imageType, String fileName);

	void uploadLogo(String storeCode, String fileName, byte[] content, String contentType);

	void deleteLogo(String storeCode, String fileName);

}
