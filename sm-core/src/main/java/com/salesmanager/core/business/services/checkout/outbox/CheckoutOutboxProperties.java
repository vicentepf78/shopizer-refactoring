package com.salesmanager.core.business.services.checkout.outbox;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "checkout.outbox")
public class CheckoutOutboxProperties {

	private boolean enabled = false;
	private Dispatcher dispatcher = new Dispatcher();

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public Dispatcher getDispatcher() {
		return dispatcher;
	}

	public void setDispatcher(Dispatcher dispatcher) {
		this.dispatcher = dispatcher;
	}

	public long getDispatcherIntervalMs() {
		return dispatcher.getIntervalMs();
	}

	public static class Dispatcher {

		private long intervalMs = 5000L;

		public long getIntervalMs() {
			return intervalMs;
		}

		public void setIntervalMs(long intervalMs) {
			this.intervalMs = intervalMs;
		}

	}

}
