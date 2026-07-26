package com.salesmanager.contracts.content.page;

import com.salesmanager.contracts.content.common.Content;

public class ContentPage extends Content {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean linkToMenu;
	public boolean isLinkToMenu() {
		return linkToMenu;
	}
	public void setLinkToMenu(boolean linkToMenu) {
		this.linkToMenu = linkToMenu;
	}

}
