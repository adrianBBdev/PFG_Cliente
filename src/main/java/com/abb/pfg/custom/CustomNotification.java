package com.abb.pfg.custom;

import com.abb.pfg.utils.Constants;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;

/**
 * Custom notificaton to show warnings, errors or success events to users
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
public class CustomNotification extends Notification {

	private static final long serialVersionUID = -8062752084356516530L;
	
	/**
	 * Default class constructor
	 * 
	 * @param text
	 * @param notificationVariant
	 */
	public CustomNotification(String text, NotificationVariant notificationVariant) {
		this.setText(text);
		this.setPosition(Position.TOP_CENTER);
		this.setDuration(Constants.NOTIF_DURATION);
		this.addThemeVariants(notificationVariant);
		this.open();
	}
}
