package com.salesmanager.core.business.services.search.index;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.salesmanager.contracts.search.ProductIndexPayload;
import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.business.services.catalog.ProductSnapshotBuilder;
import com.salesmanager.core.model.catalog.product.Product;
import com.salesmanager.core.model.merchant.MerchantStore;

@Service
public class ProductIndexPayloadBuilder {

	@Autowired
	private ProductSnapshotBuilder snapshotBuilder;

	public List<ProductIndexPayload> buildAll(MerchantStore store, Product product) throws ServiceException {
		return snapshotBuilder.buildAll(store, product).stream()
				.map(ProductSnapshotIndexMapper::toPayload)
				.collect(Collectors.toList());
	}

}
