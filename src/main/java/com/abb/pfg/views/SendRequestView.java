package com.abb.pfg.views;

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

/**
 * View that shows the option of sending a request about a job offer
 *
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route(Constants.SEND_REQ_PATH)
@PageTitle("J4S - Enviar solicitud")
public class SendRequestView extends CustomAppLayout implements HasUrlParameter<Long>{

	private static final long serialVersionUID = -2334599638209630511L;
	//Etiquetas
	private static final String HEADER_TAG = "Enviar solicitud";
	private static final String INFO_TAG = "Escribe una breve presentación";
	private static final String OFF_TAG = "Oferta";
	private static final String GNR_ERR = "Se ha producido un error inesperado";
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
	private String jobOfferJSON;
	private Long jobOfferId;

	/**
	 * Default class constructor
	 *
	 */
	public SendRequestView() {}

	@Override
	public void setParameter(BeforeEvent event, Long parameter) {
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		var userType = (String) VaadinSession.getCurrent().getAttribute("role");
		if(authToken == null || userType == null || userType == Constants.GST_ROLE || userType == Constants.CMP_ROLE){
			event.forwardTo(LoginView.class);
			new CustomNotification(ACC_REJ_ERR, NotificationVariant.LUMO_ERROR);
		}
		jobOfferId = parameter;
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
		jobOfferJSON = sendJobOfferDetailsRequest(jobOfferId);
		getJobOfferValues(jobOfferJSON);
		requestContentField = new CustomTextArea(INFO_TAG, "");
		requestContentField.setWidth("60%");
		getViewButtons();
		var baseVerticalLayout = new VerticalLayout();
		baseVerticalLayout.add(new H1(HEADER_TAG), titleField,
				nameField, requestContentField, buttonsLayout);
		baseVerticalLayout.setAlignItems(Alignment.CENTER);
		this.setContent(baseVerticalLayout);
	}

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
	 * Gets the job offer detail values from the response body
	 *
	 * @param responseBody - response body obtained from the http request
	 */
	private void getJobOfferValues(String responseBody) {
		if(responseBody != null) {
			var jsonObject = new JSONObject(responseBody);
			titleField.setValue(jsonObject.getString("title"));
			var companyJSON = jsonObject.getJSONObject("company");
			nameField.setValue(companyJSON.getString("name"));
			return;
		}
		new CustomNotification(GNR_ERR, NotificationVariant.LUMO_ERROR);
	}

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

	/**
	 * Sends the request and specifies if it is OK or not
	 *
	 * @return boolean - true if request is OK, false if not
	 */
	private boolean sendRequest() {
		var studentJSON = sendGetStudentRequest();
		if(studentJSON == null) {
			return false;
		}
		var requestBody = getRequestBody(requestContentField.getValue(), studentJSON, jobOfferJSON);
		if (requestBody == null) {
			return false;
		}
		var postUrl = Constants.REQ_REQ;
		var httpRequest = new HttpRequest(postUrl);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpPost(authToken, requestBody);
	}

	/**
	 * Sends the http request to get the student info
	 *
	 * @return String - response body
	 */
	private String sendGetStudentRequest() {
		var username = VaadinSession.getCurrent().getAttribute("username");
		if(username == null) {
			return null;
		}
		var getUrl = Constants.STD_REQ + "/student?username=" + username;
		var httpRequest = new HttpRequest(getUrl);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpGet(authToken);
	}

	/**
	 * Gets the request body needed to send the request
	 *
	 * @param content
	 * @param studentJSON
	 * @param jobOfferJSON
	 * @return
	 */
	private String getRequestBody(String content, String studentJSON, String jobOfferJSON) {
		var jsonObject = new JSONObject();
		var studentObject = new JSONObject(studentJSON);
		var jobOfferObject = new JSONObject(jobOfferJSON);
		jsonObject.put("requestState", Constants.PDG_TAG);
		jsonObject.put("content", content);
		jsonObject.put("student", studentObject);
		jsonObject.put("jobOffer", jobOfferObject);
		return jsonObject.toString();
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
}
