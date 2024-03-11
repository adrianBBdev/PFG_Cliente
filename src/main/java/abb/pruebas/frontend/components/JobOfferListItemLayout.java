/**
 * 
 */
package abb.pruebas.frontend.components;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;

import abb.pruebas.frontend.commons.Constants;

/**
 * Class which represents the layout of a list item in the job offers list
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
public class JobOfferListItemLayout extends VerticalLayout{
	
	private static final String TITLE_TAG = "Título de la oferta";
	private static final String NAME_TAG = "Nombre de la empresa";
	private static final String DETAILS_TAG = "Ver detalles";
	private static final String SMALL_WIDTH = "500px";
	
	private HorizontalLayout infoLayout;
	private VerticalLayout textLayout;
	private Image companyImage;
	private TextField titleField, nameField;
	private Button detailsButton;
	
	public JobOfferListItemLayout() {
		this.init();
		this.add(infoLayout, detailsButton);
		this.setAlignItems(FlexComponent.Alignment.CENTER);
	}
	
	private void init() {
		this.setInfoLayout();
		this.setDetailsButton();
	}
	
	private void setInfoLayout() {
		titleField = new TextField(TITLE_TAG);
		titleField.setWidth(SMALL_WIDTH);
		titleField.setReadOnly(true);
		nameField = new TextField(NAME_TAG);
		nameField.setWidth(SMALL_WIDTH);
		nameField.setReadOnly(true);
		textLayout = new VerticalLayout();
		textLayout.add(titleField, nameField);
		companyImage = new Image();
		infoLayout = new HorizontalLayout();
		infoLayout.add(companyImage, textLayout);
	}
	
	private void setDetailsButton() {
		detailsButton = new Button(DETAILS_TAG);
		detailsButton.addClickListener(clickEvent -> 
				detailsButton.getUI().ifPresent(ui -> ui.navigate(Constants.JOBOFFER_PATH)));
	}
}
