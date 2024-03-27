package com.abb.pfg.frontend;

import java.io.IOException;

import com.abb.pfg.backend.OpenBrowser;
import com.abb.pfg.frontend.commons.Constants;
import com.abb.pfg.frontend.components.CustomBasicDialog;
import com.abb.pfg.frontend.components.MainLayout;
import com.byteowls.jopencage.JOpenCageGeocoder;
import com.byteowls.jopencage.model.JOpenCageForwardRequest;
import com.byteowls.jopencage.model.JOpenCageLatLng;
import com.byteowls.jopencage.model.JOpenCageResponse;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Adrian Barco Barona
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route(Constants.JOBOFFER_PATH)
@PageTitle("J4S - Ver oferta")
public class JobOfferView extends MainLayout{
	
	private static final String HEADER_TITLE = "Oferta de trabajo";
	private static final String DETAILS_TAG = "Detalles de la oferta";
	private static final String LOCATION_TAG = "Ver localización";
	private static final String CONFIRM_TAG = "Enviar solicitud";
	private static final String DESC_TAG = "Descripción de la oferta";
	private static final String NAME_TAG = "Nombre de la empresa";
	private static final String TITLE_TAG = "Título de la oferta" ;
	private static final String JOPEN_KEY = "57f723660c0c4b14ade5831c55d7a5e5";
	private static final String DEFAULT_LOCATION = "Estadio Santiago Bernabeu, Madrid";
	private static final String MAPS_URL = "https://www.google.es/maps/place/";
	private static final String INTERNAL_ERROR = "Error interno del servidor";
	
	//private Image companyImage;
	private TextField companyField, titleField, addressField, cityField, startDate, endDate, modalityField, areaField;
	private TextArea descriptionField;
	private Button locationButton, confirmButton, cancelButton;
	private VerticalLayout mainLayout, contentLayout, titlesLayout, addressLayout;
	private HorizontalLayout buttonsLayout, headerLayout;
	private FormLayout detailsLayout, locationLayout;
	private Dialog dialog;
	private Label detailsLabel;
	
	public JobOfferView() {
		super();
		this.init();
		contentLayout.add(new H1(HEADER_TITLE), mainLayout);
		contentLayout.setAlignItems(Alignment.CENTER);
		setContent(contentLayout);
	}
	
	private void init() {
		this.createHeaderLayout();
		this.createDescriptionField();
		this.createDetailsLayout();
		this.createButtonsLayout();
		contentLayout = new VerticalLayout();
		mainLayout = new VerticalLayout();
		mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		mainLayout.setWidth(Constants.WIDTH);
		mainLayout.add(headerLayout, descriptionField, detailsLabel, 
				detailsLayout, addressLayout, buttonsLayout);
	}
	
	private void createHeaderLayout() {
		headerLayout = new HorizontalLayout();
		titlesLayout = new VerticalLayout();
		titleField = new TextField(TITLE_TAG);
		titleField.setReadOnly(true);
		titleField.setWidth(Constants.SMALL_WIDTH);
		companyField = new TextField(NAME_TAG);
		companyField.setReadOnly(true);
		companyField.setWidth(Constants.SMALL_WIDTH);
		titlesLayout.add(titleField, companyField);
		//companyImage = new Image("C/Users", "Logo");
		headerLayout.add(/*companyImage,*/ titlesLayout);
	}
	
	private void createDescriptionField() {
		descriptionField = new TextArea(DESC_TAG);
		descriptionField.setWidth(Constants.WIDTH);
		descriptionField.setMaxLength(Constants.DESC_LENGTH);
		descriptionField.setValueChangeMode(ValueChangeMode.EAGER);
		descriptionField.addValueChangeListener(e -> {
		    e.getSource()
		            .setHelperText(e.getValue().length() + "/" + Constants.DESC_LENGTH);
		});
		descriptionField.setReadOnly(true);
	}
	
	private void createDetailsLayout() {
		detailsLabel = new Label(DETAILS_TAG);
		detailsLayout = new FormLayout();
		startDate = new TextField(Constants.START_TAG);
		startDate.setReadOnly(true);
		endDate = new TextField(Constants.END_TAG);
		endDate.setReadOnly(true);
		modalityField = new TextField(Constants.MODALITY_TAG);
		modalityField.setReadOnly(true);
		areaField = new TextField(Constants.AREA_TAG);
		areaField.setReadOnly(true);
		detailsLayout.add(startDate, endDate, modalityField, areaField);
		locationLayout = new FormLayout();
		addressField = new TextField(Constants.ADDRESS_TAG);
		addressField.setReadOnly(true);
		cityField = new TextField(Constants.CITY_TAG);
		cityField.setReadOnly(true);
		locationLayout.add(addressField, cityField);
		locationButton = new Button(LOCATION_TAG);
		locationButton.setIcon(VaadinIcon.MAP_MARKER.create());
		locationButton.addClickListener(locationEvent -> {
				try {
					openDefaultBrowser(this.getLocationUrl(this.setCoordinates(DEFAULT_LOCATION)));
				} catch (IOException e) {
					dialog = new CustomBasicDialog(Constants.ERROR_TAG, INTERNAL_ERROR);
					dialog.open();
				}
			});
		addressLayout = new VerticalLayout();
		addressLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		addressLayout.add(locationLayout, locationButton);
	}

	private void createButtonsLayout() {
		buttonsLayout = new HorizontalLayout();
		confirmButton = new Button(CONFIRM_TAG);
		cancelButton = new Button(Constants.CANCEL_TAG);
		confirmButton.addClickListener(event -> {
			confirmButton.getUI().ifPresent(ui -> ui.navigate(Constants.REQUEST_PATH));
		});
		
		cancelButton.addClickListener(event -> {
			confirmButton.getUI().ifPresent(ui -> ui.navigate(Constants.ALLJOBOFFERS_PATH));
		});
		buttonsLayout.add(confirmButton, cancelButton);
	}
	
	private void openDefaultBrowser(String url) throws IOException {
		OpenBrowser openBrowser = new OpenBrowser(url);
		openBrowser.openDefaultBrowser(openBrowser.getUrl());
	}
	
	private String getLocationUrl(String coordinates) {
		return MAPS_URL + coordinates;
	}
	
	private String setCoordinates(String address) {
		JOpenCageGeocoder jOpenCageGeocoder = new JOpenCageGeocoder(JOPEN_KEY);
		JOpenCageForwardRequest request = new JOpenCageForwardRequest(DEFAULT_LOCATION);
		request.setRestrictToCountryCode("es");
		JOpenCageResponse response = jOpenCageGeocoder.forward(request);
		JOpenCageLatLng firstResultLatLng = response.getFirstPosition();
		return firstResultLatLng.getLat().toString() + "," + firstResultLatLng.getLng().toString();
	}
}
