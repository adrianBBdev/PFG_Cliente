package com.abb.pfg.custom;

import org.json.JSONObject;

import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.HttpRequest;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.VaadinSession;

/**
 * Custom header buttons layout to show profile and sign out options
 *
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
public class CustomHeaderButtonsLayout extends Composite<VerticalLayout>{

	private static final long serialVersionUID = 5415713916815872480L;
	//Tags
	private static final String PROFILE_TAG = "Ver perfil";
	private static final String SIGNOUT_TAG = "Cerrar sesión";
	private static final String SIGNUP_TAG = "Registrarse";
	private static final String SIGNIN_TAG = "Iniciar sesión";
	private static final String ADMIN_TAG = "Administrador";
	//Elementos
	private HorizontalLayout headerLayout;
	private Button signUpButton, signInButton;
	private MenuBar menuBar;
	private String name;

	/**
	 * Default class constructor
	 * 
	 * @param goBackPath
	 */
	public CustomHeaderButtonsLayout() {
		getContent().setWidthFull();
		getContent().setAlignItems(Alignment.END);
		headerLayout = new HorizontalLayout();
		headerLayout.setWidth("min-content");
		init();
		getContent().add(headerLayout);
	}

	/**
	 * initializes view components
	 * 
	 */
	private void init() {
		var userRole = (String) VaadinSession.getCurrent().getAttribute("role");
		if(userRole.equals("GUEST")) {
			signUpButton = new Button(SIGNUP_TAG, new Icon(VaadinIcon.FORM));
			signUpButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			signUpButton.addClickListener(event -> signUpListener());
			signInButton = new Button(SIGNIN_TAG, new Icon(VaadinIcon.SIGN_IN));
			signInButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			signInButton.addClickListener(event -> logOutListener());
			headerLayout.add(signUpButton, signInButton);
		} else {
			getCustomAvatarMenu(userRole);
			headerLayout.add(menuBar, new H5(name));
		}
	}

	/**
	 * Listener assigned to the log out button option
	 * 
	 */
	private void logOutListener() {
		getUI().ifPresent(ui -> ui.navigate(Constants.LOGIN_PATH));
		VaadinSession.getCurrent().close();
	}

	/**
	 * Listener assigned to the profile button option
	 * 
	 */
	private void profileListener() {
		getUI().ifPresent(ui -> ui.navigate(Constants.PROFILE_PATH));
	}

	/**
	 * Listener assigned to the sign up button option
	 * 
	 */
	private void signUpListener() {
		getUI().ifPresent(ui -> ui.navigate(Constants.SIGNUP_PATH));
	}
	
	/**
	 * Sets up the avatar menu with options
	 * 
	 * @param userRole - user's role
	 */
	private void getCustomAvatarMenu(String userRole) {
		var customAvatar = new Avatar();
		if(userRole.equals(Constants.ADM_ROLE)) {
			customAvatar = new Avatar(Constants.ADMIN_TAG);
		} else {
			var username = (String) VaadinSession.getCurrent().getAttribute("username");
			var profilePicture = sendGetUserInfo(username, userRole);
			customAvatar = getCustomAvatar(name, profilePicture);
		}
		menuBar = new MenuBar();
		menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
		var menuItem = menuBar.addItem(customAvatar);
		var subMenu = menuItem.getSubMenu();
		subMenu.addItem(PROFILE_TAG).addClickListener(event -> profileListener());
        subMenu.addItem(SIGNOUT_TAG).addClickListener(event -> logOutListener());
	}
	
	/**
	 * Gets the user's avatar to display the profile picture 
	 * 
	 * @param name - user's name
	 * @param profilePicture - user's profile picture file name
	 * @return Avatar - user's avatar
	 */
	private Avatar getCustomAvatar(String name, String profilePicture) {
		if(profilePicture.isBlank()) {
			return new Avatar(name);
		}
		return new CustomAvatar(profilePicture);
	}
	
	/**
	 * Sends the http request to get the user info
	 * 
	 * @param username - user's username
	 * @param userRole - user's role
	 * @return String - response body
	 */
	private String sendGetUserInfo(String username, String userRole) {
		var getUrl = new String();
		switch(userRole) {
			case Constants.STD_ROLE:
				getUrl = Constants.STD_REQ + "/student?username=" + username;
				break;
			case Constants.CMP_ROLE:
				getUrl = Constants.CMP_REQ + "/company?username=" + username;
				break;
			default:
				getUrl = Constants.ADMIN_REQ;
				break;
		}
		if(getUrl == Constants.ADMIN_REQ) {
			return ADMIN_TAG;
		}
		var httpRequest = new HttpRequest(getUrl);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		var responseBody = httpRequest.executeHttpGet(authToken);
		if(responseBody == null) {
			return null;
		}
		var jsonObject = new JSONObject(responseBody);
		name = jsonObject.getString("name");
		return jsonObject.getString("profilePicture");
	}
}
