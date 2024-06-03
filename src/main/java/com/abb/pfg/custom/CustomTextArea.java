package com.abb.pfg.custom;

import com.abb.pfg.utils.Constants;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.data.value.ValueChangeMode;

/**
 * Custom text area to show or edit users description
 * 
 * @author Adrián Barco Barona
 * @verion 1.0
 *
 */
public class CustomTextArea extends TextArea {

	private static final long serialVersionUID = -607702502483630692L;
	
	public CustomTextArea(String label, String content) {
		this.setLabel(label);
		this.setValue(content);
		this.setValueChangeMode(ValueChangeMode.EAGER);
		this.setMaxLength(Constants.DESC_LENGTH);
		this.addValueChangeListener(e -> {
			e.getSource().setHelperText(e.getValue().length() + "/" + Constants.DESC_LENGTH);
			if(e.getValue().length() == Constants.DESC_LENGTH) {
				new CustomNotification(Constants.DESC_LENGTH_ERROR, NotificationVariant.LUMO_PRIMARY);
			}
		});
		this.getElement().setAttribute("accept-charset", "UTF-8");
	}
}
