package com.abb.pfg.frontend;

import com.abb.pfg.frontend.commons.Constants;
import com.abb.pfg.frontend.components.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Class which represents the profile view of a company
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route(Constants.COMPANY_PATH)
@PageTitle("J4S - Empresa")
public class CompanyView extends MainLayout implements HasUrlParameter<String>{
	
	private static final String HEADER_TAG = "Perfil de empresa";
	private static final String NAME_TAG = "Nombre de la empresa";
	
	private VerticalLayout mainLayout, contentLayout, detailsLayout;
	private HorizontalLayout buttonsLayout;
	private FormLayout detailsFormLayout;
	private TextField nameField, cifField, countryField;
	private TextArea descriptionField;
	private Button goBackButton, jobOfferButton, multimediaButton;
	
	public CompanyView() {
		this.init();
		contentLayout.add(new H1(HEADER_TAG) ,mainLayout);
		setContent(contentLayout);
	}
	
	private void init() {
		contentLayout = new VerticalLayout();
		contentLayout.setAlignItems(Alignment.CENTER);
		nameField = new TextField(NAME_TAG);
		nameField.setReadOnly(true);
		nameField.setWidth(Constants.WIDTH);
		cifField = new TextField(Constants.CIF_TAG);
		cifField.setReadOnly(true);
		countryField = new TextField(Constants.COUNTRY_TAG);
		countryField.setReadOnly(true);
		descriptionField = new TextArea(Constants.DESC_TAG);
		descriptionField.setWidth(Constants.WIDTH);
		descriptionField.setMaxLength(Constants.DESC_LENGTH);
		descriptionField.setValueChangeMode(ValueChangeMode.EAGER);
		descriptionField.addValueChangeListener(e -> {
		    e.getSource()
		            .setHelperText(e.getValue().length() + "/" + Constants.DESC_LENGTH);
		});
		descriptionField.setReadOnly(true);
		detailsFormLayout= new FormLayout();
		detailsFormLayout.add(cifField, countryField);
		detailsLayout = new VerticalLayout();
		detailsLayout.setWidth(Constants.WIDTH);
		detailsLayout.setAlignItems(Alignment.CENTER);
		detailsLayout.add(detailsFormLayout);
		this.createButtonsLayout();
		mainLayout = new VerticalLayout();
		goBackButton = new Button(Constants.GOBACK_TAG);
		goBackButton.addClickListener(goBackEvent -> 
			this.getUI().ifPresent(ui -> ui.navigate(Constants.ALLUSERS_PATH)));
		mainLayout.add(nameField, descriptionField, detailsLayout, buttonsLayout, goBackButton);
		mainLayout.setAlignItems(Alignment.CENTER);
	}
	
	private void createButtonsLayout() {
		buttonsLayout = new HorizontalLayout();
		multimediaButton = new Button("Ver contenido multimedia");
		jobOfferButton = new Button("Ver ofertas de trabajo");
		jobOfferButton.addClickListener(event -> 
			this.getUI().ifPresent(ui -> ui.navigate(Constants.ALLJOBOFFERS_PATH)));
		buttonsLayout.add(multimediaButton, jobOfferButton);
	}

	@Override
	public void setParameter(BeforeEvent event, String parameter) {
		this.nameField.setValue(parameter);
	}
}
