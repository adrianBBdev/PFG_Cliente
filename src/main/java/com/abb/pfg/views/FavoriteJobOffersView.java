package com.abb.pfg.views;

import org.json.JSONObject;

import com.abb.pfg.custom.CustomAppLayout;
import com.abb.pfg.custom.CustomJobOffersGrid;
import com.abb.pfg.custom.CustomNavigationOptionsPageLayout;
import com.abb.pfg.custom.CustomNumElementsSelect;
import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.HttpRequest;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import lombok.NoArgsConstructor;

/**
 * Performs the view where the students may show their favorites job offers, which has been marked previously as favorite
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route(Constants.SV_OFFERS_PATH)
@PageTitle("J4S - Ofertas guardadas")
@NoArgsConstructor
public class FavoriteJobOffersView extends CustomAppLayout implements BeforeEnterObserver {

	private static final long serialVersionUID = 3323452156025021583L;
	//Etiquetas
	private static final String HEADER_TAG = "Ofertas guardadas";
	//Componentes
	private VerticalLayout mainLayout, contentLayout;
	private CustomJobOffersGrid jobOffersGrid;
	private CustomNavigationOptionsPageLayout navigationOptionsPageLayout;
	private CustomNumElementsSelect favoriteDisplaySelect;
	//Atributos
	private String userRole, username;
	private Integer numPage;
	
	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		userRole = (String) VaadinSession.getCurrent().getAttribute("role");
		if(authToken == null || !userRole.equals(Constants.STD_ROLE)){
			event.forwardTo(LoginView.class);
			return;
		}
		username = (String) VaadinSession.getCurrent().getAttribute("username");
		init();		//Inicializamos la vista y añadimos el layout principal
	}
	
	/**
	 * Initializes all view components
	 * 
	 */
	private void init() {
		mainLayout = new VerticalLayout();		//Inicializamos el layout principal
		mainLayout.setAlignItems(Alignment.CENTER);
		mainLayout.setWidthFull();
		numPage = 0;
		favoriteDisplaySelect = new CustomNumElementsSelect();
		favoriteDisplaySelect.addValueChangeListener(event -> setContentLayout());
		setContentLayout();
		var baseVerticalLayout = new VerticalLayout();
		baseVerticalLayout.add(new H1(HEADER_TAG), mainLayout, favoriteDisplaySelect ,navigationOptionsPageLayout);
		baseVerticalLayout.setAlignItems(Alignment.CENTER);
		this.setContent(baseVerticalLayout);
	}
	
	/**
	 * Gets all components that will be added to the main layout;
	 * 
	 */
	private void setContentLayout() {
		mainLayout.removeAll();
		contentLayout = new VerticalLayout();
		contentLayout.setMaxWidth("1300px");
		contentLayout.setWidthFull();
		var favoriteJobOffersBody = sendGetFavoriteJobOffers(username);
		if(favoriteJobOffersBody != null) {
			var jsonObject = new JSONObject(favoriteJobOffersBody);
			var isEmpty = jsonObject.getBoolean("empty");
			var isShowingFirst = jsonObject.getBoolean("first");
			var isShowingLast = jsonObject.getBoolean("last");
			setNavigationOptionsPageLayout(isShowingFirst, isShowingLast);
			if(!isEmpty) {
				var contentArray = jsonObject.getJSONArray("content");
				if(userRole.equals(Constants.STD_ROLE)) {
					jobOffersGrid = new CustomJobOffersGrid(contentArray, userRole, true);
					contentLayout.add(jobOffersGrid);
					mainLayout.add(contentLayout);
				}
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
		setContentLayout();
	}

	/**
	 * Listener assigned to the previous page button
	 */
	private void prevPageListener() {
		numPage--;
		setContentLayout();
	}
	
	/**
	 * Sends the http get request to obtain all the favorite job offers of the user
	 * 
	 * @param username - user's username
	 * @return String - response body
	 */
	private String sendGetFavoriteJobOffers(String username) {
		var httpRequest = new HttpRequest(Constants.FAV_REQ + "?username=" + username + "&page=" + numPage);
		var numElements = favoriteDisplaySelect.getValue();
		if(numElements != null) {
			httpRequest.setUrl(httpRequest.getUrl() + "&size=" + numElements);
		}
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpGet(authToken);
	}
}
