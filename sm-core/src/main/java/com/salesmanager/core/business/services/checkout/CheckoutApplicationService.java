package com.salesmanager.core.business.services.checkout;

import com.salesmanager.core.business.exception.ServiceException;
import com.salesmanager.core.model.order.Order;

public interface CheckoutApplicationService {

	Order placeOrder(CheckoutCommand command) throws ServiceException;

}
