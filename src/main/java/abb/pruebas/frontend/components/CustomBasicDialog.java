package abb.pruebas.frontend.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;

/**
 * Class which represents a custom dialog to report an error
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *	
 */
public class CustomBasicDialog extends Dialog{
	
	private String title;
	private String text;
	private Button goBackButton;
	
	public CustomBasicDialog(String title, String text) {
		this.setHeaderTitle(title);
		this.add(text);
		goBackButton = new Button("Volver", goBackEvent -> this.close());
		this.getFooter().add(goBackButton);
	}
}
