package com.abb.pfg.views;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;

import org.json.JSONException;
import org.json.JSONObject;

import com.abb.pfg.custom.CustomAppLayout;
import com.abb.pfg.custom.CustomAvatar;
import com.abb.pfg.custom.CustomFilesGrid;
import com.abb.pfg.custom.CustomNavigationOptionsPageLayout;
import com.abb.pfg.custom.CustomNotification;
import com.abb.pfg.custom.CustomNumElementsSelect;
import com.abb.pfg.custom.CustomTextArea;
import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.HttpRequest;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

/**
 * View that shows the details of a job offer
 *
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route(Constants.OFFER_PATH)
@PageTitle("J4S - Ver oferta")
public class JobOfferDetailsView extends CustomAppLayout implements HasUrlParameter<Long>, BeforeEnterObserver{

	private static final long serialVersionUID = 4361832876197528133L;
	//Tags
	private static final String HEADER_TAG = "Oferta";
	private static final String LOCATION_TAG = "Ver ubicación";
	private static final String GENERAL_ERR = "Se ha producido un error inesperado";
	private static final String APPLY_TAG = "Inscribirme";
	private static final String LOC_ERR = "Se ha producido un error. No se puede mostrar la localización";
	private static final String RES_ACC_MSG = "Para ver los recursos multimedia necesitas estar registrado";
	private static final String SHW_CMP_ERR = "No se ha podido obtener la empresa seleccionada";
	private static final String ADD_RES_TAG = "Añadir recurso";
	private static final String GET_RES_ERR = "No se han podido obtener los recursos solicitados";
	//Components
	private HorizontalLayout itemLayout,buttonsLayout;
	private CustomNavigationOptionsPageLayout navigationOptionsPageLayout;
	private VerticalLayout mainLayout, tabContentLayout, jobOfferDetailsLayout;
	private CustomAvatar companyAvatar;
	private H3 jobOfferTitle;
	private H5 companyName;
	private Tabs jobOfferTabs;
	private Tab offerTab, resourcesTab;
	private TextField modalityField, addressField, areaField;
	private CustomTextArea descriptionField;
	private Button editButton, locationButton, registerButton, cancelButton, saveButton, newResourceButton;
	private DatePicker startDate, endDate;
	private NumberField salaryField;
	private IntegerField vacanciesField;
	private CustomNumElementsSelect customSelect;
	private MenuBar menuBar;
	//Atributos
	private String userRole, jobOfferBody, username;
	private Long jobOfferId;
	private int numPage = 0;
	private Integer numElements;
	private String titleValue, descValue, modalityValue, areaValue, cityValue, addressValue, 
		companyValue, logoValue, startDateValue, endDateValue;
	private int vacanciesValue;
	private double salaryValue;

	public JobOfferDetailsView() {}

	@Override
	public void setParameter(BeforeEvent event, Long parameter) {
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		if(authToken == null){
			event.forwardTo(LoginView.class);
		}
		if(parameter == null) {
			event.forwardTo(AvailableOffersView.class);
			return;
		}
		jobOfferId = parameter;
	}
	
	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		userRole = (String) VaadinSession.getCurrent().getAttribute("role");
		username = (String) VaadinSession.getCurrent().getAttribute("username");
		jobOfferBody = sendJobOfferDetailsRequest(jobOfferId);
		if(jobOfferBody == null) {
			event.forwardTo(AvailableOffersView.class);
			return;
		}
		if(userRole.equals(Constants.CMP_ROLE)) {
			if(!verifyAccessPermission(username, jobOfferBody)) {
				new CustomNotification(Constants.ACC_REJ_MSG, NotificationVariant.LUMO_ERROR);
				event.forwardTo(AvailableOffersView.class);
				return;
			}
		}
		init();
	}

	/**
	 * Initializes the view components
	 *
	 */
	private void init() {
		mainLayout = new VerticalLayout();		//Inicializamos el layout principal
		mainLayout.setAlignItems(Alignment.CENTER);
		mainLayout.setWidthFull();
		setContentLayout();
		var baseVerticalLayout = new VerticalLayout();
		baseVerticalLayout.add(new H1(HEADER_TAG), mainLayout);
		baseVerticalLayout.setAlignItems(Alignment.CENTER);
		this.setContent(baseVerticalLayout);
	}
	
	/**
	 * Sets up the content of the view's content layout
	 * 
	 */
	private void setContentLayout() {
		jobOfferTabs = new Tabs();
		jobOfferTabs.setAutoselect(true);
		jobOfferTabs.addSelectedChangeListener(event -> setTabContent(event.getSelectedTab()));
		offerTab = new Tab("Detalles de la oferta");
		resourcesTab = new Tab("Recursos");
		tabContentLayout = new VerticalLayout();
		tabContentLayout.setAlignItems(Alignment.CENTER);
		jobOfferTabs.add(offerTab, resourcesTab);
		mainLayout.add(jobOfferTabs, tabContentLayout);
	}
	
	/**
	 * Builds the tab content depending on the selected tab
	 * 
	 * @param tab - selected tab
	 */
	private void setTabContent(Tab tab) {
		tabContentLayout.removeAll();
		if(tab == null) {
			return;
		}
		if(tab.equals(offerTab)) {
			setOfferDetailsLayout();
		} else if(tab.equals(resourcesTab)) {
			if(userRole.equals(Constants.GST_ROLE)) {
				new CustomNotification(RES_ACC_MSG, NotificationVariant.LUMO_WARNING);
				jobOfferTabs.setSelectedTab(offerTab);
				return;
			}
			setResourcesLayout();
		}
	}
	
	/**
	 * Sets up the components that displays the job offer details
	 * 
	 */
	private void setOfferDetailsLayout() {
		parseJobOfferJSON();
		itemLayout = new HorizontalLayout();
		itemLayout.setWidth("70%");
		jobOfferTitle = new H3(titleValue);
		jobOfferTitle.setWidthFull();
		companyName = new H5(companyValue);
		companyName.setWidthFull();
		setCutsomAvatar(logoValue);
		setJobOfferDestailsFields();
		if(userRole.equals(Constants.GST_ROLE) || userRole.equals(Constants.CMP_ROLE)) {
			itemLayout.add(companyAvatar, jobOfferDetailsLayout);
		} else {
			itemLayout.add(menuBar, jobOfferDetailsLayout);
		}
		setButtonsLayout();
		tabContentLayout.add(itemLayout, buttonsLayout);
	}
	
	/**
	 * Sets up the layout that displays the resources
	 * 
	 */
	private void setResourcesLayout() {
		newResourceButton = new Button(ADD_RES_TAG, new Icon(VaadinIcon.PLUS_CIRCLE_O));
		newResourceButton.addClickListener(event -> addNewResourceButtonListener());
		customSelect = new CustomNumElementsSelect();
		customSelect.addValueChangeListener(event -> customSelectListener(event.getValue()));
		setGridLayout();
	}
	
	/**
	 * Sets up the grid that displays the resources
	 * 
	 */
	private void setGridLayout() {
		var resourcesBody = sendGetResourcesRequest(Constants.RES_REQ + "?jobOfferCode=" + jobOfferId, numPage, numElements);
		if(resourcesBody == null) {
			new CustomNotification(GET_RES_ERR, NotificationVariant.LUMO_ERROR);
			return;
		}
		parseJSONResourcesList(resourcesBody);
	}
	
	
	
	/**
	 * Gets all the components needed to show the job offer info
	 * 
	 */
	private void setJobOfferDestailsFields() {
		jobOfferDetailsLayout = new VerticalLayout();
		jobOfferDetailsLayout.setAlignItems(Alignment.CENTER);
		getAreaAndModalityAndDescription();
		getLocationComponents();
		getDateComponents();
		getNumberFields();
		var horizontalLayout1 = new HorizontalLayout();
		horizontalLayout1.add(areaField, modalityField);
		horizontalLayout1.setWidthFull();
		var horizontalLayout2 = new HorizontalLayout();
		horizontalLayout2.add(salaryField, vacanciesField);
		horizontalLayout2.setWidthFull();
		var horizontalLayout3 = new HorizontalLayout();
		horizontalLayout3.add(startDate, endDate);
		horizontalLayout3.setWidthFull();
		jobOfferDetailsLayout.add(jobOfferTitle, companyName, horizontalLayout1, 
				horizontalLayout2, addressField, horizontalLayout3, descriptionField);
	}
	
	/**
	 * Gets the area, modality, description and number fields of the selected job offer
	 * 
	 */
	private void getAreaAndModalityAndDescription() {
		areaField = new TextField(Constants.AREA_TAG);
		areaField.setValue(areaValue);
		areaField.setReadOnly(true);
		areaField.setWidth("60%");
		modalityField = new TextField(Constants.MODALITY_TAG);
		modalityField.setValue(modalityValue);
		modalityField.setReadOnly(true);
		modalityField.setWidth("60%");
		descriptionField = new CustomTextArea(Constants.DESC_TAG, descValue);
		descriptionField.setReadOnly(true);
		descriptionField.setWidthFull();
	}
	
	/**
	 * Gets the number fields with the parameters of the job offer
	 * 
	 */
	private void getNumberFields() {
		salaryField = new NumberField();
		salaryField.setLabel(Constants.SLRY_TAG);
		salaryField.setReadOnly(true);
		salaryField.setValue(salaryValue);
		salaryField.setWidth("60%");
		Div euroSufix = new Div();
		euroSufix.setText("€");
		salaryField.setSuffixComponent(euroSufix);
		vacanciesField = new IntegerField();
		vacanciesField.setReadOnly(true);
		vacanciesField.setLabel(Constants.VACANCIES_TAG);
		vacanciesField.setValue(vacanciesValue);
		vacanciesField.setStepButtonsVisible(true);
		vacanciesField.setMin(1);
		vacanciesField.setMax(10);
		vacanciesField.setWidth("60%");
	}

	/**
	 * Gets the location components to show the job offer location
	 *
	 */
	private void getLocationComponents() {
		addressField = new TextField(Constants.ADDRESS_TAG);
		addressField.setValue(addressValue + ", " + cityValue);
		addressField.setReadOnly(true);
		addressField.setWidthFull();
		locationButton = new Button(LOCATION_TAG, new Icon(VaadinIcon.LOCATION_ARROW_CIRCLE));
		locationButton.addClickListener(event -> locationButtonListener());
	}

	/**
	 * Listener assigned to the location button to show the job offer locations
	 *
	 */
	private void locationButtonListener() {
		if(jobOfferId != null && !userRole.equals(Constants.GST_ROLE)) {
			this.getUI().ifPresent(ui -> ui.navigate(Constants.LOC_PATH + "/" + jobOfferId));
			return;
		}
		new CustomNotification(LOC_ERR, NotificationVariant.LUMO_ERROR);
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
		startDate.setReadOnly(true);
		startDate.setValue(LocalDate.parse(startDateValue));
		endDate = new DatePicker(Constants.END_TAG);
		endDate.setWidth("60%");
		endDate.setI18n(singleFormatI18n);
		endDate.setReadOnly(true);
		endDate.setValue(LocalDate.parse(endDateValue));
	}

	/**
	 * Gets the layout that contains the view's action buttons
	 *
	 */
	private void setButtonsLayout() {
		buttonsLayout = new HorizontalLayout();
		if(userRole.equals(Constants.CMP_ROLE) || userRole.equals(Constants.ADM_ROLE)) {
			editButton = new Button(Constants.EDIT_TAG, new Icon(VaadinIcon.EDIT));
			editButton.addClickListener(event -> editButtonListener(true));
			cancelButton = new Button(Constants.CANCEL_TAG);
			cancelButton.addClickListener(event -> editButtonListener(false));
			saveButton = new Button (Constants.SV_CHG_TAG);
			saveButton.addClickListener(event -> saveButtonListener());
			buttonsLayout.add(editButton);
		}
		if(userRole.equals(Constants.STD_ROLE)) {
			registerButton = new Button(APPLY_TAG, new Icon(VaadinIcon.USER_CHECK));
			registerButton.addClickListener(event -> registerButtonListener());
			var isRegistered = sendCheckIfStudentAppliedToOffer();
			if(isRegistered) {
				registerButton.setEnabled(false);
			}
			buttonsLayout.add(registerButton);
		}
		buttonsLayout.add(locationButton);
	}
	
	private boolean updateJobOfferBody() {
		try {
			var jsonObject = new JSONObject(jobOfferBody);
			jsonObject.put("salary", salaryField.getValue());
			jsonObject.put("vacancies", vacanciesField.getValue());
			jsonObject.put("address", addressField.getValue().replace(", " + jsonObject.getString("city"), ""));
			jsonObject.put("startDate", startDate.getValue());
			jsonObject.put("endDate", endDate.getValue());
			jsonObject.put("description", descriptionField.getValue());
			var jobOfferBodyUpdated = jsonObject.toString();
			if(jobOfferBody.equals(jobOfferBodyUpdated)) {
				return false;
			}
			jobOfferBody = jobOfferBodyUpdated;
			return true;
		} catch (JSONException e) {
			System.err.println("Error al parsear el objeto JSON: " + e.getMessage());
			return false;
		}
	}

	/**
	 * Verifies if a student user has applied to an offer or not
	 *
	 * @param responseBody - response body to verify
	 * @return boolean - true if the student has applied, false if not
	 */
	private boolean checkIfStudentAppliedToOffer(String responseBody) {
		try {
			var jsonObject = new JSONObject(responseBody);
			var isEmpty = jsonObject.getBoolean("empty");
			return !isEmpty;
		} catch(JSONException e) {
			System.err.println("Error al parsear el objeto JSON: " + e.getMessage());
			return false;
		}
	}
	
	/**
	 * Gets the custom layout used to navigate between pages
	 *
	 * @param isShowingFirst - true if it shows the first resource, false if not
	 * @param isShowingLast - true if it shows the last resource, false if not
	 */
	private void setNavigationOptionsPageLayout(boolean isShowingFirst, boolean isShowingLast) {
		if(navigationOptionsPageLayout == null) {
			navigationOptionsPageLayout = new CustomNavigationOptionsPageLayout(isShowingFirst, isShowingLast);
			navigationOptionsPageLayout.getNextPageButton().addClickListener(event -> nextPageListener());
			navigationOptionsPageLayout.getPrevPageButton().addClickListener(event -> prevPageListener());
			return;
		}
		navigationOptionsPageLayout.setEnabledNextButton(isShowingLast);
		navigationOptionsPageLayout.setEnabledPrevButton(isShowingFirst);
	}

	/**
	 * Gets the custom avatar located at the given path
	 *
	 * @param logo - path where the avatar is located
	 */
	private void setCutsomAvatar(String logo) {
		companyAvatar = new CustomAvatar(logo);
		if(userRole.equals(Constants.GST_ROLE)) {
			companyAvatar.setHeight("15%");
			companyAvatar.setWidth("15%");
			return;
		}
		menuBar = new MenuBar();
		menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
		var menuItem = menuBar.addItem(companyAvatar);
		var subMenu = menuItem.getSubMenu();
		subMenu.addItem("Ver " + Constants.COMPANY_TAG).addClickListener(event -> showCompanyInfoListener());
	}
	
	/**
	 * Sets the read only mode true and false alternatively
	 * 
	 */
	private void changeReadOnlyMode() {
		addressField.setReadOnly(!addressField.isReadOnly());
		startDate.setReadOnly(!startDate.isReadOnly());
		endDate.setReadOnly(!endDate.isReadOnly());
		descriptionField.setReadOnly(!descriptionField.isReadOnly());
		salaryField.setReadOnly(!salaryField.isReadOnly());
		vacanciesField.setReadOnly(!vacanciesField.isReadOnly());
	}
	
	/**
	 * Refresh page to reload the content
	 * 
	 */
	private void refreshPageContent() {
		parseJobOfferJSON();
		salaryField.setValue(salaryValue);
		vacanciesField.setValue(vacanciesValue);
		addressField.setValue(addressValue);
		startDate.setValue(LocalDate.parse(startDateValue));
		endDate.setValue(LocalDate.parse(endDateValue));
		descriptionField.setValue(descValue);
	}
	
	private boolean verifyAccessPermission(String username, String jobOfferBody) {
		try {
			var jsonObject = new JSONObject(jobOfferBody);
			var companyUsername = jsonObject.getJSONObject("company").getJSONObject("user").getString("username");
			if(username.equals(companyUsername)) {
				return true;
			}
			return false;
		} catch (JSONException e) {
			System.err.println("Error al parsear el objeto JSON: " + e.getMessage());
			return false;
		}
	}
	
	//LISTENERS
	
	/**
	 * Listener assigned to the edit button
	 *
	 * @param isEditing - true if is editing, false if not
	 */
	private void editButtonListener(boolean isEditing) {
		if(isEditing) {
			buttonsLayout.replace(editButton, saveButton);
			buttonsLayout.replace(locationButton, cancelButton);
		} else {
			buttonsLayout.replace(saveButton, editButton);
			buttonsLayout.replace(cancelButton, locationButton);
			refreshPageContent();
		}
		changeReadOnlyMode();
	}
	
	/**
	 * Listener assigned to the save changes button when user is editing
	 * 
	 */
	private void saveButtonListener() {
		if(salaryField.isEmpty() || vacanciesField.isInvalid() || vacanciesField.isEmpty() 
				|| addressField.getValue().isBlank() || startDate.isEmpty() || startDate.isInvalid() 
				|| endDate.isInvalid() || endDate.isEmpty() || descriptionField.getValue().isBlank()) {
			new CustomNotification(Constants.UPD_WRN, NotificationVariant.LUMO_WARNING);
			return;
		}
		if(!updateJobOfferBody()) {
			new CustomNotification(Constants.NOT_UPD_MSG, NotificationVariant.LUMO_PRIMARY);
			return;
		}
		var httpRequest = new HttpRequest(Constants.OFF_REQ);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		var isUpdated = httpRequest.executeHttpPut(authToken, jobOfferBody);
		changeReadOnlyMode();
		buttonsLayout.replace(saveButton, editButton);
		buttonsLayout.replace(cancelButton, locationButton);
		refreshPageContent();
		if(isUpdated) {
			new CustomNotification(Constants.UPD_MSG, NotificationVariant.LUMO_SUCCESS);
			return;
		}
		new CustomNotification(Constants.UPD_ERR, NotificationVariant.LUMO_ERROR);
	}
	
	/**
	 * Listener assigned to the register button, to apply to a job offer
	 *
	 */
	private void registerButtonListener() {
		if(jobOfferId != null) {
			this.getUI().ifPresent(ui -> ui.navigate(Constants.SEND_REQ_PATH + "/" + jobOfferId));
			return;
		}
		new CustomNotification(GENERAL_ERR,NotificationVariant.LUMO_ERROR);
	}
	
	private void addNewResourceButtonListener() {
		var dialog = new Dialog();
		dialog.getHeader().add(new H2("Añadir nuevo recurso"));
		dialog.setModal(true);
		dialog.setDraggable(true);
		var memoryBuffer = new MemoryBuffer();
		var resource = new Upload(memoryBuffer);
		resource.setDropLabel(new NativeLabel(Constants.DROP_TAG));
		resource.setAcceptedFileTypes(Constants.JPG, Constants.PNG, Constants.PDF, Constants.AVI, Constants.MP4);
		resource.addSucceededListener(eventUpload ->			//Asignamos listener de imagen aceptada
			new CustomNotification(Constants.UPLOAD_PIC_SUCC, NotificationVariant.LUMO_SUCCESS));
		resource.addFileRejectedListener(rejectEvent ->			//Asignamos listener de imagen rechazada
			new CustomNotification(Constants.PROF_FILE_ERROR, NotificationVariant.LUMO_ERROR));
		var saveButton = new Button("Añadir", event -> confirmAddResourceListener(dialog, memoryBuffer));
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		var cancelButton = new Button(Constants.CANCEL_TAG, event -> dialog.close());
		dialog.getFooter().add(saveButton, cancelButton);
		dialog.add(resource);
		dialog.open();
	}
	
	private void confirmAddResourceListener(Dialog dialog, MemoryBuffer memoryBuffer) {
		var fileName = memoryBuffer.getFileName();
		var fileData = memoryBuffer.getInputStream();
		if(fileData == null) {
			new CustomNotification("No ha seleccionado ningún recurso", NotificationVariant.LUMO_WARNING);
			return;
		}
		var resourceJSON = getJSONResourceObject(fileName, jobOfferBody);
		if(sendCreateResourceRequest(resourceJSON)) {
			setTabContent(jobOfferTabs.getSelectedTab());
			new CustomNotification("El recurso ha sido añadido correctamente", NotificationVariant.LUMO_SUCCESS);
			try {
				var file = new File(Constants.STORED_FILE_PATH + "\\" + fileName);
				Files.copy(fileData, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				new CustomNotification("Error al cargar el archivo", NotificationVariant.LUMO_ERROR);
			}
		} else {
			new CustomNotification("El recurso no ha podido ser añadido", NotificationVariant.LUMO_ERROR);
		}
		dialog.close();
	}
	
	/**
	 * Listener assigned to the display company option
	 * 
	 */
	private void showCompanyInfoListener() {
		var companyId = new String();
		try {
			companyId = new JSONObject(jobOfferBody).getJSONObject("company").getJSONObject("user").getString("username");
		} catch (JSONException e) {
			new CustomNotification(SHW_CMP_ERR, NotificationVariant.LUMO_ERROR);
			return;
		}
		VaadinSession.getCurrent().setAttribute("userId", companyId);
		var url = (userRole.equals(Constants.ADM_ROLE)) ? Constants.CMP_DET_PATH + "/" + Constants.CMP_ROLE : Constants.CMP_DET_PATH;
		this.getUI().ifPresent(ui -> ui.navigate(url));
		return;
	}
	
	/**
	 * Listener assigned to the next page option button
	 * 
	 */
	private void nextPageListener() {
		numPage++;
		setTabContent(jobOfferTabs.getSelectedTab());
	}
	
	/**
	 * Listener assigned to the previous page option button
	 * 
	 */
	private void prevPageListener() {
		numPage--;
		setTabContent(jobOfferTabs.getSelectedTab());
	}
	
	/**
	 * Listener assigned to the Select component which displays the number of elements
	 * 
	 * @param value - select's value
	 */
	private void customSelectListener(Integer value) {
		numElements = value;
		numPage = 0;
		setTabContent(jobOfferTabs.getSelectedTab());
	}
	
	//PARSEOS JSON
	
	/**
	 * Gets all job offer parameters from the JSON object
	 *
	 */
	private void parseJobOfferJSON() {
		try {
			var jobOfferJSON = new JSONObject(jobOfferBody);
			titleValue = jobOfferJSON.getString("title");
			descValue = jobOfferJSON.getString("description");
			modalityValue = jobOfferJSON.getString("modality");
			areaValue = jobOfferJSON.getJSONObject("area").getString("name");
			addressValue = jobOfferJSON.getString("address");
			vacanciesValue = jobOfferJSON.getInt("vacancies");
			salaryValue = jobOfferJSON.getInt("salary");
			startDateValue = jobOfferJSON.getString("startDate");
			endDateValue = jobOfferJSON.getString("endDate");
			cityValue = jobOfferJSON.getString("city");
			companyValue = jobOfferJSON.getJSONObject("company").getString("name");
			logoValue = jobOfferJSON.getJSONObject("company").getString("profilePicture");
		} catch (JSONException e) {
			System.err.println(Constants.JSON_ERR + e.getMessage());
			new CustomNotification(Constants.ERR_MSG, NotificationVariant.LUMO_ERROR);
			getUI().ifPresent(ui -> ui.navigate(Constants.OFFERS_PATH));
		}
	}
	
	/**
	 * Parses a list of job offers from a JSON object
	 * 
	 * @param resourcesBody - json object to parse
	 */
	private void parseJSONResourcesList(String resourcesBody){
		try {
			var jsonObject = new JSONObject(resourcesBody);
			var contentArray = jsonObject.getJSONArray("content");
			var isShowingFirst = jsonObject.getBoolean("first");
			var isShowingLast = jsonObject.getBoolean("last");
			var resourcesGrid = new CustomFilesGrid(contentArray, userRole);
			var resourcesLayout = new VerticalLayout();
			resourcesLayout.setMaxWidth("1000px");
			resourcesLayout.setWidthFull();
			resourcesLayout.add(resourcesGrid);
			setNavigationOptionsPageLayout(isShowingFirst, isShowingLast);
			if(userRole.equals(Constants.ADM_ROLE) || userRole.equals(Constants.CMP_ROLE)) {
				tabContentLayout.add(newResourceButton);
			}
			tabContentLayout.add(resourcesLayout, navigationOptionsPageLayout, customSelect);
		} catch (JSONException e) {
			System.err.println(Constants.JSON_ERR + e.getMessage());
			return;
		}
	}
	
	/**
	 * Builds a resource json object
	 * 
	 * @param name - resource's name
	 * @param jobOffer - offer to which the resource belongs
	 * @return String - json object
	 */
	private String getJSONResourceObject(String name, String jobOffer) {
		try {
			var resourceJSON = new JSONObject();
			resourceJSON.put("name", name);
			resourceJSON.put("jobOffer", new JSONObject(jobOffer));
			return resourceJSON.toString();
		} catch (JSONException e) {
			new CustomNotification(Constants.ERR_MSG, NotificationVariant.LUMO_ERROR);
			return null;
		}
	}
	
	//HTTP REQUESTS
	
	/**
	 * Sends the http request to obtain the job offer details
	 *
	 * @param id - job offer id
	 * @return String - response body
	 */
	private String sendJobOfferDetailsRequest(Long id) {
		var getUrl = Constants.OFF_REQ + "/jobOffer?offerCode=" + id;
		var httpRequest = new HttpRequest(getUrl);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpGet(authToken);
	}
	
	/**
	 * Sends the request to verify if a student has applied to an offer or not
	 *
	 * @return boolean - true if has applied, false if not
	 */
	private boolean sendCheckIfStudentAppliedToOffer() {
		var username = VaadinSession.getCurrent().getAttribute("username");
		var getUrl = Constants.REQ_REQ + "?userId=" + username + "&offerCode=" + jobOfferId;
		var httpRequest = new HttpRequest(getUrl);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		var httpResponse = httpRequest.executeHttpGet(authToken);
		if(httpResponse != null) {
			return checkIfStudentAppliedToOffer(httpResponse);
		}
		return false;
	}
	
	/**
	 * Sends an http request to
	 * 
	 * @param getUrl - url endpoint
	 * @param numPage - page number to request
	 * @param numElements - number of elements to request
	 * @return String response body
	 */
	private String sendGetResourcesRequest(String getUrl, Integer numPage, Integer numElements) {
		var httpRequest = new HttpRequest(getUrl);
		getUrl = (numPage == null) ? getUrl : getUrl + "&page=" + numPage;
		getUrl = (numElements == null) ? getUrl : getUrl + "&size=" + numElements;
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpGet(authToken);
	}
	
	/**
	 * Sends an http request to create a resource
	 * 
	 * @param requestBody - json body to send into the request
	 * @return boolean - true if it has been created, false if not
	 */
	private boolean sendCreateResourceRequest(String requestBody) {
		var httpRequest = new HttpRequest(Constants.RES_REQ);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpPost(authToken, requestBody);
	}
}
