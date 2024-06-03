/**
 * 
 */
package com.abb.pfg.views;

import org.json.JSONException;
import org.json.JSONObject;

import com.abb.pfg.custom.CustomAppLayout;
import com.abb.pfg.custom.CustomNotification;
import com.abb.pfg.custom.CustomSelectAreas;
import com.abb.pfg.custom.CustomTextArea;
import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.HttpRequest;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import lombok.NoArgsConstructor;

/**
 * View to create a new job offer
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route(Constants.CREATE_OFF_PATH)
@PageTitle("J4S - Crear oferta")
@NoArgsConstructor
public class CreateJobOfferView extends CustomAppLayout implements BeforeEnterObserver {
	
	private static final long serialVersionUID = -903601905715997700L;
	private static final String HEADER_TAG = "Crear oferta";
	
	private VerticalLayout mainLayout, jobOfferDetailsLayout;
	private HorizontalLayout buttonsLayout;
	private TextField titleField, addressField, cityField;
	private CustomTextArea descriptionField;
	private NumberField salaryField;
	private IntegerField vacanciesField;
	private DatePicker startDate, endDate;
	private Select<String> modalitySelect;
	private CustomSelectAreas areaSelect;
	private String username;
	
	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		var userType = (String) VaadinSession.getCurrent().getAttribute("role");
		if(authToken == null || !userType.equals(Constants.CMP_ROLE)) {
			event.forwardTo(LoginView.class);
			return;
		}
		username = (String) VaadinSession.getCurrent().getAttribute("username");
		init();
	}
	
	/**
	 * Initialices the view components
	 *
	 */
	private void init() {
		mainLayout = new VerticalLayout();		//Inicializamos el layout principal
		mainLayout.setAlignItems(Alignment.CENTER);
		mainLayout.setWidthFull();
		getJobOfferDetailsLayout();
		getButtonsLayout();
		mainLayout.add(jobOfferDetailsLayout, buttonsLayout);
		var baseVerticalLayout = new VerticalLayout();
		baseVerticalLayout.add(new H1(HEADER_TAG), mainLayout);
		baseVerticalLayout.setAlignItems(Alignment.CENTER);
		this.setContent(baseVerticalLayout);
	}
	
	/**
	 * Gets all the components needed to show the job offer info
	 * 
	 */
	private void getJobOfferDetailsLayout() {
		getJobOfferComponents();
		var horizontalLayout1 = new HorizontalLayout();
		horizontalLayout1.add(areaSelect, modalitySelect);
		horizontalLayout1.setWidthFull();
		var horizontalLayout2 = new HorizontalLayout();
		horizontalLayout2.add(salaryField, vacanciesField);
		horizontalLayout2.setWidthFull();
		var horizontalLayout3 = new HorizontalLayout();
		horizontalLayout3.add(addressField, cityField);
		horizontalLayout3.setWidthFull();
		var horizontalLayout4 = new HorizontalLayout();
		horizontalLayout4.add(startDate, endDate);
		horizontalLayout4.setWidthFull();
		jobOfferDetailsLayout.add(titleField, horizontalLayout1, 
				horizontalLayout2, horizontalLayout3, horizontalLayout4, descriptionField);
	}
	
	private void getJobOfferComponents() {
		titleField = new TextField(Constants.TITLE_TAG);
		titleField.setWidthFull();
		titleField.setMaxLength(50);
		descriptionField = new CustomTextArea(Constants.DESC_TAG, "");
		descriptionField.setWidth("70%");
		addressField = new TextField(Constants.ADDRESS_TAG);
		addressField.setWidth("60%");
		addressField.setMaxLength(100);
		cityField = new TextField(Constants.CITY_TAG);
		cityField.setWidth("60%");
		cityField.setMaxLength(50);
		getAreaAndModality();
		getDateComponents();
		getNumberFields();
		jobOfferDetailsLayout = new VerticalLayout();
		jobOfferDetailsLayout.setAlignItems(Alignment.CENTER);
		jobOfferDetailsLayout.setWidth("70%");
	}
	
	/**
	 * Gets the duration components of the job offer
	 *
	 */
	private void getDateComponents() {
		DatePicker.DatePickerI18n singleFormatI18n = new DatePicker.DatePickerI18n();
		singleFormatI18n.setDateFormat("dd-MM-yyyy");
		startDate = new DatePicker(Constants.START_TAG);
		startDate.setI18n(singleFormatI18n);
		startDate.setWidth("60%");
		endDate = new DatePicker(Constants.END_TAG);
		endDate.setWidth("60%");
		endDate.setI18n(singleFormatI18n);
	}
	
	/**
	 * Gets the number fields with the parameters of the job offer
	 * 
	 */
	private void getNumberFields() {
		salaryField = new NumberField();
		salaryField.setLabel(Constants.SLRY_TAG);
		salaryField.setWidth("60%");
		Div euroSufix = new Div();
		euroSufix.setText("€");
		salaryField.setSuffixComponent(euroSufix);
		vacanciesField = new IntegerField();
		vacanciesField.setLabel(Constants.VACANCIES_TAG);
		vacanciesField.setStepButtonsVisible(true);
		vacanciesField.setMin(1);
		vacanciesField.setMax(10);
		vacanciesField.setWidth("60%");
	}
	
	/**
	 * Gets the area, modality, description and number fields of the selected job offer
	 * 
	 */
	private void getAreaAndModality() {
		areaSelect = new CustomSelectAreas();
		areaSelect.setWidth("60%");
		modalitySelect = new Select<>();
		modalitySelect.setEmptySelectionAllowed(true);
		modalitySelect.setLabel(Constants.MODALITY_TAG);
		modalitySelect.setItems(Constants.PRES_TAG, Constants.HYBR_TAG, Constants.TELE_TAG);
		modalitySelect.setWidth("60%");
	}
	
	/**
	 * Gets the layout which will show the option buttons to the user
	 * 
	 */
	private void getButtonsLayout() {
		buttonsLayout = new HorizontalLayout();
		var createButton = new Button("Crear oferta");
		createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		createButton.addClickListener(event -> createButtonListener());
		var cancelButton = new Button(Constants.CANCEL_TAG);
		cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		cancelButton.addClickListener(event -> {
			this.getUI().ifPresent(ui -> ui.navigate(Constants.OFFERS_PATH));
		});
		buttonsLayout.add(createButton, cancelButton);
	}
	
	private void createButtonListener() {
		if(arefieldsOK()) {
			if(sendCreateJobOfferRequest(username)) {
				new CustomNotification(Constants.POST_OFF_MSG, NotificationVariant.LUMO_SUCCESS);
				this.getUI().ifPresent(ui -> ui.navigate(Constants.OFFERS_PATH));
				return;
			}
			new CustomNotification(Constants.POST_OFF_ERR, NotificationVariant.LUMO_WARNING);
			return;
		}
		new CustomNotification(Constants.ERROR_TEXT, NotificationVariant.LUMO_WARNING);
	}
	
	/**
	 * Validates all the the components before sending the http post request
	 * 
	 * @return boolean - true if fields are OK, false if not
	 */
	private boolean arefieldsOK() {
		if(titleField.getValue().isBlank() || areaSelect.getValue() == null 
				|| modalitySelect.getValue() == null || salaryField.getValue() == null
				|| vacanciesField.getValue() == null || addressField.getValue().isBlank()
				|| cityField.getValue().isBlank() || descriptionField.getValue().isBlank()
				|| startDate.getValue() == null || endDate.getValue() == null) {
			return false;
		}
		return true;
	}
	
	/**
	 * Sends a http post request to create a new job offer
	 * 
	 * @param username - company's username
	 * @return boolean - true if it has been created, false if not
	 */
	private boolean sendCreateJobOfferRequest(String username) {
		var httpRequest = new HttpRequest(Constants.OFF_REQ);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		var requestBody = parseJSONOffer(username);
		return httpRequest.executeHttpPost(authToken, requestBody);
	}
	
	/**
	 * Parses to a JSON object all the job offer info
	 * 
	 * @param username - company's username
	 * @return String - request body
	 */
	private String parseJSONOffer(String username) {
		try {
			var jsonObject = new JSONObject();
			jsonObject.put("title", titleField.getValue());
			jsonObject.put("description", descriptionField.getValue());
			jsonObject.put("startDate", startDate.getValue());
			jsonObject.put("endDate", endDate.getValue());
			jsonObject.put("modality", modalitySelect.getValue());
			jsonObject.put("vacancies", vacanciesField.getValue());
			jsonObject.put("salary", salaryField.getValue());
			jsonObject.put("address", addressField.getValue());
			jsonObject.put("city", cityField.getValue());
			var areaValue = sendGetAreaRequest(areaSelect.getValue());
			jsonObject.put("area", new JSONObject(areaValue));
			var companyValue = sendGetCompanyRequest(username);
			jsonObject.put("company", new JSONObject(companyValue));
			return jsonObject.toString();
		} catch (JSONException e) {
			System.err.println("Error al parsear el objeto JSON: " + e.getMessage());
			return null;
		}
	}
	
	/**
	 * Gets the area JSON object
	 * 
	 * @param areaValue - area's name
	 * @return String - response body
	 */
	private String sendGetAreaRequest(String areaValue) {
		var httpRequest = new HttpRequest(Constants.AREAS_REQ + "/area?name=" + areaValue);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpGet(authToken);
	}
	
	/**
	 * Gets the company JSON object
	 * 
	 * @param username - company's username
	 * @return String - response body
	 */
	private String sendGetCompanyRequest(String username) {
		var httpRequest = new HttpRequest(Constants.CMP_REQ + "/company?username=" + username);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpGet(authToken);
	}
}
