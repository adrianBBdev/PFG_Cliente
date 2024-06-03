/**
 * 
 */
package com.abb.pfg.custom;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.HttpRequest;
import com.vaadin.flow.component.select.Select;
import com.vaadin.flow.server.VaadinSession;

/**
 * Custom select where user's are able to select one single area
 *
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
public class CustomSelectAreas extends Select<String> {

	private static final long serialVersionUID = -2386873322472337088L;
	
	/**
	 * Default class constructor
	 * 
	 */
	public CustomSelectAreas() {
		this.setEmptySelectionAllowed(true);
		this.setLabel(Constants.AREA_TAG);
		getAreaSelectionContent();
	}
	
	/**
	 * Builds the custom area selection component from the app data
	 *
	 */
	private void getAreaSelectionContent() {
		var responseBody = sendAreaSelectContentRequest();
		var contentJSON = new JSONObject(responseBody);
		var isEmpty = (boolean) contentJSON.get("empty");
		if(!isEmpty) {
			var contentArray = contentJSON.getJSONArray("content");
			var numElements = (int) contentJSON.get("totalElements");
			List<String> areaItems = new ArrayList<>();
			for(var i=0; i<numElements; i++) {
				areaItems.add(contentArray.getJSONObject(i).getString("name"));
			}
			this.setItems(areaItems);
		}
	}
	
	/**
	 * Sends the http request to obtain the areas from the app data
	 *
	 * @return String - response body or null
	 */
	private String sendAreaSelectContentRequest() {
		var getUrl = Constants.AREAS_REQ;
		var httpRequest = new HttpRequest(getUrl);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpGet(authToken);
	}
	
	/**
	 * Refresh the component content
	 * 
	 */
	public void refreshCustomSelectAreas() {
		this.removeAll();
		getAreaSelectionContent();
	}

}
