package com.abb.pfg.custom;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.abb.pfg.utils.ChatListComponent;
import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.HttpRequest;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.server.VaadinSession;
/**
 * Custom grid which shows a list of chats
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
public class CustomChatsGrid extends Grid<ChatListComponent>{
	
	private static final long serialVersionUID = 422229330582080791L;
	private static final String OPEN_CHAT_TAG = "Abrir chat";
	private static final String OPEN_CHAT_ERR = "No se ha podido abrir el chat";
	//Atributos
	private List<ChatListComponent> chats;
	/**
	 * Default class constructor
	 * 
	 * @param contentArray - JSON array with the content
	 * @param userRole - user's role
	 */
	public CustomChatsGrid(JSONArray contentArray, String userRole) {
		var gridData = getGridData(contentArray, userRole);
		this.setAllRowsVisible(true);
		this.setItems(gridData);
		switch(userRole) {
			case Constants.STD_ROLE:
				this.addComponentColumn(chat -> getCustomAvatar(chat.getCompanyName(), chat.getCompanyLogo())).setHeader("");
				this.addColumn(chat -> chat.getCompanyName()).setHeader(Constants.COMPANY_TAG).setTextAlign(ColumnTextAlign.CENTER);
				break;
			case Constants.CMP_ROLE:
				this.addComponentColumn(chat -> getCustomAvatar(chat.getStudentName(), chat.getStudentPicture())).setHeader("").setTextAlign(ColumnTextAlign.CENTER);
				this.addColumn(chat -> chat.getStudentName()).setHeader(Constants.STUDENT_TAG);
				break;
			default:
				this.addColumn(chat -> chat.getCompanyName()).setHeader(Constants.COMPANY_TAG).setTextAlign(ColumnTextAlign.CENTER);
				this.addColumn(chat -> chat.getStudentName()).setHeader(Constants.STUDENT_TAG).setTextAlign(ColumnTextAlign.CENTER);
				break;
		}
		var chatsGrid = this.addContextMenu();
		chatsGrid.setOpenOnClick(true);
		chatsGrid.addItem(OPEN_CHAT_TAG, event -> openChatListener(event.getItem().get().getChatCode()));
		chatsGrid.addItem(Constants.DELETE_TAG, event -> deleteChatListener(event.getItem().get()));
		this.addThemeVariants(GridVariant.LUMO_NO_BORDER);
	}
	
	/**
	 * Gets the final grid data from a list with all the components to show
	 * 
	 * @param contentArray - JSON array with the content to show
	 * @param userType - user role
	 * @return List - list with the content customized
	 */
	private List<ChatListComponent> getGridData(JSONArray contentArray, String userType) {
		chats = new ArrayList<>();
		for(var i=0; i<contentArray.length(); i++) {
			var jsonObject = contentArray.getJSONObject(i);
			chats.add(parseChatJSON(jsonObject, userType));
		}
		return chats;
	}
	
	/**
	 * Gets the profile picture's avatar of a user
	 * 
	 * @param name - user's name
	 * @param profilePicture - user's profile picture file name
	 * @return Avatar - user's avatar
	 */
	private Avatar getCustomAvatar(String name, String profilePicture) {
		if(profilePicture.isBlank()) {
			return new Avatar(name);
		}
		return new CustomAvatar(profilePicture);
	}
	
	//LISTENERS 
	
	/**
	 * Listener assigned to the open chat option
	 * 
	 * @param chatCode - chat's code to display
	 */
	private void openChatListener(Long chatCode) {
		if(chatCode != null) {
			this.getUI().ifPresent(ui -> ui.navigate(Constants.CHAT_BOX_PATH + "/" + chatCode));
			return;
		}
		new CustomNotification(OPEN_CHAT_ERR, NotificationVariant.LUMO_ERROR);
	}
	
	/**
	 * Listener assigned to the delete chat option
	 * 
	 * @param chatListComponent - item to delete
	 */
	private void deleteChatListener(ChatListComponent chatListComponent) {
		if(sendDeleteChatRequest(chatListComponent.getChatCode())) {
			chats.remove(chatListComponent);
			this.getDataProvider().refreshAll();
			new CustomNotification(Constants.DEL_MSG, NotificationVariant.LUMO_SUCCESS);
			return;
		}
		new CustomNotification(Constants.DEL_ERR, NotificationVariant.LUMO_ERROR);
	}
	
	//PARSEOS JSON
	
	/**
	 * Parses the JSON to get all chats info
	 * 
	 * @param jsonObject - JSON object to analyze
	 * @param userRole - user's role
	 * @return ChatListComponent - grid item
	 */
	private ChatListComponent parseChatJSON(JSONObject jsonObject, String userRole) {
		var chatCode = jsonObject.getLong("id");
		var chatStudent = jsonObject.getJSONObject("student").getString("name");
		var chatStudentPic = jsonObject.getJSONObject("student").getString("profilePicture");
		var chatCompany = jsonObject.getJSONObject("company").getString("name");
		var chatCompanyPic = jsonObject.getJSONObject("company").getString("profilePicture");
		return new ChatListComponent(chatCode, chatStudent, chatStudentPic, chatCompany, chatCompanyPic);
	}
	
	//HTTP REQUESTS
	
	/**
	 * Listener assigned to the delete chat option
	 * 
	 * @param chatCode - chat's id to delete
	 * @return boolean - true if it has been delted, false if not
	 */
	private boolean sendDeleteChatRequest(Long chatCode) {
		if(chatCode == null) {
			return false;
		}
		var httpRequest = new HttpRequest(Constants.CHATS_REQ + "?chatCode=" + chatCode);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpDelete(authToken);
	}
}
