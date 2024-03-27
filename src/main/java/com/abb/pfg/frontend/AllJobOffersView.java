package com.abb.pfg.frontend;

import com.abb.pfg.frontend.commons.Constants;
import com.abb.pfg.frontend.components.CustomBasicDialog;
import com.abb.pfg.frontend.components.JobOfferListItemLayout;
import com.abb.pfg.frontend.components.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.CheckboxGroup;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Class which represents the view of all job offers
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route("/allJobOffers")
@PageTitle("J4S - Ofertas disponibles")
public class AllJobOffersView extends MainLayout {
	
	private static final String HEADER_TAG = "Ofertas disponibles";
	private static final String ERROR_MESSAGE = "Rellene correctamente los filtros deseados";
	private static final String FILTER_TAG = "Filtrar";
	
	private HorizontalLayout mainLayout;
	private VerticalLayout contentLayout, leftLayout, rightLayout, customLayout;
	private TextField cityField;
	private Button filterButton;
	private CheckboxGroup<String> modalityBox, durationBox;
	private Dialog filterDialog;
	
	public AllJobOffersView() {
		init();
		contentLayout.add(new H1(HEADER_TAG), mainLayout);
		setContent(contentLayout);
	}
	
	private void init() {
		rightLayout = new VerticalLayout();
		this.createCustomLayout(4);
		mainLayout = new HorizontalLayout();
		rightLayout.add(customLayout);
		this.createLeftLayout();
		mainLayout.add(leftLayout, rightLayout);
	}
	
	private void createLeftLayout() {
		contentLayout = new VerticalLayout();
		contentLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		leftLayout = new VerticalLayout();
		cityField = new TextField(Constants.CITY_TAG);
		modalityBox = new CheckboxGroup<>(Constants.MODALITY_TAG);
		modalityBox.setItems("Presencial", "Telemático", "Híbrido");
		durationBox = new CheckboxGroup<>(Constants.DURATION_TAG);
		durationBox.setItems("Menos de 6 meses", "Más de 6 meses");
		filterButton = new Button(FILTER_TAG);
		filterButton.addClickListener(filterEvent ->{
			if(cityField.isEmpty() && modalityBox.isEmpty() && durationBox.isEmpty()) {
				filterDialog = new CustomBasicDialog(Constants.ERROR_TAG, ERROR_MESSAGE);
				filterDialog.open();
			}
		});
		leftLayout.add(cityField, modalityBox, durationBox, filterButton);
	}
	
	private void createCustomLayout(int size) {
		for(int i=0; i<size; i++) {
			customLayout = new JobOfferListItemLayout();
			rightLayout.add(customLayout);
			customLayout.setAlignItems(FlexComponent.Alignment.CENTER);
			customLayout.setWidth(Constants.WIDTH);
		}
	}
}
