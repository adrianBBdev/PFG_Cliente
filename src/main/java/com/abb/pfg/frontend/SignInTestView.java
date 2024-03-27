package com.abb.pfg.frontend;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("/signInTest")
public class SignInTestView extends VerticalLayout{
	
	public SignInTestView() {
		Button confirmButton = new Button("Confirmar");
		confirmButton.addClickListener(event -> 
				confirmButton.getUI().ifPresent(ui -> ui.navigate("/login")));
		add(new H1("Registro"),
				this.init(),
				confirmButton);
		this.setAlignItems(FlexComponent.Alignment.CENTER);
	}
	
	private Component init() {
		
		VerticalLayout verticalLayout = new VerticalLayout();
		Label userLabel = new Label("Correo");
		TextField userTextField = new TextField();
		Label passwordLabel = new Label("Contraseña");
		TextField passwordTextField = new TextField();
		ComboBox<String> comboBox = new ComboBox<>("Rol");
		comboBox.setItems("Empresa", "Estudiante");
		comboBox.addDetachListener(event -> 
				this.evaluateComboBox(comboBox));
		verticalLayout.add(new HorizontalLayout(userLabel, userTextField),
				new HorizontalLayout(passwordLabel, passwordTextField),
				comboBox);
		verticalLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		return verticalLayout;
	}
	
	private void evaluateComboBox(ComboBox comboBox) {
		if(comboBox.getValue().equals("Estudiante")) {
			System.out.println("HOLA");
		} else if(comboBox.getValue().equals("Empresa")) {
			
		}
	}
}
