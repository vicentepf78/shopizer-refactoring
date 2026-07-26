package com.salesmanager.shop.strangler.search;

import org.springframework.stereotype.Service;

import com.salesmanager.contracts.catalog.ProductSnapshot;
import com.salesmanager.contracts.search.ProductIndexPayload;
import com.salesmanager.core.business.services.search.index.ProductSnapshotIndexMapper;

@Service
public class ProductIndexPayloadMapper {

	public ProductIndexPayload toPayload(ProductSnapshot snapshot) {
		return ProductSnapshotIndexMapper.toPayload(snapshot);
	}

}
