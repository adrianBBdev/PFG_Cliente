package com.abb.pfg.views;

import org.json.JSONException;
import org.json.JSONObject;

import com.abb.pfg.custom.CustomAppLayout;
import com.abb.pfg.custom.CustomNotification;
import com.abb.pfg.custom.CustomTextArea;
import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.HttpRequest;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import lombok.NoArgsConstructor;

/**
 * View that shows the option of sending a request about a job offer
 *
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route(Constants.SEND_REQ_PATH)
@PageTitle("J4S - Enviar solicitud")
@NoArgsConstructor
public class SendRequestView extends CustomAppLayout implements HasUrlParameter<Long>{

	private static final long serialVersionUID = -2334599638209630511L;
	//Etiquetas
	private static final String HEADER_TAG = "Enviar solicitud";
	private static final String INFO_TAG = "Escribe una breve presentación";
	private static final String OFF_TAG = "Oferta";
	private static final String SD_REQ_MSG = "Tienes que rellenar la solicitud para poder enviarla";
	private static final String SD_REQ_ERR = "No se ha podido enviar la solicitud";
	private static final String SD_REQ_SUCC_MSG = "Has sido inscrito a la oferta correctamente";
	private static final String SD_REQ_CNL_MSG = "Se ha cancelado el envío de la solicitud";
	private static final String ACC_REJ_ERR = "No tiene permitido el acceso a esta información";
	//Componentes
	private TextField titleField, nameField;
	private CustomTextArea requestContentField;
	private HorizontalLayout buttonsLayout;
	private Button sendRequestButton, cancelButton;
	private String username;
	private Long jobOfferId;

	@Override
	public void setParameter(BeforeEvent event, Long parameter) {
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		if(authToken == null){
			event.forwardTo(LoginView.class);
			new CustomNotification(ACC_REJ_ERR, NotificationVariant.LUMO_ERROR);
		}
		var userRole = (String) VaadinSession.getCurrent().getAttribute("role");
		if(userRole.equals(Constants.GST_ROLE) || userRole.equals(Constants.CMP_ROLE)) {
			event.forwardTo(AvailableOffersView.class);
			new CustomNotification(ACC_REJ_ERR, NotificationVariant.LUMO_ERROR);
		}
		jobOfferId = parameter;
		username = (String) VaadinSession.getCurrent().getAttribute("username");
		init();
	}

	/**
	 * Initializes the view components
	 *
	 */
	private void init() {
		titleField = new TextField(OFF_TAG);
		titleField.setReadOnly(true);
		titleField.setWidth("40%");
		nameField = new TextField(Constants.COMPANY_TAG);
		nameField.setReadOnly(true);
		nameField.setWidth("40%");
		var responseBody = sendGetJobOfferRequest(jobOfferId);
		parseJSONOffers(responseBody);
		requestContentField = new CustomTextArea(INFO_TAG, "");
		requestContentField.setWidth("60%");
		getViewButtons();
		var baseVerticalLayout = new VerticalLayout();
		baseVerticalLayout.add(new H1(HEADER_TAG), titleField,
				nameField, requestContentField, buttonsLayout);
		baseVerticalLayout.setAlignItems(Alignment.CENTER);
		this.setContent(baseVerticalLayout);
	}
	
	//LISTENERS

	/**
	 * Listener assigned to the send request button
	 *
	 */
	private void sendRequestButtonListener() {
		if(requestContentField.getValue().isBlank()) {
			new CustomNotification(SD_REQ_MSG,
					NotificationVariant.LUMO_PRIMARY);
			return;
		}
		if(!sendRequest()) {
			new CustomNotification(SD_REQ_ERR,
					NotificationVariant.LUMO_PRIMARY);
			return;
		}
		getUI().ifPresent(ui -> ui.navigate(Constants.OFFER_PATH + "/" + jobOfferId));
		new CustomNotification(SD_REQ_SUCC_MSG,
				NotificationVariant.LUMO_SUCCESS);
	}
	
	/**
	 * Listener assigned to cancel button to coming back to the previous page
	 * 
	 */
	private void cancelButtonListener() {
		getUI().ifPresent(ui -> ui.navigate(Constants.OFFER_PATH + "/" + jobOfferId));
		new CustomNotification(SD_REQ_CNL_MSG,
				NotificationVariant.LUMO_PRIMARY);
	}
	
	//PARSEOS JSON
	
	/**
	 * Gets the request body needed to send the request
	 *
	 * @param content
	 * @param studentJSON
	 * @param jobOfferJSON
	 * @return
	 */
	private String getRequestBody(String content, String studentUsername, Long jobOfferId) {
		try {
			var jsonObject = new JSONObject();
			jsonObject.put("requestStatus", Constants.PDG_TAG);
			jsonObject.put("content", content);
			jsonObject.put("student", studentUsername);
			jsonObject.put("jobOffer", jobOfferId);
			return jsonObject.toString();
		} catch(JSONException e) {
			return null;
		}
	}
	
	/**
	 * Gets the visible buttons depending on the user's role
	 * 
	 * @param userType - user's role
	 */
	private void getViewButtons() {
		buttonsLayout = new HorizontalLayout();
		sendRequestButton = new Button(HEADER_TAG, new Icon(VaadinIcon.ENVELOPE_O));
		sendRequestButton.addClickListener(event -> sendRequestButtonListener());
		cancelButton = new Button(Constants.CANCEL_TAG);
		cancelButton.addClickListener(event -> cancelButtonListener());
		buttonsLayout.add(sendRequestButton, cancelButton);
	}
	
	//PARSEOS JSON
	
	/**
	 * Parses a json object to get the job offer details info
	 * 
	 * @param responseBody - http response body to parse
	 */
	private void parseJSONOffers(String responseBody) {
		try {
			var jsonObject = new JSONObject(responseBody);
			titleField.setValue(jsonObject.getString("title"));
			nameField.setValue(jsonObject.getJSONObject("company").getString("name"));
		} catch (JSONException e) {
			
			new CustomNotification(Constants.ERR_MSG, NotificationVariant.LUMO_ERROR);
		}
	}
	
	//HTTP REQUESTS
	
	/**
	 * Sends the http request to get the job offer details
	 * 
	 * @param jobOfferId - job offer's id to identify the job offer
	 * @return String - response body
	 */
	private String sendGetJobOfferRequest(Long jobOfferId) {
		var httpRequest = new HttpRequest(Constants.OFF_REQ + "/jobOffer?offerCode=" + jobOfferId);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpGet(authToken);
	}
	
	/**
	 * Sends the request and specifies if it is OK or not
	 *
	 * @return boolean - true if request is OK, false if not
	 */
	private boolean sendRequest() {
		var requestBody = getRequestBody(requestContentField.getValue(), username, jobOfferId);
		if (requestBody == null) {
			return false;
		}
		var postUrl = Constants.REQ_REQ;
		var httpRequest = new HttpRequest(postUrl);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpPost(authToken, requestBody);
	}
}
