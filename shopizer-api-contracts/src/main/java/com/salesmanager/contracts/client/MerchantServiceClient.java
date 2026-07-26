package com.salesmanager.contracts.client;

import com.salesmanager.contracts.merchant.MerchantStoreSnapshot;

public interface MerchantServiceClient {

	MerchantStoreSnapshot getStoreSnapshot(String code);

}
