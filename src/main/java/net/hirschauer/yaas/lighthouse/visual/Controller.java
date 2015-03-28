package net.hirschauer.yaas.lighthouse.visual;

import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;

public abstract class Controller {
	
	public void showMenuItems(MenuBar menuBar) {

		toggleMenuItems(menuBar, true);
	}
	
	public void hideMenuItems(MenuBar menuBar) {

		toggleMenuItems(menuBar, false);
	}
		
	private void toggleMenuItems(MenuBar menuBar, boolean visible) {
		
		for (Menu menu : menuBar.getMenus()) {
			
			if (menu.getId() != null && menu.getId().startsWith(getMenuId())) {					
				menu.setVisible(visible);
//			} else {
//				for (MenuItem item : menu.getItems()) {
//					if (item.getId() != null && item.getId().startsWith("configuration")) {					
//						item.setVisible(visible);
//					}
//				}
			}
		}
	}
	
	protected abstract String getMenuId();
}
