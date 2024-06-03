package com.abb.pfg.custom;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;

import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.HttpRequest;
import com.abb.pfg.utils.UserListComponent;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.server.VaadinSession;

/**
 * Custom grid which shows a list of administrators
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
public class CustomUsersGrid extends Grid<UserListComponent>{

	private static final long serialVersionUID = -6126976873297438515L;
	//Etiquetas
	private static final String ADMIN_TAG = "Administrador";
	private static final String SHW_TAG = "Ver detalles";
	//Componentes
	
	//Atributos
	private List<UserListComponent> users;
	
	/**
	 * Default class constructor
	 * 
	 * @param numElements - number of elements that the grid will have
	 * @param contentArray - JSON array with the content
	 * @param userCategory - user's role
	 */
	public CustomUsersGrid(JSONArray contentArray, String userCategory) {
		var gridData = getGridData(contentArray, userCategory);
		this.setAllRowsVisible(true);
		this.setItems(gridData);
		if(!userCategory.equals(Constants.ADM_ROLE)) {
			this.addComponentColumn(user -> getCustomAvatar(user.getProfilePicture(), user.getName())).setHeader("");
			this.addColumn(user -> user.getName()).setHeader(Constants.NAME_TAG);
		}
		this.addColumn(user -> user.getUsername()).setHeader(Constants.USERNAME_TAG);
		var usersGrid = this.addContextMenu();
		usersGrid.setOpenOnClick(true);
		usersGrid.addItem(SHW_TAG, event -> userDetailsListener(event.getItem().get().getUsername(), userCategory));
		if(!userCategory.equals(Constants.ADM_ROLE)) {
			usersGrid.addItem(Constants.EDIT_TAG, event -> editUserDetailsListener(event.getItem().get().getUsername(), userCategory));
		}
		usersGrid.addItem(Constants.DELETE_TAG, event -> deleteUserListener(event.getItem().get(), userCategory));
	}
	
	/**
	 * Gets the users list to show
	 *
	 * @param contentArray - JSON array which contains all job offers to show
	 * @param userType - user's type to show
	 * @return List<UserListComponent> - requests list
	 */
	private List<UserListComponent> getGridData(JSONArray contentArray, String userType){
		users = new ArrayList<>();
		for(var i=0; i < contentArray.length(); i++) {
			var jsonObject = contentArray.getJSONObject(i);
			var username = jsonObject.getJSONObject("user").getString("username");
			var name = new String();
			var profilePicture = new String();
			if(userType.equals(Constants.ADM_ROLE)) {
				name = ADMIN_TAG;
				profilePicture = "";
			} else {
				name = jsonObject.getString("name");
				profilePicture = jsonObject.getString("profilePicture");
			}
			users.add(new UserListComponent(username, name, profilePicture));
		}
		return users;
	}
	
	/**
	 * Listener assigned to the show user's details option menu
	 * 
	 * @param username - user's username to display
	 * @param userCategory - user's role
	 */
	private void userDetailsListener(String username, String userCategory) {
		VaadinSession.getCurrent().setAttribute("userId", username);
		switch(userCategory) {
			case Constants.STD_ROLE:
				this.getUI().ifPresent(ui -> ui.navigate(Constants.STD_DET_PATH + "/" + userCategory));
				break;
			case Constants.CMP_ROLE:
				this.getUI().ifPresent(ui -> ui.navigate(Constants.CMP_DET_PATH + "/" + userCategory));
				break;
			default:
				this.getUI().ifPresent(ui -> ui.navigate(Constants.PROFILE_PATH_2 + "/" + userCategory));
		}
	}
	
	private void editUserDetailsListener(String username, String userCategory) {
		VaadinSession.getCurrent().setAttribute("userId", username);
		this.getUI().ifPresent(ui -> ui.navigate(Constants.PROFILE_PATH_2 + "/" + userCategory));
	}
	
	/**
	 * Listener assigned to the delete user option
	 * 
	 * @param event - user's username to delete
	 */
	private void deleteUserListener(UserListComponent user, String userCategory) {
		var username = user.getUsername();
		var deleteUrl = new String();
		switch(userCategory) {
			case Constants.STD_ROLE:
				deleteUrl = Constants.STD_REQ + "?username=" + username;
				break;
			case Constants.CMP_ROLE:
				deleteUrl = Constants.CMP_REQ + "?username=" + username;
				break;
			default:
				deleteUrl = Constants.ADMIN_REQ + "?username=" + username;
				break;
		}
		var httpRequest = new HttpRequest(deleteUrl);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		if(httpRequest.executeHttpDelete(authToken)) {
			new CustomNotification("Usuario eliminado correctamente", NotificationVariant.LUMO_SUCCESS);
			users.remove(user);
			this.getDataProvider().refreshAll();
			return;
		}
		new CustomNotification("El usuario no ha podido ser eliminado", NotificationVariant.LUMO_SUCCESS);
	}
	
	/**
	 * Creates the user's avatar
	 * 
	 * @param profilePicture - profile picture path
	 * @param name - user's name
	 * @return Avatar - custom avatar with the image resource or the name's abbreviation
	 */
	private Avatar getCustomAvatar(String profilePicture, String name) {
		if(profilePicture.isBlank()) {
			return new Avatar(name);
		}
		return new CustomAvatar(profilePicture);
	}

}
