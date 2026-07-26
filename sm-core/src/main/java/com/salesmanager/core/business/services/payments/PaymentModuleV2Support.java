package com.salesmanager.core.business.services.payments;

import com.salesmanager.core.modules.integration.payment.model.PaymentModule;
import com.salesmanager.core.modules.integration.payment.model.PaymentModuleV2;

final class PaymentModuleV2Support {

	private PaymentModuleV2Support() {
	}

	static PaymentModuleV2 resolve(PaymentModule module, LegacyPaymentEntityBundle entities) {
		if (module instanceof PaymentModuleV2) {
			return (PaymentModuleV2) module;
		}
		return new LegacyPaymentModuleBridge(module, entities);
	}

}
