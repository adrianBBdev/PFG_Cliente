package com.abb.pfg.frontend.config;

import org.springframework.stereotype.Component;



/**
 * 
 * 
 * @author Adrian Barco Barona
 * @version
 *
 */
@Component
public class SecurityService {
	private static final String LOGOUT_SUCCESS_URL = "/login";
	
	/*public UserDetails getAuthenticatedUser() {
		SecurityContext securityContext = SecurityContextHolder.getContext();
		Object principal = securityContext.getAuthentication().getPrincipal();
		
		if(principal instanceof UserDetails) {
			return (UserDetails) securityContext.getAuthentication().getPrincipal();
		}
		
		return null;
	}
	
	public void logout() {
		UI.getCurrent().getPage().setLocation(LOGOUT_SUCCESS_URL);
		SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
		logoutHandler.logout(VaadinServletRequest.getCurrent().getHttpServletRequest(), null, null);
	}*/
	
}
