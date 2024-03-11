
package abb.pruebas.frontend;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.login.AbstractLogin;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;

import abb.pruebas.frontend.commons.Constants;

@Route(Constants.LOGIN_PATH)
@RouteAlias(Constants.MAIN_PATH)
@RouteAlias(Constants.EMPTY_PATH)
@PageTitle("J4S - Iniciar sesión")
public class LoginView extends VerticalLayout {
	
	private static final long serialVersionUID = 1096561116011164613L;
	private static final String LOGIN_TAG = "Iniciar sesión";
	private static final String REGISTER_TAG = "Registrarse";
	private static final String ENTER_TAG = "Acceder";
	private static final String GUEST_TAG = "Acceder como invitado";
		
	private VerticalLayout mainLayout;
	private LoginForm loginForm;
	private LoginI18n loginI18n;
	private LoginI18n.Form i18nForm;
	private Button guestButton, signInButton;
	
	
	public LoginView() {
		this.init();
		add(mainLayout); 
		this.setAlignItems(FlexComponent.Alignment.CENTER);
	}
	
	private void init() {
		this.setLoginForm();
		this.setButtonsLayout();
		mainLayout = new VerticalLayout();
		mainLayout.add(loginForm, new HorizontalLayout(guestButton, signInButton));
		mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);
	}
	
	private void setLoginForm() {
		this.setI18nForm();
		loginForm = new LoginForm();
		loginForm.setForgotPasswordButtonVisible(false);
		loginForm.addLoginListener(authenticationEvent -> {
			boolean isAuthenticated = authenticate(authenticationEvent);
			if(isAuthenticated) {
				this.getUI().ifPresent(ui -> ui.navigate(Constants.ALLJOBOFFERS_PATH));
			} else {
				loginForm.setError(true);
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
		signInButton = new Button(REGISTER_TAG);
		guestButton.addClickListener(event -> 
				guestButton.getUI().ifPresent(ui -> ui.navigate("/Test")));
		signInButton.addClickListener(event -> 
				signInButton.getUI().ifPresent(ui -> ui.navigate(Constants.SIGNIN_PATH)));
	}
	
	private boolean authenticate (AbstractLogin.LoginEvent event) {
		if(!event.getUsername().isEmpty() && !event.getPassword().isEmpty()) {
			return true;
		} else {
			return false;
		}
	}
}
