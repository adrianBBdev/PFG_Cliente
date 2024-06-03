package com.abb.pfg.custom;

import org.json.JSONException;
import org.json.JSONObject;

import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.HttpRequest;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.VaadinSession;
/**
 * Custom item in the job offfers list
 *
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Uses(Icon.class)
public class CustomJobOfferListItem extends Composite<VerticalLayout>{

	private static final long serialVersionUID = -3189599315827003893L;
	//Tags
	private static final String GET_OFF_ERR = "Se ha producido un error. "
			+ "No se pueden mostrar los detalles de esta oferta.";
	private static final String SHW_DET_TAG = "Ver detalles";
	//Elementos
	private HorizontalLayout itemLayout, buttonsLayout;
	private VerticalLayout jobOfferDetailsLayout;
	private CustomAvatar companyAvatar;
	private H3 jobOfferTitle;
	private H5 companyName;
	private TextField modalityField, cityField, areaField;
	private Button detailsButton, likeButton;
	private MenuBar menuBar;
	private Long jobOfferId;
	private String userRole, offerBody, offerTitle, companyNameValue, modalityValue, areaValue, cityValue, logoValue;
	private boolean isSaved;

	/**
	 * Default class constructor
	 * 
	 * @param offerBody - job offer info
	 * @param isSaved - true if it has been saved by the user, false if not
	 */
	public CustomJobOfferListItem(String offerBody, boolean isSaved) {
		this.offerBody = offerBody;
		setJobOfferValues(offerBody);
		this.isSaved = isSaved;
		userRole = (String) VaadinSession.getCurrent().getAttribute("role");
		getContent().setAlignItems(Alignment.CENTER);
		itemLayout = new HorizontalLayout();
		jobOfferDetailsLayout = new VerticalLayout();
		jobOfferDetailsLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		jobOfferTitle = new H3(offerTitle);
		jobOfferTitle.setWidthFull();
		companyName = new H5(companyNameValue);
		companyName.setWidthFull();
		getCustomTextFields(modalityValue, areaValue, cityValue);
		getButtonsLayout(userRole, isSaved);
		jobOfferDetailsLayout.add(jobOfferTitle, companyName, cityField,
				areaField, modalityField);
		getCutsomAvatar(logoValue, userRole);
		if(userRole.equals(Constants.GST_ROLE)) {
			itemLayout.add(companyAvatar, jobOfferDetailsLayout);
		} else {
			itemLayout.add(menuBar, jobOfferDetailsLayout);
		}
		itemLayout.setWidthFull();
		itemLayout.setAlignItems(Alignment.START);
		getContent().add(itemLayout, buttonsLayout);
	}
	
	/**
	 * Extracts and sets up the job offer info values 
	 * 
	 * @param offerBody - all job offer's info
	 */
	private void setJobOfferValues(String offerBody) {
		try {
			var jsonObject = new JSONObject(offerBody);
			jobOfferId = jsonObject.getLong("id");
			offerTitle = jsonObject.getString("title");
			companyNameValue = jsonObject.getJSONObject("company").getString("name");
			modalityValue = jsonObject.getString("modality");
			areaValue = jsonObject.getJSONObject("area").getString("name");
			cityValue = jsonObject.getString("city");
			logoValue = jsonObject.getJSONObject("company").getString("profilePicture");
		} catch(JSONException e) {
			new CustomNotification("Se ha producido un error", NotificationVariant.LUMO_ERROR);
			this.getUI().ifPresent(ui -> ui.navigate(Constants.LOGIN_PATH));
		}
	}

	/**
	 * Sets up the custom text fields to display some job offer's info
	 * 
	 * @param modalityValue - modality's value 
	 * @param areaValue - area's value
	 * @param cityValue - city's value
	 */
	private void getCustomTextFields(String modalityValue, String areaValue, String cityValue) {
		modalityField = new TextField(Constants.MODALITY_TAG);
		modalityField.setValue(modalityValue);
		modalityField.setWidthFull();
		modalityField.setReadOnly(true);
		areaField = new TextField(Constants.AREA_TAG);
		areaField.setValue(areaValue);
		areaField.setWidthFull();
		areaField.setReadOnly(true);
		cityField = new TextField(Constants.CITY_TAG);
		cityField.setValue(cityValue);
		cityField.setWidthFull();
		cityField.setReadOnly(true);
	}
	
	/**
	 * Gets the user avatar from the profile picture name
	 * 
	 * @param logo - profile picture name
	 * @param userRole - user's role
	 */
	private void getCutsomAvatar(String logo, String userRole) {
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
	 * Builds buttonsLayout, including the like button to allow the user to save or unsave the offer
	 * 
	 * @param userType - user's role
	 * @param isSaved - true if it has already been saved, false if not
	 */
	private void getButtonsLayout(String userRole, boolean isSaved) {
		buttonsLayout = new HorizontalLayout();
		detailsButton = new Button(SHW_DET_TAG);
		detailsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		detailsButton.addClickListener(event -> jobOfferDetailsListener());
		if(userRole.equals(Constants.GST_ROLE)) {
			buttonsLayout.add(detailsButton);
			return;
		}
		if(isSaved) {
			likeButton = new Button(new Icon(VaadinIcon.BOOKMARK));
		} else {
			likeButton = new Button(new Icon(VaadinIcon.BOOKMARK_O));
		}
		likeButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		likeButton.addClickListener(event -> jobOfferLikeListener());
		buttonsLayout.add(detailsButton, likeButton);
	}
	
	//LISTENERS
	
	/**
	 * Listener assigned to the display job offer's details option
	 * 
	 */
	private void jobOfferDetailsListener() {
		if(jobOfferId != null) {
			this.getUI().ifPresent(ui -> ui.navigate(Constants.OFFER_PATH + "/" + jobOfferId));
			return;
		}
		var notification = new Notification(GET_OFF_ERR);
		notification.setPosition(Position.TOP_CENTER);
		notification.setDuration(Constants.NOTIF_DURATION);
		notification.addThemeVariants(NotificationVariant.LUMO_PRIMARY);
		notification.open();
	}
	
	/**
	 * Listener assigned to the like button, to save or not save the offers
	 * 
	 */
	private void jobOfferLikeListener() {
		var username = (String) VaadinSession.getCurrent().getAttribute("username");
		if(isSaved) {
			if(sendjobOfferFavoriteRemove(username, jobOfferId)) {
				isSaved = !isSaved;
				likeButton.setIcon(new Icon(VaadinIcon.BOOKMARK_O));
			}
			return;
		}
		var httpStudentRequest = new HttpRequest(Constants.STD_REQ + "/student?username=" + username);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		var studentBody = httpStudentRequest.executeHttpGet(authToken);
		var httpOfferRequest = new HttpRequest(Constants.OFF_REQ + "/jobOffer?offerCode=" + jobOfferId);
		var offerBody = httpOfferRequest.executeHttpGet(authToken);
		if(offerBody == null || studentBody == null) {
			return;
		}
		var jsonObject = new JSONObject();
		jsonObject.put("student", new JSONObject(studentBody));
		jsonObject.put("jobOffer", new JSONObject(offerBody));
		var httpPostRequest = new HttpRequest(Constants.FAV_REQ);
		if(httpPostRequest.executeHttpPost(authToken, jsonObject.toString())) {
			isSaved = !isSaved;
			likeButton.setIcon(new Icon(VaadinIcon.BOOKMARK));
			new CustomNotification("Oferta guardada correctamente", NotificationVariant.LUMO_PRIMARY);
		}
	}
	
	/**
	 * Listener assigned to the display company's details option
	 * 
	 */
	private void showCompanyInfoListener() {
		var companyId = new String();
		try {
			companyId = new JSONObject(offerBody).getJSONObject("company").getJSONObject("user").getString("username");
		} catch (JSONException e) {
			new CustomNotification("No se ha podido obtener la empresa seleccionada", NotificationVariant.LUMO_ERROR);
			return;
		}
		VaadinSession.getCurrent().setAttribute("userId", companyId);
		this.getUI().ifPresent(ui -> ui.navigate(Constants.CMP_DET_PATH));
		return;
	}
	
	//HTTP REQUESTS
	
	/**
	 * Listener assigned to the remove favorite job offer option on the grid
	 * 
	 * @param username - user's username
	 * @param offerCode - job offer's code
	 * @return true if it was deleted, false if not
	 */
	private boolean sendjobOfferFavoriteRemove(String username, Long offerCode) {
		var httpGetRequest = new HttpRequest(Constants.FAV_REQ + "/favorite?username=" + username + "&offerCode=" + offerCode);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		var favoriteBody = httpGetRequest.executeHttpGet(authToken);
		if(favoriteBody.isEmpty()) {
			return false;
		}
		var jsonObject = new JSONObject(favoriteBody);
		var favoriteCode = jsonObject.getLong("id");
		var httpRequest = new HttpRequest(Constants.FAV_REQ + "?favoriteCode=" + favoriteCode);
		if(httpRequest.executeHttpDelete(authToken)) {
			new CustomNotification(Constants.DEL_MSG, NotificationVariant.LUMO_PRIMARY);
			return true;
		}
		new CustomNotification(Constants.DEL_ERR, NotificationVariant.LUMO_PRIMARY);
		return false;
	}
}
