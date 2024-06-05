package com.abb.pfg.views;

import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONObject;
import org.vaadin.addons.maplibre.MapLibre;

import com.abb.pfg.custom.CustomAppLayout;
import com.abb.pfg.custom.CustomNotification;
import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.HttpRequest;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

/**
 * Shows a map to locate where offers the company the job offer
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route(Constants.LOC_PATH)
@PageTitle("J4S - Localización")
public class LocationView extends CustomAppLayout implements BeforeEnterObserver, HasUrlParameter<Long>{

	private static final long serialVersionUID = -6501593227939399474L;
	private static final String HEADER_TAG = "Localización";
	private VerticalLayout mainLayout;
	private String userRole, title, address, name, owner;
	private Long jobOfferId;
	private double lat, lng;
	
	@Override
	public void setParameter(BeforeEvent event, Long parameter) {
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		if (authToken == null) {
            event.forwardTo(LoginView.class);
            return;
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
		var username = (String) VaadinSession.getCurrent().getAttribute("username");
		var jobOfferBody = sendGetJobOfferRequest(jobOfferId);
		if(jobOfferBody == null) {
			event.forwardTo(AvailableOffersView.class);
            return;
		}
		setJobOffersFields(jobOfferBody);
		if(userRole.equals(Constants.CMP_ROLE) && !verifyAccessPermission(owner, username)) {
			event.forwardTo(AvailableOffersView.class);
            return;
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
		mainLayout.setSizeFull();
		setContentLayout();
		var baseVerticalLayout = new VerticalLayout();
		baseVerticalLayout.add(new H1(HEADER_TAG), mainLayout);
		baseVerticalLayout.setAlignItems(Alignment.CENTER);
		this.setContent(baseVerticalLayout);
	}
	
	/**
	 * Sets up the content of the view's main layout
	 * 
	 */
	private void setContentLayout() {
		sendGetCoordinatesRequest(address);
		setMapOnLayout();
		mainLayout.add(getLayoutComponents());
	}
	
	/**
	 * Sets up the map object and adds it to the main layout
	 * 
	 */
	private void setMapOnLayout() {
		try {
            MapLibre map = new MapLibre(new URI("https://demotiles.maplibre.org/style.json"));
            map.setStyle("https://api.maptiler.com/maps/streets/style.json?key=vYXVgPlhtQsWkGgfNsmu");
            map.setHeight("500px");
            map.setMaxWidth("1500px");
            map.setCenter(lng, lat);
            map.setZoomLevel(15);
            map.addMarker(lng, lat);
            mainLayout.add(map);
        } catch (URISyntaxException e) {
        	new CustomNotification(Constants.ERR_MSG, NotificationVariant.LUMO_ERROR);
        	throw new RuntimeException(e);
        }
	}
	
	/**
	 * Gets the verticalLayout that displays the job offer info on components
	 * 
	 * @return VerticalLaoyut - layout that displays the job offer info
	 */
	private VerticalLayout getLayoutComponents() {
		var verticalLayout = new VerticalLayout();
		verticalLayout.setAlignItems(Alignment.CENTER);
		var titleField = new TextField(Constants.OFFER_TAG);
		titleField.setReadOnly(!titleField.isReadOnly());
		titleField.setMaxWidth("500px");
		titleField.setWidthFull();
		titleField.setValue(title);
		var nameField = new TextField(Constants.COMPANY_TAG);
		nameField.setReadOnly(!nameField.isReadOnly());
		nameField.setMaxWidth("500px");
		nameField.setWidthFull();
		nameField.setValue(name);
		var addressField = new TextField(Constants.ADDRESS_TAG);
		addressField.setReadOnly(!addressField.isReadOnly());
		addressField.setValue(address);
		addressField.setMaxWidth("500px");
		addressField.setWidthFull();
		verticalLayout.add(titleField, nameField, addressField);
		return verticalLayout;
	}
	
	/**
	 * Formats an address so that it can be added to the http request
	 * 
	 * @param address - address to be formated
	 * @return String - formated address
	 */
	private String formatAddress(String address) {
		var finalAddress = address.replaceAll(", ", "+");
		finalAddress = address.replaceAll(" ", "+");
		return finalAddress;
	}
	
	/**
	 * Verifies if the user has enough permissions to access to this data
	 * 
	 * @param userOwner - user's job offer owner
	 * @param username - user that is trying to access to the data
	 * @return boolean - true if the user has permission, false if not
	 */
	private boolean verifyAccessPermission(String userOwner, String username) {
		var accessOK = (username.equals(userOwner)) ? true : false;
		return accessOK;
	}
	
	//PARSEOS JSON
	
	/**
	 * Sets up the job offer fields needed
	 *  
	 * @param jobOfferBody - JSON job offer body needed to extract the job offer info
	 */
	private void setJobOffersFields(String jobOfferBody) {
		try {
			var jsonObject = new JSONObject(jobOfferBody);
			title = jsonObject.getString("title");
			name = jsonObject.getJSONObject("company").getString("name");
			address = jsonObject.getString("address") + ", " +jsonObject.getString("city");
			owner = jsonObject.getJSONObject("company").getJSONObject("user").getString("username");
		} catch (JSONException e) {
			new CustomNotification("Se ha producido un error", NotificationVariant.LUMO_ERROR);
		}
	}
	
	/**
	 * Sets up the location fields
	 * 
	 * @param jsonPositiningItem - JSON object needed to extract the location info
	 */
	private void setCoordinatesFromJSON(String jsonPositiningItem) {
		try {
			var jsonObject = new JSONObject(jsonPositiningItem);
			lat = jsonObject.getJSONArray("items").getJSONObject(0).getJSONObject("position").getDouble("lat");
			lng = jsonObject.getJSONArray("items").getJSONObject(0).getJSONObject("position").getDouble("lng");
		} catch(JSONException e) {
			new CustomNotification("Se ha producido un error", NotificationVariant.LUMO_ERROR);
		}
	}
	
	//HTTP REQUESTS
	
	/**
	 * Sends an http request to get a job offer
	 * 
	 * @param id - job offer's id
	 * @return String - JSON job offer object
	 */
	private String sendGetJobOfferRequest(Long id) {
		var httpRequest = new HttpRequest(Constants.OFF_REQ + "/jobOffer?offerCode=" + id);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpGet(authToken);
 	}
	
	/**
	 * Sends an http request to get the location info
	 * 
	 * @param address - address to look up
	 */
	private void sendGetCoordinatesRequest(String address) {
		var addressFormatted = formatAddress(address);
		var apiKey = "j5g40NfAQN_FiZ430y4z0ZJY1h_nExaCKsgHsezkkN0";
		var getUrl = "https://geocode.search.hereapi.com/v1/geocode?q=" + addressFormatted + "&limit=4&apiKey=" + apiKey;
		var responseBody = new HttpRequest(getUrl).executeHttpGet(null);
		if(responseBody != null) {
			setCoordinatesFromJSON(responseBody);
			return;
		}
		new CustomNotification("Se ha producido un error", NotificationVariant.LUMO_ERROR);
	}
}
