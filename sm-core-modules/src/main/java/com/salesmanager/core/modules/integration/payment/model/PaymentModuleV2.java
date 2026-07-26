package com.salesmanager.core.modules.integration.payment.model;

import com.salesmanager.core.model.system.IntegrationConfiguration;
import com.salesmanager.core.modules.integration.IntegrationException;
import com.salesmanager.core.modules.integration.common.dto.IntegrationStoreContext;
import com.salesmanager.core.modules.integration.payment.dto.PaymentCaptureContext;
import com.salesmanager.core.modules.integration.payment.dto.PaymentRefundContext;
import com.salesmanager.core.modules.integration.payment.dto.PaymentRequestContext;
import com.salesmanager.core.modules.integration.payment.dto.TransactionResult;

public interface PaymentModuleV2 {

	void validateModuleConfiguration(IntegrationConfiguration configuration, IntegrationStoreContext store)
			throws IntegrationException;

	TransactionResult initTransaction(PaymentRequestContext context) throws IntegrationException;

	TransactionResult authorize(PaymentRequestContext context) throws IntegrationException;

	TransactionResult authorizeAndCapture(PaymentRequestContext context) throws IntegrationException;

	TransactionResult capture(PaymentCaptureContext context) throws IntegrationException;

	TransactionResult refund(PaymentRefundContext context) throws IntegrationException;

}
