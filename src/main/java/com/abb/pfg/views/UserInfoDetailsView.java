package com.abb.pfg.views;

import org.json.JSONException;
import org.json.JSONObject;

import com.abb.pfg.custom.CustomAppLayout;
import com.abb.pfg.custom.CustomAvatar;
import com.abb.pfg.custom.CustomFilesGrid;
import com.abb.pfg.custom.CustomJobOffersGrid;
import com.abb.pfg.custom.CustomNavigationOptionsPageLayout;
import com.abb.pfg.custom.CustomNotification;
import com.abb.pfg.custom.CustomNumElementsSelect;
import com.abb.pfg.custom.CustomTextArea;
import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.HttpRequest;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinSession;

import lombok.NoArgsConstructor;

/**
 * Shows the user's info to other users, depending on the role they have
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route(Constants.STD_DET_PATH)
@RouteAlias(Constants.CMP_DET_PATH)
@PageTitle("J4S - Ver detalles")
@NoArgsConstructor
public class UserInfoDetailsView extends CustomAppLayout implements HasUrlParameter<String>, BeforeEnterObserver{
	 
	private static final long serialVersionUID = 5874622058917424448L;
	private static final String CMP_INFO = "Información de la empresa";
	private static final String STD_INFO = "Información del estudiante";
	private static final String FILE_INFO = "Archivos";
	private static final String OFF_INFO = "Ofertas";
	
	private VerticalLayout mainLayout, tabContentLayout;
	private CustomNavigationOptionsPageLayout navigationOptionsPageLayout;
	private Avatar customAvatar;
	private Tabs userTabs;
	private CustomNumElementsSelect customSelect;
	private Tab studentInfo, companyInfo, filesInfo, offersInfo;
	private String userId, headerTag, userBody, userCategory, userRole;
	private Integer numPage = 0;
	
	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		userRole = (String) VaadinSession.getCurrent().getAttribute("role");
		if(userRole != null) {
			if(parameter != null && userRole.equals(Constants.ADM_ROLE)) {
				userCategory = parameter;
				return;
			} else if (parameter == null && !userRole.equals(Constants.ADM_ROLE)){
				userCategory = (userRole.equals(Constants.STD_ROLE)) ? Constants.CMP_ROLE : Constants.STD_ROLE;
				return;
			} else {
				new CustomNotification("No tiene acceso al recurso solicitado", NotificationVariant.LUMO_ERROR);
				event.forwardTo(LoginView.class);
				return;
			}
		}
		new CustomNotification("No tiene acceso al recurso solicitado", NotificationVariant.LUMO_ERROR);
		event.forwardTo(LoginView.class);
		return;
	}
	
	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		if(authToken == null) {
			event.forwardTo(LoginView.class);
			return;
		}
		userId = (String) VaadinSession.getCurrent().getAttribute("userId");
		userBody = sendGetUserInfo(userId, userCategory, authToken);
		if (userBody == null) {
			event.forwardTo(LoginView.class);
			return;
		}
		headerTag = (userCategory.equals(Constants.CMP_ROLE)) ? Constants.COMPANY_TAG : Constants.STUDENT_TAG;
		init();
	}
	
	/**
	 * Initializes all view components
	 * 
	 */
	private void init() {
		mainLayout = new VerticalLayout();		//Inicializamos el layout principal
		mainLayout.setAlignItems(Alignment.CENTER);
		mainLayout.setWidthFull();
		getContentLayout();
		var baseVerticalLayout = new VerticalLayout();
		baseVerticalLayout.add(new H1(headerTag), mainLayout);
		baseVerticalLayout.setAlignItems(Alignment.CENTER);
		this.setContent(baseVerticalLayout);
	}
	
	/**
	 * Gets the view components to show to the user
	 * 
	 */
	private void getContentLayout() {
		customSelect = new CustomNumElementsSelect();
		customSelect.addValueChangeListener(event -> customSelectListener());
		userTabs = new Tabs();
		userTabs.setAutoselect(true);
		userTabs.addSelectedChangeListener(event -> setTabContent(event.getSelectedTab()));
		tabContentLayout = new VerticalLayout();
		tabContentLayout.setAlignItems(Alignment.CENTER);
		if(userCategory.equals(Constants.CMP_ROLE)) {
			companyInfo = new Tab(CMP_INFO);
			filesInfo = new Tab(FILE_INFO);
			offersInfo = new Tab(OFF_INFO);
			userTabs.add(companyInfo, filesInfo, offersInfo);
		} else if(userCategory.equals(Constants.STD_ROLE)) {
			studentInfo = new Tab(STD_INFO);
			filesInfo = new Tab(FILE_INFO);
			userTabs.add(studentInfo, filesInfo);
		}
		mainLayout.add(userTabs, tabContentLayout);
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
		if(tab.equals(studentInfo)) {
			getStudentInfoLayout(userBody);
		} else if(tab.equals(filesInfo)) {
			setFilesInfoLayout();
		} else if(tab.equals(companyInfo)) {
			setCompanyInfoLayout(userBody);
		} else if(tab.equals(offersInfo)) {
			setOffersInfoLayout(userId);
		}
	}
	
	/**
	 * Gets the student info layout with all the components needed to show the student info
	 * 
	 * @param student - student JSON object
	 */
	private void getStudentInfoLayout(String student) {
		var jsonObject = new JSONObject(student);
		var nameValue = jsonObject.getString("name");
		var picValue = jsonObject.getString("profilePicture");
		var dniValue = jsonObject.getString("dni");
		var descValue = jsonObject.getString("description");
		var studiesValue = jsonObject.getString("studies");
		var phoneValue = jsonObject.getString("phoneNumber");
		getCustomAvatar(nameValue, picValue);
		setStudentInfoComponents(nameValue, dniValue, phoneValue, studiesValue, descValue);
	}
	
	/**
	 * Sets the value on the student info components
	 * 
	 * @param nameValue - student name value
	 * @param dniValue - student dni value
	 * @param phoneValue - student phone value
	 * @param studiesValue - student studies value
	 * @param descValue - student description value
	 */
	private void setStudentInfoComponents(String nameValue, String dniValue, String phoneValue, String studiesValue, String descValue) {
		var fieldsLayout = new HorizontalLayout();
		var dniField = new TextField(Constants.DNI_TAG);
		dniField.setValue(dniValue);
		dniField.setWidth("50%");
		dniField.setReadOnly(true);
		var phoneField = new TextField(Constants.PHONE_TAG);
		phoneField.setValue(phoneValue);
		phoneField.setWidth("50%");
		phoneField.setReadOnly(true);
		fieldsLayout.setWidth("50%");
		fieldsLayout.add(dniField, phoneField);
		var studiesField = new TextField(Constants.STUDIES_TAG);
		studiesField.setValue(studiesValue);
		studiesField.setReadOnly(true);
		studiesField.setWidth("50%");
		var descField = new CustomTextArea(Constants.DESC_TAG, descValue);
		descField.setReadOnly(true);
		descField.setWidth("70%");
		tabContentLayout.add(customAvatar, new H4(nameValue), fieldsLayout, studiesField, descField);
	}
	
	/**
	 * Gets the company info layout from the company response body
	 * 
	 * @param company - company response body
	 */
	private void setCompanyInfoLayout(String company) {
		var jsonObject = new JSONObject(company);
		var nameValue = jsonObject.getString("name");
		var logoValue = jsonObject.getString("profilePicture");
		var cifValue = jsonObject.getString("cif");
		var descValue = jsonObject.getString("description");
		var countryValue = jsonObject.getString("country");
		getCustomAvatar(nameValue, logoValue);
		setCompanyInfoComponents(nameValue, cifValue, countryValue, descValue);
	}
	
	/**
	 * Sets the value on the company info components
	 * 
	 * @param nameValue - company name value
	 * @param cifValue - company cif value
	 * @param countryValue - company country value
	 * @param descValue - company description value
	 */
	private void setCompanyInfoComponents(String nameValue, String cifValue, String countryValue, String descValue) {
		var fieldsLayout = new HorizontalLayout();
		var cifField = new TextField(Constants.CIF_TAG);
		cifField.setValue(cifValue);
		cifField.setWidth("50%");
		cifField.setReadOnly(true);
		var countryField = new TextField(Constants.COUNTRY_TAG);
		countryField.setValue(countryValue);
		countryField.setWidth("50%");
		countryField.setReadOnly(true);
		fieldsLayout.setWidth("50%");
		fieldsLayout.add(cifField, countryField);
		var descField = new CustomTextArea(Constants.DESC_TAG, descValue);
		descField.setReadOnly(true);
		descField.setWidth("70%");
		tabContentLayout.add(customAvatar, new H4(nameValue), fieldsLayout, descField);
	}
	
	/**
	 * Gets the company offers layout from the company response body
	 * 
	 * @param userId - company user id
	 * @param numPage - page number to show to the user
	 * @param numElements - number of elements to show to the user
	 */
	private void setOffersInfoLayout(String userId) {
		var offersBody = sendGetListRequest(Constants.OFF_REQ + "?companyId=" + userId, numPage, customSelect.getValue());
		setGridLayout(offersBody, Constants.OFFER_TAG);
	}
	
	private void setFilesInfoLayout() {
		var mediaBody = sendGetListRequest(Constants.MEDIA_REQ + "?username=" + userId, numPage, customSelect.getValue());
		setGridLayout(mediaBody, Constants.MEDIA_TAG);
	}
	
	private void setGridLayout(String responseBody, String resourceCategory) {
		if(responseBody == null) {
			new CustomNotification("No se han podido obtener los recursos solicitados", NotificationVariant.LUMO_ERROR);
			return;
		}
		try {
			var jsonObject = new JSONObject(responseBody);
			var contentArray = jsonObject.getJSONArray("content");
			var isShowingFirst = jsonObject.getBoolean("first");
			var isShowingLast = jsonObject.getBoolean("last");
			var tempGrid = (resourceCategory.equals(Constants.MEDIA_TAG)) ? new CustomFilesGrid(contentArray, userRole, userId) : 
				new CustomJobOffersGrid(contentArray, userRole, false);
			var tempLayout = new VerticalLayout();
			tempLayout.setWidth("50%");
			tempLayout.add(tempGrid);
			setNavigationOptionsPageLayout(isShowingFirst, isShowingLast);
			tabContentLayout.add(tempLayout, navigationOptionsPageLayout, customSelect);
		} catch (JSONException e) {
			new CustomNotification("No se han podido obtener los recursos solicitados", NotificationVariant.LUMO_ERROR);
			return;
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
	 * Gets the user's avatar from the picture name and path
	 * 
	 * @param name - user's name
	 * @param logo - picture name
	 */
	private void getCustomAvatar(String name, String logo) {
		if(logo == null) {
			customAvatar = new Avatar(name);
		} else {
			customAvatar = new CustomAvatar(logo);
		}
		customAvatar.setHeight("15%");
		customAvatar.setWidth("15%");
	}
	
	//LISTENERS
	
	private void nextPageListener() {
		numPage++;
		tabContentLayout.removeAll();
		setTabContent(userTabs.getSelectedTab());
	}
	
	private void prevPageListener() {
		numPage--;
		tabContentLayout.removeAll();
		setTabContent(userTabs.getSelectedTab());
	}
	
	private void customSelectListener() {
		numPage=0;
		tabContentLayout.removeAll();
		setTabContent(userTabs.getSelectedTab());
	}
	
	//HTTP REQUESTS
	
	/**
	 * Sends the http request to get the user info details
	 * 
	 * @param username - username to search
	 * @param userType - user's role
	 * @param authToken - authentication token to allow the request
	 * @return String - response body
	 */
	private String sendGetUserInfo(String username, String userCategory, String authToken) {
		if(username == null) {
			return null;
		}
		var getUrl = (userCategory.equals(Constants.CMP_ROLE)) 
				? Constants.CMP_REQ + "/company?username=" + username 
				: Constants.STD_REQ + "/student?username=" + username;
		var httpRequest = new HttpRequest(getUrl);
		return httpRequest.executeHttpGet(authToken);
	}
	
	private String sendGetListRequest(String getUrl, Integer numPage, Integer numElements) {
		var httpRequest = new HttpRequest(getUrl + "&page=" + numPage);
		getUrl = (numElements == null) ? getUrl : getUrl + "&size=" + numElements;
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpGet(authToken);
	}
}
