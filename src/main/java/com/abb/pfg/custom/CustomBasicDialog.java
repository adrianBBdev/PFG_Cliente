package com.abb.pfg.custom;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Class which represents a custom dialog to report an error
 *
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class CustomBasicDialog extends Dialog{

	private static final long serialVersionUID = 1L;
	private String title;
	private String text;
	private Button goBackButton;

	/**
	 * Default class constructor
	 * 
	 * @param title - title the dialog will has
	 * @param text - text the dialog will has
	 */
	public CustomBasicDialog(String title, String text) {
		this.setHeaderTitle(title);
		this.add(text);
		goBackButton = new Button("Volver", goBackEvent -> this.close());
		this.getFooter().add(goBackButton);
	}
}
