package com.abb.pfg.views;

import org.json.JSONException;
import org.json.JSONObject;

import com.abb.pfg.custom.CustomAppLayout;
import com.abb.pfg.custom.CustomAvatar;
import com.abb.pfg.custom.CustomNotification;
import com.abb.pfg.custom.CustomTextArea;
import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.HttpRequest;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import lombok.NoArgsConstructor;

/**
 * View that shows the details of a request sent by a student
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route(Constants.REQ_DET_PATH)
@PageTitle("J4S - Detalles de la solicitud")
@NoArgsConstructor
public class RequestDetailsView extends CustomAppLayout implements HasUrlParameter<Long>, BeforeEnterObserver {
	
	private static final long serialVersionUID = 5415498538961208612L;
	//Etiquetas
	private static final String HEADER_TAG = "Detalles de la solicitud";
	private static final String OFF_TAG = "Oferta";
	private static final String INFO_TAG = "Descripción de la solicitud";
	private static final String GNR_ERR = "Se ha producido un error inesperado";
	private static final String PEND_TAG = "Pendiente";
	private static final String PROC_TAG = "Procesar";
	private static final String ACC_TAG = "Aceptar";
	private static final String REJ_TAG = "Rechazar";
	private static final String EDIT_WRN = "La solicitud ya ha sido procesada, por lo que no se puede editar";
	private static final String SENDER_TAG = "Solicitante";
	private static final String OP_CHT_TAG = "Abrir chat";
	private static final String SHW_STD_TAG = "Ver estudiante";
	private static final String SHW_CMP_TAG = "Ver empresa";
	private static final String DEL_REQ_TAG = "Eliminar solicitud";
	private static final String DEL_QST_MSG = "¿Estás seguro de que quieres eliminar la solicitud?";
	private static final String GET_CHT_ERR = "No se ha podido obtener el chat";
	private static final String DEL_REQ_MSG = "La solicitud fue eliminada con éxito";
	private static final String DEL_REQ_ERR = "La solicitud no ha podido ser eliminada";
	//Componentes
	private HorizontalLayout buttonsLayout;
	private TextField titleField, nameField, studentNameField;
	private CustomTextArea requestContentField;
	private Button pendingButton, acceptButton, rejectButton, processButton, editRequestButton, 
		cancelEditButton, saveButton,cancelButton, deleteButton;
	private MenuBar stdMenuBar, cmpMenuBar;
	private String userRole, requestJSONBody, requestStatus;
	private Long requestId;
	
	@Override
	public void setParameter(BeforeEvent event, Long parameter) {
		userRole = (String) VaadinSession.getCurrent().getAttribute("role");
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		if (authToken == null) {
            event.forwardTo(LoginView.class);
            return;
        }
		if(userRole.equals(Constants.GST_ROLE) || parameter == null) {
			event.forwardTo(AvailableOffersView.class);
			return;
		}
		requestId = parameter;
	}
	
	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		var username = (String) VaadinSession.getCurrent().getAttribute("username");
		requestJSONBody = sendRequestDetailsRequest(requestId);
		if(requestJSONBody == null) {
			event.forwardTo(RequestsView.class);
			return;
		}
		if(!userRole.equals(Constants.ADM_ROLE)) {
			if(!verifyAccessPermission(username, userRole, requestJSONBody)) {
				new CustomNotification(Constants.ACC_REJ_MSG, NotificationVariant.LUMO_ERROR);
				event.forwardTo(RequestsView.class);
				return;
			}
		}
		init();
	}
	
	/**
	 * Verifies if the user is or not allowed to acces to this view
	 * 
	 * @param username - user's username
	 * @param userRole - user's role
	 * @param requestBody - request to display
	 * @return
	 */
	private boolean verifyAccessPermission(String username, String userRole, String requestBody) {
		var usernameToVerify = new String();
		try {
			var jsonObject = new JSONObject(requestBody);
			if(userRole.equals(Constants.STD_ROLE)) {
				usernameToVerify = jsonObject.getJSONObject("student").getJSONObject("user").getString("username");
			} else {
				usernameToVerify = jsonObject.getJSONObject("jobOffer").getJSONObject("company").getJSONObject("user").getString("username");
			}
		} catch (JSONException e) {
			System.err.println(Constants.JSON_ERR + e.getMessage());
			return false;
		}
		if(username.equals(usernameToVerify)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Initializes the view components
	 * 
	 */
	private void init() {
		getRequestComponents();
		getRequestValues(requestJSONBody);
		getViewButtons(userRole);
		var baseVerticalLayout = new VerticalLayout();
		switch(userRole) {
			case Constants.STD_ROLE:
				setStudentRoleLayout(baseVerticalLayout);
				break;
			case Constants.CMP_ROLE:
				setCompanyRoleLayout(baseVerticalLayout);
				break;
			default:	//ADMIN
				setAdminRoleLayout(baseVerticalLayout);
		}
		baseVerticalLayout.setAlignItems(Alignment.CENTER);
		setContent(baseVerticalLayout);
	}
	
	/**
	 * Gets the request components where is going to be shown all the request info
	 * 
	 */
	private void getRequestComponents() {
		titleField = new TextField(OFF_TAG);
		titleField.setReadOnly(true);
		titleField.setWidth("40%");
		nameField = new TextField(Constants.COMPANY_TAG);
		nameField.setReadOnly(true);
		if(userRole.equals(Constants.CMP_ROLE)) {
			nameField.setVisible(!isVisible());
		}
		nameField.setWidthFull();
		requestContentField = new CustomTextArea(INFO_TAG, "");
		requestContentField.setWidth("60%");
		requestContentField.setReadOnly(true);
		studentNameField = new TextField(SENDER_TAG);
		studentNameField.setReadOnly(true);
		studentNameField.setWidthFull();
		if(userRole.equals(Constants.STD_ROLE)) {
			studentNameField.setVisible(false);
		}
	}
	
	/**
	 * Gets the job offer detail values from the response body
	 *
	 * @param responseBody - response body obtained from the http request
	 */
	private void getRequestValues(String responseBody) {
		if(responseBody != null) {
			var jsonObject = new JSONObject(responseBody);
			titleField.setValue(jsonObject.getJSONObject("jobOffer").getString("title"));
			nameField.setValue(jsonObject.getJSONObject("jobOffer").getJSONObject("company").getString("name"));
			requestStatus = jsonObject.getString("requestStatus");
			requestContentField.setValue(jsonObject.getString("content"));
			var stdProfilePicture = jsonObject.getJSONObject("student").getString("profilePicture");
			var cmpProfilePicture = jsonObject.getJSONObject("jobOffer").getJSONObject("company").getString("profilePicture");
			setCustomAvatarMenus(nameField.getValue() , stdProfilePicture, cmpProfilePicture);
			studentNameField.setValue(jsonObject.getJSONObject("student").getString("name"));
			return;
		}
		new CustomNotification(GNR_ERR, NotificationVariant.LUMO_ERROR);
	}
	
	/**
	 * Sets up the student's and company's avatar
	 * 
	 * @param name - user's name
	 * @param stdProfilePicture - student's profile picture
	 * @param cmpProfilePicture - company's profile picture
	 */
	private void setCustomAvatarMenus(String name, String stdProfilePicture, String cmpProfilePicture) {
		if(!userRole.equals(Constants.STD_ROLE)) {	//ADMIN Y COMPANY
			var stdCustomAvatar = (stdProfilePicture.isBlank()) ? new Avatar(name) : new CustomAvatar(stdProfilePicture);
			stdMenuBar = new MenuBar();
			stdMenuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
			var menuItem = stdMenuBar.addItem(stdCustomAvatar);
			var subMenu = menuItem.getSubMenu();
			subMenu.addItem(SHW_STD_TAG).addClickListener(event -> showStudentInfoListener());
			if(userRole.equals(Constants.CMP_ROLE)) {
				subMenu.addItem(OP_CHT_TAG).addClickListener(event -> openChatListener());
			}
		}
		if(!userRole.equals(Constants.CMP_ROLE)) {	//ADMIN Y STUDENT
			var cmpCustomAvatar = new CustomAvatar(cmpProfilePicture);
			cmpMenuBar = new MenuBar();
			cmpMenuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);
			var menuItem = cmpMenuBar.addItem(cmpCustomAvatar);
			var subMenu = menuItem.getSubMenu();
			subMenu.addItem(SHW_CMP_TAG).addClickListener(event -> showCompanyInfoListener());
		}
	}
	
	/**
	 * Gets the visible buttons depending on the user's role
	 * 
	 * @param userType - user's role
	 */
	private void getViewButtons(String userType) {
		buttonsLayout = new HorizontalLayout();
		if(userType.equals(Constants.CMP_ROLE)) {
			getUpdateRequestStateButtons(requestStatus);
		} else {
			editRequestButton = new Button(Constants.EDIT_TAG, new Icon(VaadinIcon.PENCIL));
			editRequestButton.addClickListener(event -> editButtonListener(event));
			if(!requestStatus.equals(Constants.PDG_TAG)) {
				editRequestButton.setEnabled(false);
				new CustomNotification(EDIT_WRN, NotificationVariant.LUMO_PRIMARY);
			}
			cancelButton = new Button(Constants.CANCEL_TAG);
			cancelButton.addClickListener(event -> cancelButtonListener());
			deleteButton = new Button(Constants.DELETE_TAG, new Icon(VaadinIcon.CLOSE_CIRCLE));
			deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
			deleteButton.addClickListener(event -> deleteButtonListener());
			saveButton = new Button(Constants.SV_CHG_TAG, new Icon(VaadinIcon.ENVELOPE_O));
			saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
			saveButton.addClickListener(event -> saveChangesButtonListener(event));
			cancelEditButton = new Button(Constants.CANCEL_TAG);
			cancelEditButton.addClickListener(event -> cancelEditButtonListener());
			buttonsLayout.add(editRequestButton, deleteButton, cancelButton);
		}
	}
	
	/**
	 * Gets the layout with the company options to update request state
	 * 
	 */
	private void getUpdateRequestStateButtons(String requestStatus) {
		pendingButton = new Button(PEND_TAG, new Icon(VaadinIcon.EXCLAMATION_CIRCLE_O));
		pendingButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		pendingButton.addClickListener(event -> updateRequestButtonListener(event, Constants.PDG_TAG));
		acceptButton = new Button(ACC_TAG, new Icon(VaadinIcon.CHECK_CIRCLE_O));
		acceptButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);
		acceptButton.addClickListener(event -> updateRequestButtonListener(event, Constants.ACC_TAG));
		rejectButton = new Button(REJ_TAG, new Icon(VaadinIcon.CLOSE_CIRCLE_O));
		rejectButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
		rejectButton.addClickListener(event -> updateRequestButtonListener(event, Constants.REJ_TAG));
		processButton = new Button(PROC_TAG, new Icon(VaadinIcon.CALENDAR_CLOCK));
		processButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		processButton.addClickListener(event -> updateRequestButtonListener(event, Constants.PRO_TAG));
		buttonsLayout.add(pendingButton, processButton, acceptButton, rejectButton);
		switch(requestStatus) {
			case Constants.ACC_TAG:
				acceptButton.setEnabled(false);
				break;
			case Constants.REJ_TAG:
				rejectButton.setEnabled(false);
				break;
			case Constants.PRO_TAG:
				processButton.setEnabled(false);
				break;
			default:
				pendingButton.setEnabled(false);
		}
	}
	
	/**
	 * Sets up the content for students
	 * 
	 * @param baseVerticalLayout - vertical layout where the content is going to be displayed
	 */
	private void setStudentRoleLayout(VerticalLayout baseVerticalLayout){
		var horizontalLayout = new HorizontalLayout();
		horizontalLayout.add(cmpMenuBar, nameField);
		horizontalLayout.setWidth("50%");;
		baseVerticalLayout.add(new H1(HEADER_TAG), titleField,
				horizontalLayout, requestContentField, buttonsLayout);
	}
	
	/**
	 * Sets up the content for companies
	 * 
	 * @param baseVerticalLayout - vertical layout where the content is going to be displayed
	 */
	private void setCompanyRoleLayout(VerticalLayout baseVerticalLayout) {
		var horizontalLayout = new HorizontalLayout();
		horizontalLayout.add(stdMenuBar, studentNameField);
		horizontalLayout.setWidth("50%");
		baseVerticalLayout.add(new H1(HEADER_TAG), titleField,
				nameField, horizontalLayout, requestContentField, buttonsLayout);
	}
	
	/**
	 * Sets up the content for admins
	 * 
	 * @param baseVerticalLayout - vertical layout where the content is going to be displayed
	 */
	private void setAdminRoleLayout(VerticalLayout baseVerticalLayout) {
		var horizontalLayout1 = new HorizontalLayout();
		horizontalLayout1.add(cmpMenuBar, nameField);
		horizontalLayout1.setWidth("50%");
		var horizontalLayout2 = new HorizontalLayout();
		horizontalLayout2.add(stdMenuBar, studentNameField);
		horizontalLayout2.setWidth("50%");
		baseVerticalLayout.add(new H1(HEADER_TAG), titleField,
				horizontalLayout1, horizontalLayout2, requestContentField, buttonsLayout);
	}
	
	//LISTENERS
	
	/**
	 * Listener assigned to the edit button, reserved for students only
	 * 
	 * @param event - click event performed when a student clicks on the button
	 */
	private void editButtonListener(ClickEvent<Button> event) {
		event.getSource().setVisible(!event.getSource().isVisible());
		buttonsLayout.replace(deleteButton, saveButton);
		buttonsLayout.replace(cancelButton, cancelEditButton);
		requestContentField.setReadOnly(!requestContentField.isReadOnly());
	}
	
	/**
	 * Listener assigned to the save button, reserved for students only
	 * 
	 * @param event - click event performed when a student clicks on the button
	 */
	private void saveChangesButtonListener(ClickEvent<Button> event) {
		var value = requestContentField.getValue();
		if(value.isBlank() || value == null) {
			new CustomNotification(GNR_ERR, NotificationVariant.LUMO_ERROR);
			return;
		}
		var jsonObject = new JSONObject(requestJSONBody);
		jsonObject.put("content", requestContentField.getValue());
		requestJSONBody = jsonObject.toString();
		if(sendUpdateRequest(requestJSONBody)) {
			event.getSource().setVisible(!event.getSource().isVisible());
			cancelEditButtonListener();
			new CustomNotification(Constants.UPD_MSG, NotificationVariant.LUMO_SUCCESS);
			return;
		}
		new CustomNotification(GNR_ERR, NotificationVariant.LUMO_ERROR);
	}
	
	/**
	 * Listener assigned to the cancel edit button, reserved for students only
	 * 
	 */
	private void cancelEditButtonListener() {
		editRequestButton.setVisible(!editRequestButton.isVisible());
		buttonsLayout.replace(saveButton, deleteButton);
		buttonsLayout.replace(cancelEditButton, cancelButton);
		requestContentField.setReadOnly(!requestContentField.isReadOnly());
	}
	
	/**
	 * Updates the request status
	 * 
	 * @param event - click event assigned to the button
	 * @param requestStatus - request status that will be updated
	 */
	private void updateRequestButtonListener(ClickEvent<Button> event, String requestStatus) {
		var jsonObject = new JSONObject(requestJSONBody);
		jsonObject.put("requestStatus", requestStatus);
		requestJSONBody = jsonObject.toString();
		if(sendUpdateRequest(requestJSONBody)) {
			pendingButton.setEnabled(true);
			acceptButton.setEnabled(true);
			rejectButton.setEnabled(true);
			processButton.setEnabled(true);
			event.getSource().setEnabled(false);
			new CustomNotification(Constants.UPD_MSG, NotificationVariant.LUMO_PRIMARY);
			return;
		}
		new CustomNotification(GNR_ERR, NotificationVariant.LUMO_PRIMARY);
	}
	
	/**
	 * Listener assigned to the cancel button, to go back to the requests list
	 * 
	 */
	private void cancelButtonListener() {
		this.getUI().ifPresent(ui -> ui.navigate(Constants.REQ_PATH));
	}
	
	/**
	 * Listener asigned to the delete button, which deletes the request sent by a student
	 * 
	 */
	private void deleteButtonListener() {
		var dialog = new Dialog();
		dialog.setHeaderTitle(DEL_REQ_TAG);
		dialog.add(DEL_QST_MSG);
        var deleteButton = new Button(Constants.DELETE_TAG, event -> sendDeleteRequest(dialog, requestId));
        deleteButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY,
                ButtonVariant.LUMO_ERROR);
        var cancelButton = new Button(Constants.CANCEL_TAG, event -> dialog.close());
        cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        dialog.getFooter().add(deleteButton,cancelButton);
        dialog.open();
	}
	
	/**
	 * Listener assigned to the open chat option
	 * 
	 */
	private void openChatListener() {
		var jsonObject = new JSONObject(requestJSONBody);
		var studentUsername = jsonObject.getJSONObject("student").getJSONObject("user").getString("username");
		var companyUsername = jsonObject.getJSONObject("jobOffer").getJSONObject("company").getJSONObject("user").getString("username");
		var chatCode = sendGetChatRequest(studentUsername, companyUsername);
		if(chatCode == null) {
			var requestBody = getChatBody();
			var httpPostRequest = new HttpRequest(Constants.CHATS_REQ);
			var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
			if(httpPostRequest.executeHttpPost(authToken, requestBody)) {
				var chatJsonObject = new JSONObject(requestBody);
				var stUsername = chatJsonObject.getJSONObject("student").getJSONObject("user").getString("username");
				var cmUsername = chatJsonObject.getJSONObject("company").getJSONObject("user").getString("username");
				var newChatCode = sendGetChatRequest(stUsername, cmUsername);
				this.getUI().ifPresent(ui -> ui.navigate(Constants.CHAT_BOX_PATH + "/" + newChatCode));
				return;
			}
			new CustomNotification(GET_CHT_ERR, NotificationVariant.LUMO_PRIMARY);
			return;
		}
		this.getUI().ifPresent(ui -> ui.navigate(Constants.CHAT_BOX_PATH + "/" + chatCode));
	}
	
	/**
	 * Listener assigned to the student info option
	 * 
	 */
	private void showStudentInfoListener() {
		var jsonObject = new JSONObject(requestJSONBody);
		var userId = jsonObject.getJSONObject("student").getJSONObject("user").getString("username");
		VaadinSession.getCurrent().setAttribute("userId", userId);
		var url = (userRole.equals(Constants.ADM_ROLE)) ? Constants.STD_DET_PATH + "/" + Constants.STD_ROLE : Constants.STD_DET_PATH;
		this.getUI().ifPresent(ui -> ui.navigate(url));
	}
	
	/**
	 * Listener assigned to the company info option
	 * 
	 */
	private void showCompanyInfoListener() {
		var jsonObject = new JSONObject(requestJSONBody);
		var userId = jsonObject.getJSONObject("jobOffer").getJSONObject("company").getJSONObject("user").getString("username");
		VaadinSession.getCurrent().setAttribute("userId", userId);
		var url = (userRole.equals(Constants.ADM_ROLE)) ? Constants.CMP_DET_PATH + "/" + Constants.CMP_ROLE : Constants.STD_DET_PATH;
		this.getUI().ifPresent(ui -> ui.navigate(url));
	}
	
	//PARSEOS JSON
	
	/**
	 * Builds a chat JSON object from the request details info
	 * 
	 * @return String - chat json object
	 */
	private String getChatBody() {
		var requestBody = new JSONObject(requestJSONBody);
		var chatBody = new JSONObject();
		chatBody.put("student", requestBody.getJSONObject("student"));
		chatBody.put("company", requestBody.getJSONObject("jobOffer").getJSONObject("company"));
		return chatBody.toString();	
	}
	
	//HTTP REQUESTS
	
	/**
	 * Sends the http request to obtain the request details
	 *
	 * @param requestId - request id
	 * @return String - response body
	 */
	private String sendRequestDetailsRequest(Long requestId) {
		var getUrl = Constants.REQ_REQ + "/request?requestCode=" + requestId;
		var httpRequest = new HttpRequest(getUrl);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpGet(authToken);
	}
	
	/**
	 * Sends the put request to update a request sended by a student
	 * 
	 * @param requestBody - request body to update
	 * @return boolean - true if the response is OK, false if not
	 */
	private boolean sendUpdateRequest(String requestBody) {
		var putUrl = Constants.REQ_REQ;
		var httpRequest = new HttpRequest(putUrl);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpPut(authToken, requestBody);
	}
	
	/**
	 * Listener assigned to the delete button, reserved for students only
	 * 
	 * @param dialog - error dialog to ask again if the student is sure to delete the request
	 * @param requestCode - request code of the request that will be deleted
	 */
	private void sendDeleteRequest(Dialog dialog, Long requestCode) {
		dialog.close();
		var httpRequest = new HttpRequest(Constants.REQ_REQ + "?requestCode=" + requestCode);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		if(httpRequest.executeHttpDelete(authToken)) {
			new CustomNotification(DEL_REQ_MSG, NotificationVariant.LUMO_SUCCESS);
			this.getUI().ifPresent(ui -> ui.navigate(Constants.REQ_PATH));
			return;
		}
		new CustomNotification(DEL_REQ_ERR, NotificationVariant.LUMO_ERROR);
	}
	
	/**
	 * Sends the http request to get a chat from its student and company
	 * 
	 * @param student - chat's student
	 * @param company - chat's company
	 * @return String - chat json object
	 */
	private Long sendGetChatRequest(String student, String company) {
		var httpRequest = new HttpRequest(Constants.CHATS_REQ + "?companyId=" + company + "&studentId=" + student);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		var responseBody = httpRequest.executeHttpGet(authToken);
		if(responseBody != null) {
			var jsonChatsObject = new JSONObject(responseBody);
			var numElements = jsonChatsObject.getInt("totalElements");
			if(numElements == 1) {
				var chatCode = jsonChatsObject.getJSONArray("content").getJSONObject(0).getLong("id");
				return chatCode;
			}
		}
		return null;
	}
}
