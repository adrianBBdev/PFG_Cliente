package com.abb.pfg.views;

import java.io.IOException;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.abb.pfg.frontend.commons.Constants;
import com.abb.pfg.frontend.components.CustomBasicDialog;
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

/**
 * 
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
	private static final String LOGIN_TAG = "Iniciar sesión";
	private static final String REGISTER_TAG = "Registrarse";
	private static final String ENTER_TAG = "Acceder";
	private static final String GUEST_TAG = "Acceder como invitado";
	private static final String AUTH_ERR = "Credenciales erróneas";
	private static final String AUTH_ERR_MSG = "Comprueba que las credenciales son correctas";
	//URLs
	private static final String LOGIN_REQ = "http://localhost:8081/auth/login?username=";
	private static final String OFFERS_PATH = "/availableJobOffers";
	
	private VerticalLayout mainLayout;
	private LoginForm loginForm;
	private LoginI18n loginI18n;
	private LoginI18n.Form i18nForm;
	private Button guestButton, signUpButton;
	
	
	public LoginView() {
		this.init();
		getContent().add(mainLayout); 
		getContent().setAlignItems(FlexComponent.Alignment.CENTER);
	}
	
	private void init() {
		this.setLoginForm();
		this.setButtonsLayout();
		mainLayout = new VerticalLayout();
		mainLayout.add(loginForm, new HorizontalLayout(guestButton, signUpButton));
		mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);
	}
	
	private void setLoginForm() {
		this.setI18nForm();
		loginForm = new LoginForm();
		loginForm.setForgotPasswordButtonVisible(false);
		loginForm.addLoginListener(authenticationEvent -> {
			var token = authenticate(authenticationEvent);
			var isAuthenticated = (token != null) ? true : false;
			if(isAuthenticated) {
				this.getUI().ifPresent(ui -> ui.navigate(OFFERS_PATH + "/" + token));
			} else {
				var errorDialog = new CustomBasicDialog(AUTH_ERR, AUTH_ERR_MSG);
				errorDialog.open();
				loginForm.setEnabled(true);
			}
		});
		loginForm.setI18n(loginI18n);
	}
	
	private void setI18nForm() {
		loginI18n = LoginI18n.createDefault();
		i18nForm = loginI18n.getForm();
		i18nForm.setTitle(LOGIN_TAG);
		i18nForm.setUsername(Constants.USERNAME_TAG);
		i18nForm.setPassword(Constants.PASSWORD_TAG);
		i18nForm.setSubmit(ENTER_TAG);
		loginI18n.setForm(i18nForm);
	}
	
	private void setButtonsLayout() {
		guestButton = new Button(GUEST_TAG);
		signUpButton = new Button(REGISTER_TAG);
		guestButton.addClickListener(event -> 
				guestButton.getUI().ifPresent(ui -> ui.navigate(OFFERS_PATH)));
		signUpButton.addClickListener(event -> 
				signUpButton.getUI().ifPresent(ui -> ui.navigate(Constants.SIGNUP_PATH)));
	}
	
	private String authenticate (LoginEvent event) {
		var username = event.getUsername();
		var password = event.getPassword();
		if(username.isEmpty() || password.isEmpty()) {
			return null;
		}		
		return sendAuthenticationRequest(username, password);
	}
	
	private String sendAuthenticationRequest(String username, String password) {
		var httpClient = HttpClients.createDefault();
		var urlLogin = LOGIN_REQ + username + "&password=" + password;
		var httpPost = new HttpPost(urlLogin);
		try {		//Verificar resultado de la autenticación
			var httpResponse = httpClient.execute(httpPost);
			var httpStatus = httpResponse.getStatusLine().getStatusCode();
			//Verificar si autenticacion exitosa o fallida
			if(httpStatus == HttpStatus.SC_CREATED) {
				var responseBody = EntityUtils.toString(httpResponse.getEntity());
				var jsonResponse = new JSONObject(responseBody);
				var token = jsonResponse.getString("token");
				return token;
			}
			return null;
		} catch (IOException | NullPointerException e) {
			return null;
		}
	}
}
