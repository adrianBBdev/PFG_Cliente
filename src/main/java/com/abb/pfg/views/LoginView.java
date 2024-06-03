package com.abb.pfg.views;

import org.json.JSONObject;

import com.abb.pfg.custom.CustomBasicDialog;
import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.HttpRequest;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.login.AbstractLogin.LoginEvent;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinSession;

/**
 * Default login view, to authenticate the users
 *
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route(Constants.LOGIN_PATH)
@RouteAlias(Constants.MAIN_PATH)
@RouteAlias(Constants.EMPTY_PATH)
@PageTitle("J4S - Iniciar sesión")
public class LoginView extends Composite<VerticalLayout> {
	
	private static final long serialVersionUID = 1096561116011164613L;
	//Etiquetas
	private static final String LOGIN_TAG = "Iniciar sesión";
	private static final String REGISTER_TAG = "Registrarse";
	private static final String ENTER_TAG = "Acceder";
	private static final String GUEST_TAG = "Acceder como invitado";
	private static final String AUTH_ERR = "Credenciales erróneas";
	private static final String AUTH_ERR_MSG = "Comprueba que las credenciales son correctas";
	//Componentes
	private VerticalLayout mainLayout;
	private LoginForm loginForm;
	private LoginI18n loginI18n;
	private LoginI18n.Form i18nForm;
	private Button guestButton, signUpButton;
	private String username;

	/**
	 * Default class constructor
	 *
	 */
	public LoginView() {
		init();
		getContent().add(mainLayout);
		getContent().setAlignItems(FlexComponent.Alignment.CENTER);
	}

	/**
	 * Initializes all view components
	 *
	 */
	private void init() {
		setLoginForm();
		setButtonsLayout();
		mainLayout = new VerticalLayout();
		mainLayout.add(loginForm, new HorizontalLayout(guestButton, signUpButton));
		mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);
	}

	/**
	 * Builds the login form
	 *
	 */
	private void setLoginForm() {
		this.setI18nForm();
		loginForm = new LoginForm();
		loginForm.setForgotPasswordButtonVisible(false);
		loginForm.addLoginListener(authenticationEvent -> authenticationListener(authenticate(authenticationEvent)));
		loginForm.setI18n(loginI18n);
	}

	/**
	 * Builds the components login form
	 *
	 */
	private void setI18nForm() {
		loginI18n = LoginI18n.createDefault();
		i18nForm = loginI18n.getForm();
		i18nForm.setTitle(LOGIN_TAG);
		i18nForm.setUsername(Constants.USERNAME_TAG);
		i18nForm.setPassword(Constants.PASSWORD_TAG);
		i18nForm.setSubmit(ENTER_TAG);
		loginI18n.setForm(i18nForm);
	}

	/**
	 * Builds the layout that contains the action buttons
	 *
	 */
	private void setButtonsLayout() {
		guestButton = new Button(GUEST_TAG);
		signUpButton = new Button(REGISTER_TAG);
		guestButton.addClickListener(event -> authenticationListener(sendAuthenticationRequest(null, null)));
		signUpButton.addClickListener(event ->
				signUpButton.getUI().ifPresent(ui -> ui.navigate(Constants.SIGNUP_PATH)));
	}

	/**
	 * Gets the user credentials to send the authentication rrequest
	 *
	 * @param event
	 * @return
	 */
	private String authenticate (LoginEvent event) {
		username = event.getUsername();
		var password = event.getPassword();
		if(username.isBlank() || password.isBlank()) {
			return null;
		}
		return sendAuthenticationRequest(username, password);
	}

	/**
	 * Sends the request that verifies the user credentials
	 *
	 * @param username - users's username
	 * @param password - user's password
	 * @return
	 */
	private String sendAuthenticationRequest(String username, String password) {
		var httpRequest = new HttpRequest(Constants.AUTH_REQ + "/login");
		if(username != null && password != null) {
			httpRequest.setUrl(httpRequest.getUrl() + "?username=" + username + "&password=" + password);
		}
		return httpRequest.executeLoginRequest();
	}

	/**
	 * Listener assigned to the login button
	 *
	 * @param responseBody - user JSON object to analyze
	 */
	private void authenticationListener(String responseBody) {
		if(responseBody != null) {
			var jsonResponse = new JSONObject(responseBody);
			var token = jsonResponse.getString("token");
			var role = jsonResponse.getString("role");
			var isAuthenticated = (token != null) ? true : false;
			if(isAuthenticated) {
				VaadinSession.getCurrent().setAttribute("authToken", token);
				VaadinSession.getCurrent().setAttribute("role", role);
				var userTmp = (role.equals("GUEST") ? role : username);
				VaadinSession.getCurrent().setAttribute("username", userTmp);
				verifyRoleToAssignPath(role);
			} else {
				showErrorDialog(AUTH_ERR, AUTH_ERR_MSG);
			}
			return;
		}
		showErrorDialog(AUTH_ERR, AUTH_ERR_MSG);
	}

	/**
	 * Verifies the user role to assign a specified path
	 *
	 * @param role - user's role
	 */
	private void verifyRoleToAssignPath(String role) {
		if(role.equals(Constants.STD_ROLE)) {
			this.getUI().ifPresent(ui -> ui.navigate(Constants.REQ_PATH));
			return;
		}
		if(role.equals(Constants.ADM_ROLE)) {
			this.getUI().ifPresent(ui -> ui.navigate(Constants.MNG_USERS_PATH));
			return;
		}
		this.getUI().ifPresent(ui -> ui.navigate(Constants.OFFERS_PATH));
	}

	/**
	 * Shows a custom error dialog if necessary
	 *
	 * @param title - dialog title
	 * @param message - dialog message
	 */
	private void showErrorDialog(String title, String message) {
		var errorDialog = new CustomBasicDialog(title, message);
		errorDialog.open();
		loginForm.setEnabled(true);
	}
}
