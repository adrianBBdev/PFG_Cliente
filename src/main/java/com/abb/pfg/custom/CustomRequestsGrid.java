package com.abb.pfg.custom;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.HttpRequest;
import com.abb.pfg.utils.RequestListComponent;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.contextmenu.GridContextMenu.GridContextMenuItemClickEvent;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.server.VaadinSession;

/**
 * Custom grid which shows a list of requests
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
public class CustomRequestsGrid extends Grid<RequestListComponent> {

	private static final long serialVersionUID = 2283140912438746255L;
	private static final String ACC_TAG = "Aceptada";
	private static final String PEN_TAG = "Pendiente";
	private static final String PRO_TAG = "Procesada";
	private static final String REJ_TAG = "Rechazada";
	private static final String SHW_TAG = "Ver solicitud";
	private static final String DATE_TAG = "Realizada el día";
	private static final String PARSE_ERR = "Se ha producido un error obteniendo el estado de la solicitud";
	private static final String GET_OFF_ERR = "No se puede mostrar alguna de las solicitudes";
	private static final String APP_TAG = "Solicitante";
	private List<RequestListComponent> requests;
	
	/**
	 * Default class constructor
	 * 
	 * @param numElements - number of elements that the grid will have
	 * @param contentArray - JSON array with the content
	 * @param userType - user's role
	 */
	public CustomRequestsGrid(int numElements, JSONArray contentArray, String userRole) {
		var gridData = getGridData(numElements, contentArray);
		this.setAllRowsVisible(true);
		this.setItems(gridData);
		this.addColumn(request -> request.getTitle()).setHeader(Constants.TITLE_TAG);
		switch(userRole) {
			case Constants.STD_ROLE:
				setStudentRoleGrid(userRole);
				break;
			case Constants.CMP_ROLE:
				setCompanyRoleGrid(userRole);
				break;
			default: 
				setAdminRoleGrid(userRole);
		}
	}
	
	/**
	 * Gets the requests list to show to users their own requests
	 *
	 * @param numElements - number of elements that the list will have
	 * @param contentArray - JSON array which contains all job offers to show
	 * @return List<RequestListComponent> - requests list
	 */
	private List<RequestListComponent> getGridData(int numElements, JSONArray contentArray){
		requests = new ArrayList<>();
		for(var i=0; i<numElements; i++) {
			var jsonObject = (JSONObject) contentArray.get(i);
			var requestCode = jsonObject.getLong("id");
			var requestTitle = jsonObject.getJSONObject("jobOffer").getString("title");
			var requestTimeStamp = jsonObject.getString("timeStamp");
			var originalDate = requestTimeStamp.substring(0, 10);
			var date = LocalDate.parse(originalDate);
			var requestCompany = jsonObject.getJSONObject("jobOffer").getJSONObject("company").getString("name");
			var requestStatus = parseRequestStateValue(jsonObject.getString("requestStatus"));
			var studentName = jsonObject.getJSONObject("student").getString("name");
			var studentUsername = jsonObject.getJSONObject("student").getJSONObject("user").getString("username");
			requests.add(new RequestListComponent(requestCode, requestTitle, 
					requestCompany, date, requestStatus, studentName, studentUsername));
		}
		return requests;
	}
	
	private void setStudentRoleGrid(String userRole) {
		this.addColumn(request -> request.getCompanyName()).setHeader(Constants.COMPANY_TAG);
		this.addColumn(request -> request.getRequestDate()).setHeader(DATE_TAG);
		this.addComponentColumn(request -> createStatusIcon(request.getRequestStatus())).setHeader(Constants.STATUS_TAG);
		var requestsGrid = this.addContextMenu();
		requestsGrid.setOpenOnClick(true);
		requestsGrid.addItem(SHW_TAG, event -> requestDetailsListener(event.getItem().get().getRequestCode()));
		requestsGrid.addItem("Ver " + Constants.COMPANY_TAG, event -> showUserInfoDetailsListener(event.getItem().get(), 
				Constants.COMPANY_TAG, userRole));
		requestsGrid.addItem(Constants.DELETE_TAG, event -> deleteRequestListener(event));
	}
	
	private void setCompanyRoleGrid(String userRole) {
		this.addColumn(request -> request.getStudentName()).setHeader(APP_TAG);
		this.addColumn(request -> request.getRequestDate()).setHeader(DATE_TAG);
		this.addComponentColumn(request -> createStatusIcon(request.getRequestStatus())).setHeader(Constants.STATUS_TAG);
		var requestsGrid = this.addContextMenu();
		requestsGrid.setOpenOnClick(true);
		requestsGrid.addItem(SHW_TAG, event -> requestDetailsListener(event.getItem().get().getRequestCode()));
		requestsGrid.addItem("Ver " + Constants.STUDENT_TAG, event -> showUserInfoDetailsListener(event.getItem().get(), 
				Constants.STUDENT_TAG, userRole));
		requestsGrid.addItem("Abrir chat", event -> openChatListener(event));
	}
	
	private void setAdminRoleGrid(String userRole) {
		this.addColumn(request -> request.getCompanyName()).setHeader(Constants.COMPANY_TAG);
		this.addColumn(request -> request.getStudentName()).setHeader(APP_TAG);
		this.addColumn(request -> request.getRequestDate()).setHeader(DATE_TAG);
		this.addComponentColumn(request -> createStatusIcon(request.getRequestStatus())).setHeader(Constants.STATUS_TAG);
		var requestsGrid = this.addContextMenu();
		requestsGrid.setOpenOnClick(true);
		requestsGrid.addItem(SHW_TAG, event -> requestDetailsListener(event.getItem().get().getRequestCode()));
		requestsGrid.addItem("Ver " + Constants.COMPANY_TAG, event -> showUserInfoDetailsListener(event.getItem().get(), 
				Constants.COMPANY_TAG, userRole));
		requestsGrid.addItem("Ver " + Constants.STUDENT_TAG, event -> showUserInfoDetailsListener(event.getItem().get(), 
				Constants.STUDENT_TAG, userRole));
		requestsGrid.addItem(Constants.DELETE_TAG, event -> deleteRequestListener(event));
	}
	
	//LISTENERS
	
	/**
	 * Listener assigned to the view request details option
	 * 
	 * @param requestCode - request code of the selected request
	 */
	private void requestDetailsListener(Long requestCode) {
		if(requestCode != null) {
			this.getUI().ifPresent(ui -> ui.navigate(Constants.REQ_DET_PATH + "/" + requestCode));
			return;
		}
		new CustomNotification(GET_OFF_ERR, NotificationVariant.LUMO_PRIMARY);
	}
	
	/**
	 * Listener assigned to the open chat option
	 * 
	 * @param event - event which happened when the user clicks on the option
	 */
	private void openChatListener(GridContextMenuItemClickEvent<RequestListComponent> event) {
		var studentUsername = event.getItem().get().getStudentUsername();
		var companyUsername = (String) VaadinSession.getCurrent().getAttribute("username");
		var chatCode = sendGetChatRequest(studentUsername, companyUsername);
		if(chatCode == null) {
			var requestBody = getChatBody(studentUsername, companyUsername);
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
			new CustomNotification("No se ha podido obtener el chat", NotificationVariant.LUMO_PRIMARY);
			return;
		}
		this.getUI().ifPresent(ui -> ui.navigate(Constants.CHAT_BOX_PATH + "/" + chatCode));
	}
	
	/**
	 * Listener assigned to the delete request option
	 * 
	 * @param event - click event when the user clicks on the option
	 */
	private void deleteRequestListener(GridContextMenuItemClickEvent<RequestListComponent> event) {
		var httpRequest = new HttpRequest(Constants.REQ_REQ + "?requestCode=" + event.getItem().get().getRequestCode());
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		if(httpRequest.executeHttpDelete(authToken)) {
			new CustomNotification(Constants.DEL_MSG, NotificationVariant.LUMO_PRIMARY);
			requests.remove(event.getItem().get());
			this.getDataProvider().refreshAll();
			return;
		}
		new CustomNotification(Constants.DEL_ERR, NotificationVariant.LUMO_PRIMARY);
	}
	
	/**
	 * Listener assigned to the show student and show company grid context option
	 * 
	 * @param event - event performed when the user clicks on the option
	 * @param tag - option tag
	 */
	private void showUserInfoDetailsListener(RequestListComponent item, String tag, String userRole) {
		var requestCode = item.getRequestCode();
		var httpRequest = new HttpRequest(Constants.REQ_REQ + "/request?requestCode=" + requestCode);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		var responseBody = httpRequest.executeHttpGet(authToken);
		var jsonObject = new JSONObject(responseBody);
		if(tag.equals(Constants.STUDENT_TAG)) {
			var userId = jsonObject.getJSONObject("student").getJSONObject("user").getString("username");
			VaadinSession.getCurrent().setAttribute("userId", userId);
			var path = (userRole.equals(Constants.ADM_ROLE) 
					? Constants.STD_DET_PATH + "/" + Constants.STD_ROLE : Constants.STD_DET_PATH);
			this.getUI().ifPresent(ui -> ui.navigate(path));
		} else if(tag.equals(Constants.COMPANY_TAG)) {
			var userId = jsonObject.getJSONObject("jobOffer").getJSONObject("company").getJSONObject("user").getString("username");
			VaadinSession.getCurrent().setAttribute("userId", userId);
			var path = (userRole.equals(Constants.ADM_ROLE) 
					? Constants.CMP_DET_PATH + "/" + Constants.CMP_ROLE : Constants.CMP_DET_PATH);
			this.getUI().ifPresent(ui -> ui.navigate(path));
		}
	}
	
	/**
	 * Parses the request state value to an understanding value
	 *
	 * @param requestStatusValue - request state value to parse
	 * @return String - final request state value
	 */
	private String parseRequestStateValue(String requestStatusValue) {
		var requestStatusParsed = new String();
		switch(requestStatusValue) {
			case Constants.ACC_TAG:
				requestStatusParsed = ACC_TAG;
				break;
			case Constants.PDG_TAG:
				requestStatusParsed = PEN_TAG;
				break;
			case Constants.PRO_TAG:
				requestStatusParsed = PRO_TAG;
				break;
			case Constants.REJ_TAG:
				requestStatusParsed = REJ_TAG;
				break;
			default:
				new CustomNotification(PARSE_ERR, NotificationVariant.LUMO_ERROR);
				requestStatusParsed = null;
				break;
		}
		return requestStatusParsed;
	}
	
	/**
	 * Creates an icon depending on the request status that the request has
	 * 
	 * @param status - request status
	 * @return Icon - final Icon object
	 */
	private Icon createStatusIcon(String status) {
		Icon icon;
        switch (status) {
        	case PRO_TAG:
        		icon = createIcon(VaadinIcon.CALENDAR_CLOCK, PRO_TAG);
        		icon.getElement().getThemeList().add("badge primary");
        		break;
        	case ACC_TAG:
        		icon = createIcon(VaadinIcon.CHECK, ACC_TAG);
        		icon.getElement().getThemeList().add("badge success");
        		break;
        	case REJ_TAG:
        		icon = createIcon(VaadinIcon.CLOSE_SMALL, REJ_TAG);
        		icon.getElement().getThemeList().add("badge success");
        		break;
        	default:
        		icon = createIcon(VaadinIcon.EXCLAMATION_CIRCLE_O, PEN_TAG);
        		icon.getElement().getThemeList().add("badge primary");
        		break;
        }
        return icon;
    }
	
	/**
	 * Creates an icon to show on the requests grid list
	 * 
	 * @param vaadinIcon - default Vaadin icon to use
	 * @param label - label or title the icon will have
	 * @return Icon - final Icon object
	 */
	private Icon createIcon(VaadinIcon vaadinIcon, String label) {
	    Icon icon = vaadinIcon.create();
	    icon.getStyle().set("padding", "var(--lumo-space-xs");
	    icon.getElement().setAttribute("aria-label", label);
	    icon.getElement().setAttribute("title", label);
	    return icon;
	}
	
	//PARSEOS JSON
	
	/**
	 * Builds a chat JSON object from the request details info
	 * 
	 * @param studentUsername - student's username
	 * @param companyUsername - company's username
	 * @return String - chat json object
	 */
	private String getChatBody(String studentUsername, String companyUsername) {
		var httpRequest1 = new HttpRequest(Constants.STD_REQ + "/student?username=" + studentUsername);
		var httpRequest2 = new HttpRequest(Constants.CMP_REQ + "/company?username=" + companyUsername);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		var studentBody = httpRequest1.executeHttpGet(authToken);
		var companyBody = httpRequest2.executeHttpGet(authToken);
		var chatBody = new JSONObject();
		chatBody.put("student", new JSONObject(studentBody));
		chatBody.put("company", new JSONObject(companyBody));
		return chatBody.toString();
	}
	
	//HTTP REQUESTS
	
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
