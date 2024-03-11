package com.abb.pfg.frontend.views;

import org.springframework.beans.factory.annotation.Autowired;

import com.abb.pfg.frontend.config.SecurityService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Main view implemented with Vaadin
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route("/Principal")
@PageTitle("J4S - Principal")
@AnonymousAllowed
public class MainView {
	
	private Button logoutButton;
	private SecurityService securityService;
	
	
	public MainView(@Autowired SecurityService securityServices) {
		
		/*if (securityService.getAuthenticatedUser() != null) {
            logoutButton = new Button("Logout", click ->
                    securityService.logout());
        }*/
	}
}
