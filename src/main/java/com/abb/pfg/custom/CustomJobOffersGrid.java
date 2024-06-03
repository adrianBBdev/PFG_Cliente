package com.abb.pfg.custom;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.HttpRequest;
import com.abb.pfg.utils.JobOfferListComponent;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.server.VaadinSession;

/**
 * Custom grid to show a list of job offers, depending on the user's role
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
public class CustomJobOffersGrid extends Grid<JobOfferListComponent> {
	
	private static final long serialVersionUID = 7382412712097701289L;
	//Etiquetas
	private static final String SHW_TAG = "Ver detalles";
	private static final String NUM_PETICIONES_TAG = "Nº de solicitudes";
	private static final String GET_OFF_ERR = "Se ha producido un error. No se pueden mostrar los detalles de esta oferta";
	private static final String GET_REQ_ERR = "Se ha producido un error. No se pueden mostrar las solicitudes relativas a esta oferta";
	private static final String DEAC_WRN = "La oferta fue actualizada correctamente";
	private static final String DEAC_ERR = "No se ha podido desactivar la oferta";
	//Atributos
	private List<JobOfferListComponent> jobOffers;
	
	/**
	 * Default class constructor
	 * 
	 * @param contentArray - JSON array with the content
	 * @param userRole - user's role
	 * @param isShowingFavorites - true if it has to show favorite job offers, false if not
	 */
	public CustomJobOffersGrid(JSONArray contentArray, String userRole, boolean isShowingFavorites) {
		var gridData = getGridData(contentArray, userRole, isShowingFavorites);
		this.setAllRowsVisible(true);
		this.setItems(gridData);
		switch(userRole) {
			case Constants.STD_ROLE:
				setStudentRoleGrid(isShowingFavorites);
				break;
			case Constants.CMP_ROLE:
				setCompanyRoleGrid();
				break;
			default:	//ADMIN
				setAdminRoleGrid();
		}
	}

	/**
	 * Gets the final grid data from a list with all the components to show
	 * 
	 * @param contentArray - JSON array with the content to show
	 * @param userRole - user's role
	 * @param isShowingFavorites - true if it has to show favorite job offers, false if not
	 * @return List - list with the content customized
	 */
	private List<JobOfferListComponent> getGridData(JSONArray contentArray, String userRole, boolean isShowingFavorites) {
		jobOffers = new ArrayList<>();
		if(isShowingFavorites) {
			for(var i=0; i<contentArray.length(); i++) {
				var jsonObject = contentArray.getJSONObject(i);
				var favoriteCode = jsonObject.getLong("id");
				var finalJsonObject = jsonObject.getJSONObject("jobOffer");
				jobOffers.add(parseJobOfferJSON(favoriteCode, finalJsonObject, userRole));
			}
		} else {
			for(var i=0; i<contentArray.length(); i++) {
				var jsonObject = contentArray.getJSONObject(i);
				jobOffers.add(parseJobOfferJSON(null, jsonObject, userRole));
			}
		}
		return jobOffers;
	}
	
	/**
	 * Sets up the job offer's grid for students
	 * 
	 * @param isShowingFavorites - true if is is displaying their favorites job offers, fals if not
	 */
	private void setStudentRoleGrid(boolean isShowingFavorites) {
		this.addColumn(jobOffer -> jobOffer.getTitle()).setHeader(Constants.NAME_TAG);
		this.addColumn(jobOffer -> jobOffer.getArea()).setHeader(Constants.AREA_TAG);
		this.addColumn(jobOffer -> jobOffer.getCity()).setHeader(Constants.CITY_TAG);
		var jobOffersGrid = this.addContextMenu();
		jobOffersGrid.setOpenOnClick(true);
		jobOffersGrid.addItem(SHW_TAG, event -> jobOfferDetailsListener(event.getItem().get().getOfferCode()));
		jobOffersGrid.addItem("Ver " + Constants.COMPANY_TAG, event -> showCompanyInfoDetailsListener(event.getItem().get().getOfferCode()));
		if(isShowingFavorites) {
			jobOffersGrid.addItem(Constants.REM_TAG, event -> jobOfferDeleteListener(event.getItem().get()));
		}
	}
	
	/**
	 * Sets up the job offer's grid for companys
	 * 
	 */
	private void setCompanyRoleGrid() {
		this.addColumn(jobOffer -> jobOffer.getTitle()).setHeader(Constants.NAME_TAG);
		this.addColumn(jobOffer -> jobOffer.getArea()).setHeader(Constants.AREA_TAG);
		this.addColumn(jobOffer -> jobOffer.getCity()).setHeader(Constants.CITY_TAG);
		this.addColumn(jobOffer -> jobOffer.getStatus()).setHeader(Constants.STATUS_TAG);
		this.addColumn(jobOffer -> jobOffer.getNumRequests()).setHeader(NUM_PETICIONES_TAG);
		var jobOffersGrid = this.addContextMenu();
		jobOffersGrid.setOpenOnClick(true);
		jobOffersGrid.addItem(SHW_TAG, event -> jobOfferDetailsListener(event.getItem().get().getOfferCode()));
		jobOffersGrid.addItem(Constants.SH_REQ_TAG , event -> jobOfferRequestsListener(event.getItem().get().getOfferCode()));
		jobOffersGrid.addItem(Constants.DEAC_TAG, event -> jobOfferActivateOrDeactivateListener(event.getItem().get()));
		jobOffersGrid.addItem(Constants.DELETE_TAG, event -> jobOfferDeleteListener(event.getItem().get()));
	}
	
	/**
	 * Sets up the job offer's grid fot admins
	 * 
	 */
	private void setAdminRoleGrid() {
		this.addColumn(jobOffer -> jobOffer.getCompany()).setHeader(Constants.COMPANY_TAG);
		this.addColumn(jobOffer -> jobOffer.getTitle()).setHeader(Constants.NAME_TAG);
		this.addColumn(jobOffer -> jobOffer.getArea()).setHeader(Constants.AREA_TAG);
		this.addColumn(jobOffer -> jobOffer.getCity()).setHeader(Constants.CITY_TAG);
		this.addColumn(jobOffer -> jobOffer.getStatus()).setHeader(Constants.STATUS_TAG);
		var jobOffersGrid = this.addContextMenu();
		jobOffersGrid.setOpenOnClick(true);
		jobOffersGrid.addItem(SHW_TAG, event -> jobOfferDetailsListener(event.getItem().get().getOfferCode()));
		jobOffersGrid.addItem(Constants.DEAC_TAG, event -> jobOfferActivateOrDeactivateListener(event.getItem().get()));
		jobOffersGrid.addItem(Constants.DELETE_TAG, event -> jobOfferDeleteListener(event.getItem().get()));
	}
	
	//LISTENERS
	
	/**
	 * Listener assigned to the view details option on the grid
	 * 
	 * @param jobOfferId - job offer id requested
	 */
	private void jobOfferDetailsListener(Long jobOfferId) {
		if(jobOfferId != null) {
			this.getUI().ifPresent(ui -> ui.navigate(Constants.OFFER_PATH + "/" + jobOfferId));
			return;
		}
		new CustomNotification(GET_OFF_ERR, NotificationVariant.LUMO_PRIMARY);
	}
	
	/**
	 * Listener assigned to the view requests option about this selected job offer
	 * 
	 * @param offerCode - job offer id requested
	 */
	private void jobOfferRequestsListener(Long offerCode) {
		if(offerCode != null) {
			this.getUI().ifPresent(ui -> ui.navigate(Constants.REQ_PATH + "/" + offerCode));
			return;
		}
		new CustomNotification(GET_REQ_ERR, NotificationVariant.LUMO_PRIMARY);
	}
	
	/**
	 * Listener assigned to the show company grid context option
	 * 
	 * @param offerCode - offer code of the job offer selected
	 */
	private void showCompanyInfoDetailsListener(Long offerCode) {
		var httpRequest = new HttpRequest(Constants.OFF_REQ + "/jobOffer?offerCode=" + offerCode);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		var responseBody = httpRequest.executeHttpGet(authToken);
		if(responseBody != null) {
			var jsonObject = new JSONObject(responseBody);
			var userId = jsonObject.getJSONObject("company").getJSONObject("user").getString("username");
			VaadinSession.getCurrent().setAttribute("userId", userId);
			this.getUI().ifPresent(ui -> ui.navigate(Constants.CMP_DET_PATH));
			return;
		}
		new CustomNotification(Constants.CMP_ERR, NotificationVariant.LUMO_ERROR);
	}
	
	/**
	 * Listener assigned to the activate/deactivate option about the selected offer
	 * 
	 * @param event - event performed when user clicks on the selected option
	 */
	private void jobOfferActivateOrDeactivateListener(JobOfferListComponent item) {
		var httpRequest = new HttpRequest(Constants.OFF_REQ + "/jobOffer?offerCode=" + item.getOfferCode());
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		var jobOfferBody = httpRequest.executeHttpGet(authToken);
		if(jobOfferBody == null) {
			new CustomNotification(DEAC_ERR, NotificationVariant.LUMO_PRIMARY);
			return;
		}
		var jsonObject = new JSONObject(jobOfferBody);
		var status = jsonObject.getBoolean("status");
		jsonObject.put("state", !status);
		var jobOfferUpdated = jsonObject.toString();
		var httpPut = new HttpRequest(Constants.OFF_REQ);
		var isUpdated = httpPut.executeHttpPut(authToken, jobOfferUpdated);
		if(isUpdated) {
			var jobOffer = item;
			var finalStatus = (status) ? "No activa" : "Activa";
			jobOffer.setStatus(finalStatus);
			this.getDataProvider().refreshItem(jobOffer);
			this.getDataProvider().refreshAll();
			new CustomNotification(DEAC_WRN, NotificationVariant.LUMO_PRIMARY);
			return;
		}
		new CustomNotification(DEAC_ERR, NotificationVariant.LUMO_PRIMARY);
		return;
	}
	
	/**
	 * Listener assigned to the remove favorite job offer option on the grid
	 * 
	 * @param jobOfferListComponent - favorite job offer id requested
	 */
	private void jobOfferDeleteListener(JobOfferListComponent jobOfferListComponent) {
		var httpRequest = new HttpRequest();
		if(jobOfferListComponent.getFavoriteCode() == null) {
			httpRequest.setUrl(Constants.OFF_REQ + "?offerCode=" + jobOfferListComponent.getOfferCode());
		} else {
			httpRequest.setUrl(Constants.FAV_REQ + "?favoriteCode=" + jobOfferListComponent.getFavoriteCode());
		}
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		if(httpRequest.executeHttpDelete(authToken)) {
			jobOffers.remove(jobOfferListComponent);
			this.getDataProvider().refreshAll();
			new CustomNotification(Constants.DEL_MSG, NotificationVariant.LUMO_PRIMARY);
			return;
		}
		new CustomNotification(Constants.DEL_ERR, NotificationVariant.LUMO_PRIMARY);
	}
	
	//PARSEOS JSON
	
	/**
	 * Parses the JSON to get all the job offer info
	 * 
	 * @param favoriteCode - favorite code if the grid is displaying the favorites job offers, null if not
	 * @param jsonObject - json object to analyze
	 * @param userRole - user's role
	 * @return JobOfferListComponent - grid's item
	 */
	private JobOfferListComponent parseJobOfferJSON(Long favoriteCode, JSONObject jsonObject, String userRole) {
		var jobOfferCode = jsonObject.getLong("id");
		var jobOfferTitle = jsonObject.getString("title");
		var jobOfferCompany = jsonObject.getJSONObject("company").getString("name");
		var jobOfferCity = jsonObject.getString("city");
		var jobOfferArea = jsonObject.getJSONObject("area").getString("name");
		var jobOfferStatus = jsonObject.getBoolean("status");
		String status = ((jobOfferStatus) ? "Activa" : "No activa");
		var numRequests = (userRole.equals(Constants.CMP_ROLE)) ? getRequestsNum(jobOfferCode) : null;
		return new JobOfferListComponent(jobOfferCode, favoriteCode,jobOfferTitle, jobOfferCompany,
				jobOfferArea, jobOfferCity, status, numRequests);
	}
	
	//HTTP REQUESTS
	
	/**
	 * Sends the http request to obtain the number of elements
	 * 
	 * @param offerCode - requested offer code
	 * @return Integer - number of elements
	 */
	private Integer getRequestsNum(Long offerCode) {
		var getUrl = Constants.REQ_REQ + "?offerCode=" + offerCode;
		var httpRequest = new HttpRequest(getUrl);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken"); 
		return getTotalElements(httpRequest.executeHttpGet(authToken));
	}
	
	/**
	 * Gets the total elements that the response body contains.
	 * 
	 * @param responseBody - response body of the http request
	 * @return Integer - number of elements
	 */
	private Integer getTotalElements(String responseBody) {
		if(responseBody != null) {
			return new JSONObject(responseBody).getInt("totalElements");
		}
		return null;
	}
}
