package com.salesmanager.contracts.merchant;

import java.util.List;

import com.salesmanager.contracts.reference.PersistableAddress;

public class PersistableMerchantStore extends MerchantStoreEntity {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private PersistableAddress address;
	//code of parent store (can be null if retailer)
	private String retailerStore;
	private List<String> supportedLanguages;

	public List<String> getSupportedLanguages() {
		return supportedLanguages;
	}

	public void setSupportedLanguages(List<String> supportedLanguages) {
		this.supportedLanguages = supportedLanguages;
	}

	public PersistableAddress getAddress() {
		return address;
	}

	public void setAddress(PersistableAddress address) {
		this.address = address;
	}

  public String getRetailerStore() {
    return retailerStore;
  }

  public void setRetailerStore(String retailerStore) {
    this.retailerStore = retailerStore;
  }

}
