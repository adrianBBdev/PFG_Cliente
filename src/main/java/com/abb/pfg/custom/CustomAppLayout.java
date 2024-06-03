package com.abb.pfg.custom;

import com.abb.pfg.utils.Constants;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.Scroller;
import com.vaadin.flow.server.VaadinSession;

/**
 * Custom app layout used to display all user options
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
public class CustomAppLayout extends AppLayout {

	private static final long serialVersionUID = -2237204727083010500L;
	private CustomHeaderButtonsLayout buttonsHeaderLayout;
	private CustomSideNav customSideNav;
	
	/**
	 * Default class constructor
	 * 
	 */
	public CustomAppLayout() {
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		if(authToken == null) {
			this.getUI().ifPresent(ui -> ui.navigate(Constants.LOGIN_PATH));
			return;
		}
		getHeaderContent();
	}
	
	/**
	 * Gets the header components of the current view
	 * 
	 */
	private void getHeaderContent() {
		buttonsHeaderLayout = new CustomHeaderButtonsLayout();
		var drawerToggle = new DrawerToggle();
		var username = (String) VaadinSession.getCurrent().getAttribute("username");
		var userType = (String) VaadinSession.getCurrent().getAttribute("role");
		customSideNav = new CustomSideNav(username, userType);
		var scroller = new Scroller(customSideNav);
		addToDrawer(scroller);
		addToNavbar(drawerToggle, new H3(Constants.APP_NAME), buttonsHeaderLayout);
	}
}
