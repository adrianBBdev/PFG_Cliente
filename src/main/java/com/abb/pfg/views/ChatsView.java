package com.abb.pfg.views;

import org.json.JSONObject;

import com.abb.pfg.custom.CustomAppLayout;
import com.abb.pfg.custom.CustomChatsGrid;
import com.abb.pfg.custom.CustomNavigationOptionsPageLayout;
import com.abb.pfg.custom.CustomNotification;
import com.abb.pfg.custom.CustomNumElementsSelect;
import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.HttpRequest;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import lombok.NoArgsConstructor;

/**
 * Shows a chat between a student and a company
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route(Constants.CHT_PATH)
@PageTitle("J4S - Chats")
@NoArgsConstructor
public class ChatsView extends CustomAppLayout implements BeforeEnterObserver{
	
	private static final long serialVersionUID = 7136958004224813817L;
	//Etiquetas
	private static final String HEADER_TAG = "Mis Chats";
	private static final String MNG_CHT_TAG = "Gestionar chats";
	private static final String CHATS_ERR = "No se han podido obtener los chats del usuario";
	//Components
	private VerticalLayout mainLayout, contentLayout;
	private CustomNavigationOptionsPageLayout navigationOptionsPageLayout;
	private CustomNumElementsSelect numElementsSelect;
	private CustomChatsGrid chatsGrid;
	private TextField searchField;
	//Atributos
	private String username, userRole;
	private int numPage = 0;
	private Integer numElements;

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		username = (String) VaadinSession.getCurrent().getAttribute("username");
		userRole = (String) VaadinSession.getCurrent().getAttribute("role");
		if(authToken == null){
			event.forwardTo(LoginView.class);
			return;
		}
		if(userRole.equals(Constants.GST_ROLE)) {
			event.forwardTo(LoginView.class);
			return;
		}
		init();		//Inicializamos la vista y añadimos el layout principal
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
		mainLayout.add(searchField, contentLayout, numElementsSelect, navigationOptionsPageLayout);
		var baseVerticalLayout = new VerticalLayout();
		var headerTag = (userRole.equals(Constants.ADM_ROLE)) ? MNG_CHT_TAG : HEADER_TAG;
		baseVerticalLayout.add(new H1(headerTag), mainLayout);
		baseVerticalLayout.setAlignItems(Alignment.CENTER);
		this.setContent(baseVerticalLayout);
	}
	
	/**
	 * Sets up the content of the view's main layout
	 * 
	 */
	private void setContentLayout() {
		contentLayout = new VerticalLayout();
		contentLayout.setAlignItems(Alignment.CENTER);
		contentLayout.setMaxWidth("1000px");
		contentLayout.setWidthFull();
		setSearchField(username, userRole);
		numElementsSelect = new CustomNumElementsSelect();
		numElementsSelect.addValueChangeListener(event -> customSelectListener(event.getValue()));
		setGridContent(username, userRole);
	}
	
	/**
	 * Sets up the search field, to filter the chats
	 * 
	 * @param username - user's username
	 * @param userRole - user's role
	 */
	private void setSearchField(String username, String userRole) {
		searchField = new TextField();
		searchField.setMaxWidth("500px");
		searchField.setWidthFull();
		searchField.setPlaceholder(Constants.SEARCH_TAG);
		searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
		searchField.addValueChangeListener(event -> searchFieldListener(username, userRole));
	}
	
	/**
	 * Gets the user's chats
	 * 
	 * @param username - user's username
	 * @param userRole - user's role
	 */
	private void setGridContent(String username, String userRole) {
		var responseBody = sendGetUserChatsRequest(username, userRole);
		if(responseBody == null) {
			new CustomNotification(CHATS_ERR, NotificationVariant.LUMO_ERROR);
			return;
		}
		var jsonObject = new JSONObject(responseBody);
		numPage = jsonObject.getJSONObject("pageable").getInt("pageNumber");
		var contentArray = jsonObject.getJSONArray("content");
		var numElements = contentArray.length();
		if(chatsGrid == null) {
			setNavigationOptionsPageLayout(jsonObject.getBoolean("first"), jsonObject.getBoolean("last"));
			chatsGrid = new CustomChatsGrid(contentArray, userRole);
			contentLayout.add(chatsGrid);
		} else {
			var chatsGrid = new CustomChatsGrid(contentArray, userRole);
			contentLayout.replace(this.chatsGrid, chatsGrid);
			this.chatsGrid = chatsGrid;
		}
		if(numElements == 0) {
			new CustomNotification("No hay chats disponibles", NotificationVariant.LUMO_WARNING);
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
	
	//LISTENERS
	
	/**
	 * Listener assigned to the search field, which filter the chats
	 * 
	 * @param username - user's username
	 * @param userType - user's role
	 */
	private void searchFieldListener(String username, String userType) {
		setGridContent(username, userType);
	}
	
	/**
	 * Listener assigned to the next page option button
	 * 
	 */
	private void nextPageListener() {
		numPage++;
		setGridContent(username, userRole);
	}
	
	/**
	 * Listener assigned to the previous page option button
	 * 
	 */
	private void prevPageListener() {
		numPage--;
		setGridContent(username, userRole);
	}
	
	/**
	 * Listener assigned to the select that points the number of elements to display
	 * 
	 * @param value select's value
	 */
	private void customSelectListener(Integer value) {
		numElements = value;
		numPage = 0;
		setGridContent(username, userRole);
	}
	
	//HTTP REQUESTS
	
	/**
	 * Sends the request to obtain the user's chats
	 * 
	 * @param username - user's username
	 * @param userRole - user's role
	 * @return String - response body of the http request
	 */
	private String sendGetUserChatsRequest(String username, String userRole) {
		var getUrl = Constants.CHATS_REQ + "?page=" + numPage;
		if(userRole.equals(Constants.STD_ROLE)) {
			getUrl+= "&studentId=" + username;
		} else if(userRole.equals(Constants.CMP_ROLE)) { 
			getUrl+= "&companyId=" + username;
		}
		if(numElementsSelect.getValue() != null) {
			getUrl+= "&size=" + numElements;
		}
		if(!searchField.getValue().isBlank()) {
			switch(userRole) {
				case Constants.STD_ROLE:
					getUrl += "&companyName=" + searchField.getValue();
					break;
				case Constants.CMP_ROLE:
					getUrl += "&studentName=" + searchField.getValue();
					break;
				default: 
					getUrl += "&name=" + searchField.getValue();
					break;
			}
		}
		var httpRequest = new HttpRequest(getUrl);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpGet(authToken);
	}
}
