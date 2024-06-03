package com.abb.pfg.views;

import org.json.JSONObject;

import com.abb.pfg.custom.CustomAppLayout;
import com.abb.pfg.custom.CustomJobOfferListItem;
import com.abb.pfg.custom.CustomJobOffersGrid;
import com.abb.pfg.custom.CustomNavigationOptionsPageLayout;
import com.abb.pfg.custom.CustomNotification;
import com.abb.pfg.custom.CustomNumElementsSelect;
import com.abb.pfg.custom.CustomSelectAreas;
import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.HttpRequest;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import lombok.NoArgsConstructor;

/**
 * Default view when user access to the app. Shows a list of job offers
 *
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route(Constants.OFFERS_PATH)
@PageTitle("J4S - Ofertas disponibles")
@NoArgsConstructor
public class AvailableOffersView extends CustomAppLayout implements BeforeEnterObserver{

	private static final long serialVersionUID = -6549352517346464385L;
	//Etiquetas o títulos
	private static final String HEADER_TAG = "Ofertas disponibles";
	private static final String FILTER_TAG = "Filtrar";
	private static final String DUR_MIN_TAG = "Duración mín. (en meses)";
	private static final String DUR_MAX_TAG = "Duración máx. (en meses)";
	private static final String FILTER_ERR = "Rellene los campos correctamente para poder filtrar las ofertas";
	private static final String OFF_WRN = "No hay ofertas disponibles";
	//Elementos
	private VerticalLayout mainLayout, contentLayout, filterLayout;
	private HorizontalLayout durationOptionsLayout;
	private CustomNavigationOptionsPageLayout navigationOptionsPageLayout;
	private CustomJobOffersGrid jobOffersGrid;
	private Button filterButton;
	private Select<String> modalitySelect;
	private CustomSelectAreas areaSelect;
	private TextField cityField, searchField;
	private NumberField minDurationField, maxDurationField;
	private CustomNumElementsSelect customNumElementsSelect;
	private String userRole, username;
	private boolean isShowingAllOffers;
	private int numPage = 0;

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		if(authToken == null){
			event.forwardTo(LoginView.class);
			return;
		}
		username = (String) VaadinSession.getCurrent().getAttribute("username");
		userRole = (String) VaadinSession.getCurrent().getAttribute("role");
		init();		//Inicializamos la vista y añadimos el layout principal
	}

	/**
	 * Initialices the view components
	 *
	 */
	private void init() {
		mainLayout = new VerticalLayout();		//Inicializamos el layout principal
		mainLayout.setAlignItems(Alignment.CENTER);
		mainLayout.setWidthFull();
		setFilterLayout(userRole);
		isShowingAllOffers = true;
		customNumElementsSelect = new CustomNumElementsSelect();
		customNumElementsSelect.addValueChangeListener(event -> setContentLayout());
		contentLayout = new VerticalLayout();
		contentLayout.setWidth("70%");
		setContentLayout();
		mainLayout.add(filterLayout, contentLayout, navigationOptionsPageLayout, customNumElementsSelect);
		var baseVerticalLayout = new VerticalLayout();
		baseVerticalLayout.add(new H1(HEADER_TAG), mainLayout);
		baseVerticalLayout.setAlignItems(Alignment.CENTER);
		this.setContent(baseVerticalLayout);
	}

	/**
	 * Gets the layout which contains the job offers filter components
	 * 
	 * @param userRole - user's role
	 */
	private void setFilterLayout(String userRole) {
		filterLayout = new VerticalLayout();
		filterLayout.setAlignItems(Alignment.CENTER);
		if(userRole.equals(Constants.ADM_ROLE)) {
			setSearchField();
			return;
		} 
		modalitySelect = new Select<>();
		modalitySelect.setEmptySelectionAllowed(true);
		modalitySelect.setLabel(Constants.MODALITY_TAG);
		modalitySelect.setItems(Constants.PRES_TAG, Constants.HYBR_TAG, Constants.TELE_TAG);
		areaSelect = new CustomSelectAreas();
		cityField = new TextField(Constants.CITY_TAG);
		cityField.setWidth("60%");
		var horizontalLayoutSelects = new HorizontalLayout();
		horizontalLayoutSelects.add(cityField, areaSelect, modalitySelect);
		setDurationOptionsLayout();
		filterButton = new Button(FILTER_TAG);
		filterButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		filterButton.addClickListener(event -> filterListener(true));
		filterLayout.add(horizontalLayoutSelects, durationOptionsLayout, filterButton);
	}
	
	/**
	 * Sets up the search field, to filter the offers
	 * 
	 */
	private void setSearchField() {
		searchField = new TextField();
		searchField.setWidth("30%");
		searchField.setPlaceholder(Constants.SEARCH_TAG);
		searchField.setPrefixComponent(new Icon(VaadinIcon.SEARCH));
		searchField.addValueChangeListener(event -> filterListener(true));
		filterLayout.add(searchField);
	}

	/**
	 * Gets the components related with job offer duration
	 *
	 */
	private void setDurationOptionsLayout() {
		durationOptionsLayout = new HorizontalLayout();
		minDurationField = new NumberField(DUR_MIN_TAG);
		maxDurationField = new NumberField(DUR_MAX_TAG);
		durationOptionsLayout.add(minDurationField, maxDurationField);
	}

	/**
	 * Listener assigned to the filter button to execute the job offers filter action
	 *
	 * @param isFiltering - true if is filtering, false if not
	 */
	private void filterListener(boolean isFiltering) {
		if(userRole.equals(Constants.ADM_ROLE)) {
			setContentLayout();
		} else {
			if(!isValid(cityField.getValue()) && !isValid(areaSelect.getValue())
					&& !isValid(modalitySelect.getValue()) && !isValid(minDurationField.getValue())
					&& !isValid(maxDurationField.getValue()) && isShowingAllOffers && isFiltering) {
				new CustomNotification(FILTER_ERR, NotificationVariant.LUMO_PRIMARY);
				return;
			}
			setContentLayout();
		}
	}

	/**
	 * Specifies if an object is valid or not
	 *
	 * @param value - value to verify
	 * @return boolean - true if is valid, false if not
	 */
	public boolean isValid(Object value) {
		if(value == null || String.valueOf(value).isBlank()) {
			return false;
		}
		return true;
	}

	/**
	 * Gets the content layout where appears all job offers to show to each user
	 *
	 */
	private void setContentLayout() {
		var jobOffersBody = sendAvailableJobOffersRequest();
		if(jobOffersBody != null) {
			var jobOffersJSON = new JSONObject(jobOffersBody);
			var isShowingFirst = jobOffersJSON.getBoolean("first");
			var isShowingLast = jobOffersJSON.getBoolean("last");
			setNavigationOptionsPageLayout(isShowingFirst, isShowingLast);
			numPage = jobOffersJSON.getJSONObject("pageable").getInt("pageNumber");
			var contentArray = jobOffersJSON.getJSONArray("content");
			var numElements = contentArray.length();
			if(userRole.equals(Constants.CMP_ROLE) || userRole.equals(Constants.ADM_ROLE)) {
				if(jobOffersGrid == null) {
					jobOffersGrid = new CustomJobOffersGrid(contentArray, userRole, false);
					contentLayout.add(jobOffersGrid);
				} else {
					var jobOffersGrid = new CustomJobOffersGrid(contentArray, userRole, false);
					contentLayout.replace(this.jobOffersGrid, jobOffersGrid);
					this.jobOffersGrid = jobOffersGrid;
				}
			} else {
				contentLayout.removeAll();
				for(var i=0; i<numElements; i++) {
					parseJobOfferJSON(contentArray.getJSONObject(i));
				}
			}
			if(numElements == 0) {
				new CustomNotification(OFF_WRN, NotificationVariant.LUMO_WARNING);
			}
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
	 * Listener assigned to the next page button
	 *
	 */
	private void nextPageListener() {
		numPage++;
		filterListener(false);
	}

	/**
	 * Listener assigned to the previous page button
	 */
	private void prevPageListener() {
		numPage--;
		filterListener(false);
	}

	/**
	 * Builds the appropiate URL to send the request for the available offers
	 *
	 * @param url - initial  URL before adding parameters
	 * @param paramName - name of the param to add if required
	 * @param param - value of the param to add if required
	 * @return Sting - final URL with params or not
	 */
	private String getAvailableJobOffersURLStringParameter(String url, String paramName , String param) {
		var separator = (url.contains("?") ? "&" : "?");
		var finalUrl = (isValid(param)) ? url + separator + paramName + "=" + param : url;
		return finalUrl;
	}

	/**
	 * Builds the appropiate URL to send the request for the available offers
	 *
	 * @param url - initial  URL before adding parameters
	 * @param paramName - name of the param to add if required
	 * @param param - value of the param to add if required
	 * @return Sting - final URL with params or not
	 */
	private String getAvailableJobOffersURLIntParameter(String url, String paramName , Integer param) {
		var separator = (url.contains("?") ? "&" : "?");
		var finalUrl = (isValid(param) && param > 0) ? url + separator + paramName + "=" + param : url;
		return finalUrl;
	}
	
	//PARSEOS JSON

	/**
	 * Gets all job offer parameters from the JSON object given
	 *
	 * @param jobOfferJSON - job offer JSON object to analyze
	 */
	private void parseJobOfferJSON(JSONObject jobOfferJSON) {
		var jobOfferId = jobOfferJSON.getLong("id");
		var isSaved = false;
		if(userRole.equals(Constants.STD_ROLE)) {
			isSaved = isJobOfferSaved(username, jobOfferId);
		}
		var listItem = new CustomJobOfferListItem(jobOfferJSON.toString(), isSaved);
		contentLayout.add(listItem);
	}
	
	//HTTP REQUESTS
	
	/**
	 * Sends the request to obtain the job offers
	 *
	 * @return String - response body
	 */
	private String sendAvailableJobOffersRequest() {
		var getUrl = Constants.OFF_REQ;
		if(userRole.equals(Constants.ADM_ROLE)) {
			getUrl = getAvailableJobOffersURLStringParameter(getUrl, "name", searchField.getValue());
		}
		if(userRole.equals(Constants.CMP_ROLE)) {
			getUrl = getAvailableJobOffersURLStringParameter(getUrl, "companyId", username);
		}
		if(!userRole.equals(Constants.ADM_ROLE)) {
			getUrl = getAvailableJobOffersURLStringParameter(getUrl, "city", cityField.getValue());
			getUrl = getAvailableJobOffersURLStringParameter(getUrl, "area", areaSelect.getValue());
			getUrl = getAvailableJobOffersURLStringParameter(getUrl, "modality", modalitySelect.getValue());
			var minDuration = (minDurationField.getValue() == null) ? null : minDurationField.getValue().intValue();
			getUrl = getAvailableJobOffersURLIntParameter(getUrl, "minDuration", minDuration);
			var maxDuration = (maxDurationField.getValue() == null) ? null : maxDurationField.getValue().intValue();
			getUrl = getAvailableJobOffersURLIntParameter(getUrl, "maxDuration", maxDuration);
		}
		getUrl = getAvailableJobOffersURLIntParameter(getUrl, "page", numPage);
		getUrl = getAvailableJobOffersURLIntParameter(getUrl, "size", customNumElementsSelect.getValue());
		isShowingAllOffers = !getUrl.contains("?");
		var httpRequest = new HttpRequest(getUrl);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpGet(authToken);
	}
	
	private boolean isJobOfferSaved(String username, Long offerCode) {
		var httpRequest = new HttpRequest(Constants.FAV_REQ + "/favorite?username=" + username + "&offerCode=" + offerCode);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		var response = httpRequest.executeHttpGet(authToken);
		if(!response.isEmpty()) {
			return true;
		}
		return false;
	}
}
