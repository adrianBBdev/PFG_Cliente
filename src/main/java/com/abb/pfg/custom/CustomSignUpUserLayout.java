package com.abb.pfg.custom;

import com.abb.pfg.utils.Constants;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Layout which has all the components needed to sign up a user
 *
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class CustomSignUpUserLayout extends VerticalLayout{

	private static final long serialVersionUID = 8999087538630823959L;
	//Componentes
	private EmailField emailField;
	private PasswordField passwordField1, passwordField2;
	private Button saveButton, cancelButton;
	
	/**
	 * Default class constructor
	 * 
	 * @param userCategory - user's role to create
	 */
	public CustomSignUpUserLayout(String userCategory) {
		var userDataLayout = new VerticalLayout();				//Inicializamos el layout del formulario inicial
		this.setAlignItems(Alignment.CENTER);
		userDataLayout.setAlignItems(Alignment.CENTER);
		userDataLayout.setWidthFull();
		setEmailField();
		setPasswordFields();
		userDataLayout.add(emailField, passwordField1, passwordField2);//Creamos campos de passwords
		if(userCategory.equals(Constants.ADM_ROLE)) {
			saveButton = new Button(Constants.CREATE_TAG);
			saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			cancelButton = new Button(Constants.CANCEL_TAG);
			cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			var buttonsLayout = new VerticalLayout(new HorizontalLayout(saveButton,cancelButton));
			buttonsLayout.setAlignItems(Alignment.CENTER);
			this.add(userDataLayout, buttonsLayout);
			return;
		}
		this.add(userDataLayout);
	}
	
	/**
	 * Sets up the email field
	 * 
	 */
	private void setEmailField() {
		emailField = new EmailField(Constants.EMAIL_TAG);	//Creamos el campo del email para el registro
		emailField.setMaxLength(Constants.FIELDS_MAX_LENGTH);
		emailField.setClearButtonVisible(true);
		emailField.setPrefixComponent(VaadinIcon.ENVELOPE.create());
		emailField.setErrorMessage(Constants.EMAIL_FORMAT_ERROR);
		emailField.setWidth("30%");
	}
	
	/**
	 * Gets the password fields for authentication
	 *
	 */
	private void setPasswordFields() {
		passwordField1 = new PasswordField(Constants.PASSWORD_TAG);		//Creamos el campo de la password para el registro
		passwordField1.setMinLength(Constants.PASSWD_MIN_LENGTH);
		passwordField1.setMaxLength(Constants.PASSWD_MAX_LENGTH);
		passwordField1.setWidth("30%");
		passwordField1.setErrorMessage(Constants.PASSWD_LENGTH_ERROR);
		passwordField2 = new PasswordField(Constants.CONFIRMPASS_TAG);			//Creamos el campo de la repeticion de la password para el registro
		passwordField1.setMinLength(Constants.PASSWD_MIN_LENGTH);
		passwordField2.setMaxLength(Constants.PASSWD_MAX_LENGTH);
		passwordField2.setWidth("30%");
		passwordField2.addClientValidatedEventListener(event -> {
			var isInvalid = (passwordField2.getValue().equals(passwordField1.getValue())) ? false : true;
			passwordField2.setInvalid(isInvalid);
		});
		passwordField2.setErrorMessage(Constants.PASSWD_ERROR);
	}
}
