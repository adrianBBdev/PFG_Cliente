package com.abb.pfg.frontend;

import com.abb.pfg.frontend.commons.Constants;
import com.abb.pfg.frontend.components.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * 
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route(Constants.ADMIN_PATH)
@PageTitle("J4S - Administrador")
public class AdminView extends MainLayout implements HasUrlParameter<String>{
	
	private static final String HEADER_TITLE = "Perfil de administrador";
	
	private VerticalLayout mainLayout, contentLayout;
	private FormLayout formLayout;
	private TextField usernameField, emailField;
	private TextArea descriptionField;
	private Button goBackButton;
	
	public AdminView(){
		init();
		contentLayout.add(new H1(HEADER_TITLE), mainLayout);
		contentLayout.setAlignItems(Alignment.CENTER);
		setContent(contentLayout);
	}
	
	private void init() {
		contentLayout = new VerticalLayout();
		usernameField = new TextField(Constants.USERNAME_TAG);
		usernameField.setReadOnly(true);
		emailField = new TextField(Constants.EMAIL_TAG);
		emailField.setReadOnly(true);
		formLayout = new FormLayout();
		formLayout.add(usernameField, emailField);
		descriptionField = new TextArea(Constants.DESC_TAG);
		descriptionField.setReadOnly(true);
		descriptionField.setWidth(Constants.WIDTH);
		setButton();
		mainLayout = new VerticalLayout();
		mainLayout.setWidth(Constants.WIDTH);
		mainLayout.add(formLayout, descriptionField, goBackButton);
		mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);
	}
	
	private void setButton() {
		goBackButton = new Button(Constants.GOBACK_TAG);
		goBackButton.addClickListener(goBackEvent -> 
				goBackButton.getUI().ifPresent(ui -> ui.navigate(Constants.ALLUSERS_PATH)));
	}

	@Override
	public void setParameter(BeforeEvent event, String parameter) {
		this.usernameField.setValue(parameter);
	}

}
