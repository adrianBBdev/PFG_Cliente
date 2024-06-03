package com.abb.pfg.views;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.abb.pfg.custom.CustomAppLayout;
import com.abb.pfg.custom.CustomAvatar;
import com.abb.pfg.custom.CustomNotification;
import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.HttpRequest;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageInput.SubmitEvent;
import com.vaadin.flow.component.messages.MessageInputI18n;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

/**
 * Shows a chat between a student and a company
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route(Constants.CHAT_BOX_PATH)
@PageTitle("J4S - Chat")
public class ChatBoxView extends CustomAppLayout implements HasUrlParameter<Long>, BeforeEnterObserver{

	private static final long serialVersionUID = -3209432078545293474L;
	private static String HEADER_TAG = "Chat";
	private VerticalLayout mainLayout;
	private MessageList messageList;
	private MessageInput messageInput;
	private Button prevMessagesButton;
	private String username, userRole, chatBody;
	private Long chatId;
	private Integer numPage = 0;
	private LinkedHashSet<MessageListItem> items;
	
	@Override
	public void setParameter(BeforeEvent event, Long parameter) {
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		userRole = (String) VaadinSession.getCurrent().getAttribute("role");
		if(authToken == null || userRole.equals(Constants.GST_ROLE)){
			event.forwardTo(LoginView.class);
			return;
		}
		chatId = parameter;
		chatBody = sendChatDetailsRequest(chatId);
		if(chatBody == null) {
			event.forwardTo(AvailableOffersView.class);
			return;
		}
	}
	
	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		username = (String) VaadinSession.getCurrent().getAttribute("username");
		if(!userRole.equals(Constants.ADM_ROLE)) {
			if(!verifyAccessPermission(username, userRole, chatBody)) {
				new CustomNotification(Constants.ACC_REJ_MSG, NotificationVariant.LUMO_ERROR);
				event.forwardTo(ChatsView.class);
				return;
			}
		}
		init();
	}
	
	/**
	 * Sends the http request to obtain the chat details
	 * 
	 * @param chatId - chat code to identify the chat
	 * @return String - chat JSON body
	 */
	private String sendChatDetailsRequest(Long chatId) {
		var getUrl = Constants.CHATS_REQ + "/chat?chatCode=" + chatId;
		var httpRequest = new HttpRequest(getUrl);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpGet(authToken);
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
		var baseVerticalLayout = new VerticalLayout();
		baseVerticalLayout.add(new H1(HEADER_TAG), prevMessagesButton, mainLayout);
		baseVerticalLayout.setAlignItems(Alignment.CENTER);
		this.setContent(baseVerticalLayout);
	}
	
	/**
	 * Sets up the content of the view's main layout
	 * 
	 */
	private void setContentLayout() {
		setMessagesList();
		if(!userRole.equals(Constants.ADM_ROLE)) {
			messageInput = new MessageInput();
			messageInput.setI18n(new MessageInputI18n().setMessage("Escribe un mensaje").setSend("Enviar"));
			messageInput.setWidth("70%");
			messageInput.addSubmitListener(event -> addNewMessageListener(event));
			mainLayout.add(messageList, messageInput);
		} else {
			mainLayout.add(messageList);
		}
	}
	
	/**
	 * Sets up the messages list of the view
	 * 
	 */
	private void setMessagesList() {
		messageList = new MessageList();
		messageList.setWidth("70%");
		var messagesJSONArray = sendGetChatMessagesRequest(chatId, numPage);
		var jsonObject = new JSONObject(messagesJSONArray);
		var isShowingLast = jsonObject.getBoolean("last");
		setPreviousMessagesButton(isShowingLast);
		var content = jsonObject.getJSONArray("content");
		items = new LinkedHashSet<>();
		setMessagesListContent(content, items);
		messageList.setItems(items);
	}
	
	/**
	 * Sets up the content of the messages list of the view
	 * 
	 * @param content - list of messages to display
	 * @param items - serialized messages list
	 */
	private void setMessagesListContent(JSONArray content, LinkedHashSet<MessageListItem> items) {
		for(int i=content.length()-1; i>=0 ;i--) {
			var messageObject = content.getJSONObject(i);
			var messageContent = messageObject.getString("content");
			var messageDateTime = messageObject.getString("timeStamp");
			var userName = new String();
			var profilePic = new String();
			if(messageObject.getString("senderType").equals(Constants.STD_ROLE)) {
				userName = messageObject.getJSONObject("chat").getJSONObject("student").getString("name");
				profilePic = messageObject.getJSONObject("chat").getJSONObject("student").getString("profilePicture");
			} else {
				userName = messageObject.getJSONObject("chat").getJSONObject("company").getString("name");
				profilePic = messageObject.getJSONObject("chat").getJSONObject("company").getString("profilePicture");
			}
			items.add(createMessageItem(messageContent, convertTimeStampToInstant(messageDateTime), 
					userName, new CustomAvatar(profilePic)));
		}
	}
	
	/**
	 * Listener assigned to the send message button
	 * 
	 * @param event - event that occurrs when the user click the send button
	 */
	private void addNewMessageListener(SubmitEvent event) {
		if(event.getValue().length() > 250) {
			new CustomNotification("No se pueden enviar mensajes de más de 250 caracteres", NotificationVariant.LUMO_WARNING);
			return;
		}
		var jsonObject = new JSONObject(chatBody);
		var userName = (userRole.equals(Constants.STD_ROLE)) ? jsonObject.getJSONObject("student").getString("name") 
				: jsonObject.getJSONObject("company").getString("name");
		var requestBody = setMessageJSONBody(event.getValue(), userRole, messageList.getItems().size(), chatBody);
		if(sendCreateMessageRequest(requestBody)) {
			var jsonChatObject = new JSONObject(chatBody);
			var profilePicture = (userRole.equals(Constants.STD_ROLE) 
					? jsonChatObject.getJSONObject("student").getString("profilePicture")
					: jsonChatObject.getJSONObject("company").getString("profilePicture"));
			var customAvatar = getCustomAvatar(userName, profilePicture);
			LinkedHashSet<MessageListItem> items = new LinkedHashSet<>(messageList.getItems());
			items.add(createMessageItem(event.getValue(), Instant.now(), userName, customAvatar));
			messageList.setItems(items);
			return;
		}
		new CustomNotification("No se ha podido enviar el mensaje", NotificationVariant.LUMO_ERROR);
	}
	
	/**
	 * Creates a new message list item
	 * 
	 * @param value - message content
	 * @param instant - message's time stamp
	 * @param name - message's sender
	 * @param customAvatar - message's sender avatar
	 * @return MessageListItem - the new message list item
	 */
	private MessageListItem createMessageItem(String value, Instant instant, String name, Avatar customAvatar) {
		var messageListItem = new MessageListItem(value, Instant.now(), name);
		messageListItem.setUserImageResource(customAvatar.getImageResource());
		return messageListItem;
	}
	
	/**
	 * Sets up the button which allow users to display previous messages
	 * 
	 * @param isShowingLast
	 */
	private void setPreviousMessagesButton(boolean isShowingLast) {
		prevMessagesButton = new Button("Mensajes anteriores", new Icon(VaadinIcon.ANGLE_DOUBLE_UP));
		prevMessagesButton.addClickListener(event -> previousMessagesButtonListener());
		if(isShowingLast) {
			prevMessagesButton.setEnabled(!isShowingLast);
		}
	}
	
	/**
	 * Listener assigned to the previous messages button, to display the previous messages
	 * 
	 */
	private void previousMessagesButtonListener() {
		numPage++;
		var messagesJSONArray = sendGetChatMessagesRequest(chatId, numPage);
		var jsonObject = new JSONObject(messagesJSONArray);
		var isShowingLast = jsonObject.getBoolean("last");
		if(isShowingLast) {
			prevMessagesButton.setEnabled(!isShowingLast);
		}
		var content = jsonObject.getJSONArray("content");
		var previousItems = new LinkedHashSet<MessageListItem>();
		setMessagesListContent(content, previousItems);
		previousItems.addAll(items);
		items = previousItems;
		messageList.setItems(items);
	}
	
	/**
	 * Verifies if the user that want to access to this route is allowed to access or not
	 * 
	 * @param username - user's username
	 * @param userRole - user's role
	 * @param chatBody - chat to display
	 * @return boolean - true if the user has permission, false if not
	 */
	private boolean verifyAccessPermission(String username, String userRole, String chatBody) {
		var usernameToVerify = new String();
		try {
			var jsonObject = new JSONObject(chatBody);
			if(userRole.equals(Constants.STD_ROLE)) {
				usernameToVerify = jsonObject.getJSONObject("student").getJSONObject("user").getString("username");
			} else {
				usernameToVerify = jsonObject.getJSONObject("company").getJSONObject("user").getString("username");
			}
		} catch (JSONException e) {
			System.err.println("Error al parsear el objeto JSON: " + e.getMessage());
			return false;
		}
		if(username.equals(usernameToVerify)) {
			return true;
		}
		return false;
	}
	
	/**
	 * Gets an Avatar object with the user's profile picture
	 * 
	 * @param name - user's name
	 * @param profilePicture - user's profile picture
	 * @return Avatar - avatar with the user's profile picture
	 */
	private Avatar getCustomAvatar(String name, String profilePicture) {
		if(profilePicture.isBlank()) {
			return new Avatar(name);
		}
		return new CustomAvatar(profilePicture);
	}
	
	/**
	 * Converts the time stamp to Instant object
	 * 
	 * @param timeStamp - time stamp to convert
	 * @return Instant - time stamp converted
	 */
	private Instant convertTimeStampToInstant(String timeStamp) {
		var formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
		return Instant.from(formatter.parse(timeStamp));
	}
	
	//PARSEOS JSON
	
	private String setMessageJSONBody(String content, String senderType, int order,String chatBody) {
		try {
			var messageJSONObject = new JSONObject();
			messageJSONObject.put("content", content);
			messageJSONObject.put("senderType", senderType);
			messageJSONObject.put("order", order);
			messageJSONObject.put("chat", new JSONObject(chatBody));
			return messageJSONObject.toString();
		} catch (JSONException e) {
			System.err.println("Error al parsear el objeto JSON: " + e.getMessage());
			return null;
		}
	}
	
	//HTTP REQUESTS
	
	/**
	 * Sends the http request to get the messages of the chat
	 * 
	 * @param chatId - chat code to identify the chat
	 * @param numPage - page number to display
	 * @return String - response body
	 */
	private String sendGetChatMessagesRequest(Long chatId, Integer numPage) {
		var getUrl = Constants.MSG_REQ + "?chatCode=" + chatId;
		getUrl = (numPage != null) ? getUrl + "&page=" + numPage : getUrl;
		var httpRequest = new HttpRequest(getUrl);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpGet(authToken);
	}
	
	/**
	 * Sends the http request to create a new message
	 * 
	 * @param requestBody - the message info needed to create the new message
	 * @return boolean - true if it has been created, false if not
	 */
	private boolean sendCreateMessageRequest(String requestBody) {
		if(requestBody == null) {
			return false;
		}
		var httpRequest = new HttpRequest(Constants.MSG_REQ);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpPost(authToken, requestBody);
	}
}
