package com.abb.pfg.frontend.views;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

/**
 * Login view implemented with Vaadin
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route("/login")
@PageTitle("J4S - Iniciar sesion")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver{

	private LoginI18n loginI18n;
	private LoginI18n.Form i18nForm;
	private LoginForm loginForm;
	private Button signInButton, guestAccessButton;
	private HorizontalLayout horizontalLayout;
	
	public LoginView() {
		this.initComponents();
		
		setAlignItems(Alignment.CENTER);
		
		add(new H1("Bienvenido a Jobs for Students"), loginForm, horizontalLayout);
	}
	
	@Override
	public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
		if(beforeEnterEvent.getLocation()
				.getQueryParameters()
				.getParameters()
				.containsKey("error")) {
			loginForm.setError(true);
		}
	}
	
	private void initComponents() {
		loginI18n = LoginI18n.createDefault();
		i18nForm = loginI18n.getForm();
		i18nForm.setTitle("Inicio de sesión");
		i18nForm.setUsername("Nombre de usuario");
		i18nForm.setPassword("Contraseña");
		i18nForm.setSubmit("Iniciar sesión");
		loginI18n.setForm(i18nForm);
		loginForm = new LoginForm();
		loginForm.setI18n(loginI18n);
		
		horizontalLayout = new HorizontalLayout();
		signInButton = new Button("Registrarse");
		guestAccessButton = new Button("Acceder como invitado");
		horizontalLayout.add(signInButton, guestAccessButton);
	}
}
