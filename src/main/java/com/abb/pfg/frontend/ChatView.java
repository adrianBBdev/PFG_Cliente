package com.abb.pfg.frontend;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import com.abb.pfg.frontend.commons.Constants;
import com.abb.pfg.frontend.components.MainLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.messages.MessageInput;
import com.vaadin.flow.component.messages.MessageList;
import com.vaadin.flow.component.messages.MessageListItem;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Class which represents the chat view
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route(Constants.CHAT_PATH)
@PageTitle("J4S - Chat")
public class ChatView extends MainLayout {
	
	private static final String HEADER_TITLE = "Chat";
	private static final String MESSAGE_ERROR = "No se puede enviar un mensaje vacío";
	
	private VerticalLayout mainLayout, contentLayout,messagesLayout;
	private MessageInput messageInput;
	private MessageList messageList;
	private List<MessageListItem> messages;

	public ChatView() {
		init();
		contentLayout.add(new H1(HEADER_TITLE), mainLayout);
		setContent(contentLayout);
		
	}
	
	private void init() {
		contentLayout = new VerticalLayout();
		contentLayout.setAlignItems(FlexComponent.Alignment.CENTER);
		setMessageList();
		messageInput = new MessageInput();
		messageInput.setWidth(Constants.WIDTH);
		messageInput.addSubmitListener(submitEvent -> {
			messagesLayout.remove(messageList);
			messageList = new MessageList();
			MessageListItem message = this.createMessageItem(submitEvent.getValue(), "Adrian Barco");
			messages.add(message);
			messageList.setItems(messages);
			messagesLayout.add(messageList);
		});	
		mainLayout = new VerticalLayout();
		mainLayout.add(messagesLayout, messageInput);
		mainLayout.setAlignItems(FlexComponent.Alignment.CENTER);
	}
	
	private MessageListItem createMessageItem(String message, String sender) {
		Instant instant = LocalDateTime.now().minusDays(0)
				.toInstant(ZoneOffset.UTC);
		MessageListItem messageItem = new MessageListItem(message,
				instant, sender);
		messageItem.setUserColorIndex(1);
		return messageItem;	
	}
	
	private void setMessageList() {
		messageList = new MessageList();
		messages = new ArrayList<>();
		Instant instant = LocalDateTime.now().minusDays(0)
				.toInstant(ZoneOffset.UTC);
		MessageListItem messageItem = new MessageListItem("Hola, ¿qué tal?", instant, "Cristian Molina");
		messageItem.setUserColorIndex(1);
		messages.add(messageItem);
		messageList.setItems(messages);
		messagesLayout = new VerticalLayout();
		messagesLayout.add(messageList);
	}

}
