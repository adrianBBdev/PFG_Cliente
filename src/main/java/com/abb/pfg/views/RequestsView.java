package com.abb.pfg.views;

import org.json.JSONException;
import org.json.JSONObject;

import com.abb.pfg.custom.CustomAppLayout;
import com.abb.pfg.custom.CustomNavigationOptionsPageLayout;
import com.abb.pfg.custom.CustomNotification;
import com.abb.pfg.custom.CustomNumElementsSelect;
import com.abb.pfg.custom.CustomRequestsGrid;
import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.HttpRequest;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import lombok.NoArgsConstructor;

/**
 * Shows the requests sended by a student or the requests received by a company
 *
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route(Constants.REQ_PATH)
@PageTitle("J4S - Solicitudes a ofertas")
@NoArgsConstructor
public class RequestsView extends CustomAppLayout implements BeforeEnterObserver, HasUrlParameter<Long> {

	private static final long serialVersionUID = -6045761630213160271L;
	//Etiquetas
	private static final String HEADER_TAG = "Mis solicitudes";
	private static final String SD_REQ_ERR = "No se han podido obtener las solicitudes del usuario";
	private static final String REQ_ST_TAG = "Estado de las solicitudes";
	private static final String ALL_TAG = "Todos";
	private static final String ACC_TAG = "Aceptada";
	private static final String PEN_TAG = "Pendiente";
	private static final String PRO_TAG = "Procesada";
	private static final String REJ_TAG = "Rechazada";
	private static final String MNG_REQ_TAG = "Gestionar solicitudes";
	private static final String GET_REQ_WRN = "No hay solicitudes disponibles";
	private static final String OPT_ALL_TAG = "Todas seleccionado";
	//Componentes
	private VerticalLayout mainLayout, contentLayout, filterLayout;
	private CustomNavigationOptionsPageLayout navigationOptionsPageLayout;
	private RadioButtonGroup<String> radioButtonGroup;
	private CustomRequestsGrid requestsGrid;
	private CustomNumElementsSelect customSelect;
	private TextField searchField;
	private String username, userRole;
	private Long jobOfferId;
	private int numPage = 0;
	
	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter Long parameter) {
		if(parameter != null) {
			jobOfferId = parameter;
		}
	}
	
	@Override
    public void beforeEnter(BeforeEnterEvent event) {
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		userRole = (String) VaadinSession.getCurrent().getAttribute("role");
		if (authToken == null || userRole.equals(Constants.GST_ROLE)) {
            event.forwardTo(LoginView.class);
            return;
        }
        username = (String) VaadinSession.getCurrent().getAttribute("username");
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
		getFilterLayout();
		contentLayout = new VerticalLayout();
		contentLayout.setMaxWidth("1500px");
		contentLayout.setWidthFull();
		numPage = 0;
		customSelect = new CustomNumElementsSelect();
		customSelect.addValueChangeListener(event -> getGridContent());
		getGridContent();
		mainLayout.add(filterLayout, contentLayout, customSelect, navigationOptionsPageLayout);
		var baseVerticalLayout = new VerticalLayout();
		var headerTag = (userRole.equals(Constants.ADM_ROLE)) ? MNG_REQ_TAG : HEADER_TAG;
		baseVerticalLayout.add(new H1(headerTag), mainLayout);
		baseVerticalLayout.setAlignItems(Alignment.CENTER);
		this.setContent(baseVerticalLayout);
	}

	/**
	 * Gets the grid list to show all user requests
	 * 
	 * @throws JSONException - if a JSON error ocurrs
	 */
	private void getGridContent() throws JSONException {
		var responseBody = sendGetUsersRequest(userRole);
		if(responseBody == null) {
			new CustomNotification(SD_REQ_ERR, NotificationVariant.LUMO_ERROR);
			return;
		}
		var jsonObject = new JSONObject(responseBody);
		numPage = jsonObject.getJSONObject("pageable").getInt("pageNumber");
		var contentArray = jsonObject.getJSONArray("content");
		var numElements = contentArray.length();
		setNavigationOptionsPageLayout(jsonObject.getBoolean("first"), jsonObject.getBoolean("last"));
		if(requestsGrid == null) {
			requestsGrid = new CustomRequestsGrid(contentArray, userRole);
			contentLayout.add(requestsGrid);
		} else {
			var requestsGrid = new CustomRequestsGrid(contentArray, userRole);
			contentLayout.replace(this.requestsGrid, requestsGrid);
			this.requestsGrid = requestsGrid;
		}
		if(numElements == 0) {
			new CustomNotification(GET_REQ_WRN, NotificationVariant.LUMO_WARNING);
		}
	}
	
	/**
	 * Gets the final URL including all possible params
	 * 
	 * @param role - user's role
	 * @param requestStatus - request status requested
	 * @return String - final URL
	 */
	private String getUrlParamsRequests(String role, String requestStatus) {
		var urlParams = new String();
		if(jobOfferId != null) {
			urlParams = "&offerCode=" + jobOfferId;
		} else {
			urlParams = "&userId=" + username;
		}
		if(!requestStatus.equals(ALL_TAG)) {
			var status = new String();
			switch(requestStatus) {
				case ACC_TAG:
					status = Constants.ACC_TAG;
					break;
				case REJ_TAG:
					status = Constants.REJ_TAG;
					break;
				case PRO_TAG:
					status = Constants.PRO_TAG;
					break;
				default:
					status = Constants.PDG_TAG;
					break;
			}
			urlParams = urlParams + "&requestStatus=" + status;
		}
		return urlParams;
	}
	
	/**
	 * Gets the layout used to filter user requests
	 *
	 */
	private void getFilterLayout() {
		filterLayout = new VerticalLayout();
		filterLayout.setAlignItems(Alignment.CENTER);
		if(!userRole.equals(Constants.ADM_ROLE)) {
			radioButtonGroup = new RadioButtonGroup<>();
			radioButtonGroup.setLabel(REQ_ST_TAG);
			radioButtonGroup.setItems(ALL_TAG, ACC_TAG, PEN_TAG, PRO_TAG, REJ_TAG);
			radioButtonGroup.setValue(ALL_TAG);
			radioButtonGroup.addValueChangeListener(event -> getGridContent());
			filterLayout.add(radioButtonGroup);
		}
		searchField = new TextField();
		searchField.setMaxWidth("500px");
		searchField.setWidthFull();
		searchField.setPlaceholder(Constants.SEARCH_TAG);
		searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
		searchField.addValueChangeListener(event -> getGridContent());
		filterLayout.add(searchField);
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
		filterListener(radioButtonGroup.getValue());
	}

	/**
	 * Listener assigned to the previous page button
	 */
	private void prevPageListener() {
		numPage--;
		filterListener(radioButtonGroup.getValue());
	}

	/**
	 * Listener assigned to the radio button group to filter requests
	 *
	 * @param requestStateValue - request state value to filter
	 */
	private void filterListener(String requestStateValue) {
		if(requestStateValue.equals(ALL_TAG)) {
			new CustomNotification(OPT_ALL_TAG, NotificationVariant.LUMO_PRIMARY);
			return;
		}
		new CustomNotification(requestStateValue + " seleccionado", NotificationVariant.LUMO_PRIMARY);
	}
	
	//HTTP REQUESTS
	
	/**
	 * Sends the request to obtain the users requests already sent
	 * 
	 * @param role - user's role
	 * @param requestStatus - current request status of the requested requests
	 * @return String - response body of the http request
	 */
	private String sendGetUsersRequest(String role) {
		var getUrl = Constants.REQ_REQ + "?page=" + numPage;
		if(!userRole.equals(Constants.ADM_ROLE)) {
			getUrl += getUrlParamsRequests(role, radioButtonGroup.getValue());
		}
		getUrl = (searchField.getValue().isEmpty()) ? getUrl 
				: getUrl + "&name=" + searchField.getValue();
		var numElements = customSelect.getValue();
		getUrl = (numElements == null) ? getUrl : getUrl + "&size=" + numElements;
		var httpRequest = new HttpRequest(getUrl);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpGet(authToken);
	}
}
