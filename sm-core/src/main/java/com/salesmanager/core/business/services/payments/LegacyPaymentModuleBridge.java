package com.salesmanager.core.business.services.payments;

import com.salesmanager.core.model.system.IntegrationConfiguration;
import com.salesmanager.core.model.system.IntegrationModule;
import com.salesmanager.core.modules.integration.IntegrationException;
import com.salesmanager.core.modules.integration.common.dto.IntegrationModuleDto;
import com.salesmanager.core.modules.integration.common.dto.IntegrationStoreContext;
import com.salesmanager.core.modules.integration.payment.dto.PaymentCaptureContext;
import com.salesmanager.core.modules.integration.payment.dto.PaymentRefundContext;
import com.salesmanager.core.modules.integration.payment.dto.PaymentRequestContext;
import com.salesmanager.core.modules.integration.payment.dto.TransactionResult;
import com.salesmanager.core.modules.integration.payment.model.PaymentModule;
import com.salesmanager.core.modules.integration.payment.model.PaymentModuleV2;

public class LegacyPaymentModuleBridge implements PaymentModuleV2 {

	private final PaymentModule delegate;
	private final LegacyPaymentEntityBundle entities;

	public LegacyPaymentModuleBridge(PaymentModule delegate, LegacyPaymentEntityBundle entities) {
		this.delegate = delegate;
		this.entities = entities;
	}

	@Override
	public void validateModuleConfiguration(IntegrationConfiguration configuration, IntegrationStoreContext store)
			throws IntegrationException {
		delegate.validateModuleConfiguration(configuration, entities.getStore());
	}

	@Override
	public TransactionResult initTransaction(PaymentRequestContext context) throws IntegrationException {
		return IntegrationContextMapper.toTransactionResult(delegate.initTransaction(entities.getStore(),
				entities.getCustomer(), context.getAmount(), entities.getPayment(), context.getConfiguration(),
				resolveModule(context)));
	}

	@Override
	public TransactionResult authorize(PaymentRequestContext context) throws IntegrationException {
		return IntegrationContextMapper.toTransactionResult(delegate.authorize(entities.getStore(),
				entities.getCustomer(), entities.getItems(), context.getAmount(), entities.getPayment(),
				context.getConfiguration(), resolveModule(context)));
	}

	@Override
	public TransactionResult authorizeAndCapture(PaymentRequestContext context) throws IntegrationException {
		return IntegrationContextMapper.toTransactionResult(delegate.authorizeAndCapture(entities.getStore(),
				entities.getCustomer(), entities.getItems(), context.getAmount(), entities.getPayment(),
				context.getConfiguration(), resolveModule(context)));
	}

	@Override
	public TransactionResult capture(PaymentCaptureContext context) throws IntegrationException {
		return IntegrationContextMapper.toTransactionResult(delegate.capture(entities.getStore(), entities.getCustomer(),
				entities.getOrder(), entities.getCapturableTransaction(), context.getConfiguration(),
				resolveModule(context.getModule())));
	}

	@Override
	public TransactionResult refund(PaymentRefundContext context) throws IntegrationException {
		return IntegrationContextMapper.toTransactionResult(delegate.refund(entities.isPartialRefund(),
				entities.getStore(), entities.getRefundableTransaction(), entities.getOrder(), context.getAmount(),
				context.getConfiguration(), resolveModule(context.getModule())));
	}

	private IntegrationModule resolveModule(PaymentRequestContext context) {
		if (entities.getIntegrationModule() != null) {
			return entities.getIntegrationModule();
		}
		return IntegrationContextMapper.toModule(context.getModule());
	}

	private IntegrationModule resolveModule(IntegrationModuleDto module) {
		if (entities.getIntegrationModule() != null) {
			return entities.getIntegrationModule();
		}
		return IntegrationContextMapper.toModule(module);
	}

}
