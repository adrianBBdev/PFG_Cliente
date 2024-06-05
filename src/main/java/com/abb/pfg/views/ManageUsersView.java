package com.abb.pfg.views;

import org.json.JSONException;
import org.json.JSONObject;

import com.abb.pfg.custom.CustomAppLayout;
import com.abb.pfg.custom.CustomNavigationOptionsPageLayout;
import com.abb.pfg.custom.CustomNotification;
import com.abb.pfg.custom.CustomNumElementsSelect;
import com.abb.pfg.custom.CustomUsersGrid;
import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.HttpRequest;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

/**
 * View which administrator uses to managed all web app users
 *
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route(Constants.MNG_USERS_PATH)
@PageTitle("J4S - Gestionar usuarios")
public class ManageUsersView extends CustomAppLayout implements BeforeEnterObserver{

	private static final long serialVersionUID = -7377435871469996975L;
	//Etiquetas
	private static final String HEADER_TAG = "Gestión de usuarios";
	private static final String ADM_TAB = "Administradores";
	private static final String STD_TAB = "Estudiantes";
	private static final String CMP_TAB = "Empresas";
	private static final String SEL_USR_ERR = "No se han podido obtener los usuarios seleccionados";
	//Componentes
	private VerticalLayout mainLayout, tabContentLayout;
	private CustomNavigationOptionsPageLayout navigationOptionsPageLayout;
	private Tabs userTabs;
	private Tab adminsTab, studentsTab, companysTab;
	private CustomNumElementsSelect customSelect;
	private Button addNewUserButton;
	//Atributos
	private String userRole;
	private int numPage = 0;
	private Integer numElements;

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		userRole = (String) VaadinSession.getCurrent().getAttribute("role");
		if(authToken == null || !userRole.equals(Constants.ADM_ROLE)){
			event.forwardTo(LoginView.class);
			return;
		}
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
		customSelect = new CustomNumElementsSelect();
		customSelect.addValueChangeListener(event -> customSelectListener(event.getValue()));
		addNewUserButton = new Button("Crear usuario", new Icon(VaadinIcon.PLUS_CIRCLE_O));
		addNewUserButton.addClickListener(event -> addNewUserButtonListener(userTabs.getSelectedTab().getLabel()));
		userTabs = new Tabs();
		userTabs.setAutoselect(true);
		userTabs.addSelectedChangeListener(event -> setTabContent(event.getSelectedTab()));
		tabContentLayout = new VerticalLayout();
		tabContentLayout.setAlignItems(Alignment.CENTER);
		adminsTab = new Tab(ADM_TAB);
		studentsTab = new Tab(STD_TAB);
		companysTab = new Tab(CMP_TAB);
		userTabs.add(adminsTab, studentsTab, companysTab);
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
		if(tab.equals(adminsTab)) {
			setAdminsLayout();
		} else if(tab.equals(studentsTab)) {
			setStudentsLayout();
		} else if(tab.equals(companysTab)) {
			setCompanysLayout();
		}
	}
	
	/**
	 * Sets up the layout with a list of all administrators
	 * 
	 */
	private void setAdminsLayout() {
		var adminsBody = sendGetRequest(numPage, numElements, Constants.ADMIN_REQ);
		setGridLayout(adminsBody, userRole);
	}
	
	/**
	 * Sets up the layout with a list of all students
	 * 
	 */
	private void setStudentsLayout() {
		var studentsBody = sendGetRequest(numPage, numElements, Constants.STD_REQ);
		setGridLayout(studentsBody, Constants.STD_ROLE);
	}
	
	/**
	 * Sets up the layout with a list of all companies
	 * 
	 */
	private void setCompanysLayout() {
		var companysBody = sendGetRequest(numPage, numElements, Constants.CMP_REQ);
		setGridLayout(companysBody, Constants.CMP_ROLE);
	}
	
	/**
	 * Sets up the grid which displays the list of users
	 * 
	 * @param usersBody - list of users to display
	 * @param userRole - user's role of the users that are being displayed
	 */
	private void setGridLayout(String usersBody, String userRole) {
		if(usersBody == null) {
			new CustomNotification(SEL_USR_ERR, NotificationVariant.LUMO_ERROR);
			return;
		}
		try {
			var jsonObject = new JSONObject(usersBody);
			var contentArray = jsonObject.getJSONArray("content");
			var isShowingFirst = jsonObject.getBoolean("first");
			var isShowingLast = jsonObject.getBoolean("last");
			var usersGrid = new CustomUsersGrid(contentArray, userRole);
			var usersLayout = new VerticalLayout();
			var maxWidth = (userRole.equals(Constants.ADM_ROLE)) ? "800px" : "1200px";
			usersLayout.setMaxWidth(maxWidth);
			usersLayout.setWidthFull();
			usersLayout.add(usersGrid);
			setNavigationOptionsPageLayout(isShowingFirst, isShowingLast);
			tabContentLayout.add(addNewUserButton, usersLayout, customSelect, navigationOptionsPageLayout);
		} catch (JSONException e) {
			new CustomNotification(SEL_USR_ERR, NotificationVariant.LUMO_ERROR);
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
	
	//LISTENERS
	
	/**
	 * Listener assigned to the next page button
	 * 
	 */
	private void nextPageListener() {
		numPage++;
		setTabContent(userTabs.getSelectedTab());
	} 
	
	/**
	 * Listener assigned to the previous page button
	 * 
	 */
	private void prevPageListener() {
		numPage--;
		setTabContent(userTabs.getSelectedTab());
	}
	
	/**
	 * Listener assigned to the create user option button
	 * 
	 * @param label
	 */
	private void addNewUserButtonListener (String label) {
		var userCategory = new String();
		switch(label) {
			case STD_TAB:
				userCategory = Constants.STD_ROLE;
				break;
			case CMP_TAB:
				userCategory = Constants.CMP_ROLE;
				break;
			default:
				userCategory = Constants.ADM_ROLE;
				break;
		}
		this.getUI().get().navigate(Constants.CREATE_USER_PATH + "/" + userCategory);
	}
	
	/**
	 * Listener assigned to the Select component which displays the number of elements
	 * 
	 * @param value - select's value
	 */
	
	private void customSelectListener(Integer value) {
		numElements = value;
		numPage = 0;
		setTabContent(userTabs.getSelectedTab());
	}
	
	//HTTP REQUESTS
	
	/**
	 * Sends the http request to obtain the specified users
	 * 
	 * @param numPage - page number of the results
	 * @param numElements - number elements to show
	 * @param getUrl - url neededd to send the http request
	 * @return String - response body
	 */
	private String sendGetRequest(int numPage, Integer numElements, String getUrl) {
		getUrl+= "?page=" + numPage;
		getUrl = (numElements != null) ? getUrl+= "&size=" + numElements : getUrl;
		var httpRequest = new HttpRequest(getUrl);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpGet(authToken);
	}
}
