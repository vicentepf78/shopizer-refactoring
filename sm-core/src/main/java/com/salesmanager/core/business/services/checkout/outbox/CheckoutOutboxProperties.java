package com.salesmanager.core.business.services.checkout.outbox;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "checkout.outbox")
public class CheckoutOutboxProperties {

	private boolean enabled = false;
	private long dispatcherIntervalMs = 5000L;

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public long getDispatcherIntervalMs() {
		return dispatcherIntervalMs;
	}

	public void setDispatcherIntervalMs(long dispatcherIntervalMs) {
		this.dispatcherIntervalMs = dispatcherIntervalMs;
	}

}
