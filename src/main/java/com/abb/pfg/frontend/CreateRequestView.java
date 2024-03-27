package com.abb.pfg.frontend;

import com.abb.pfg.frontend.commons.Constants;
import com.abb.pfg.frontend.components.CustomBasicDialog;
import com.abb.pfg.frontend.components.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Class which represents the creation's view of a new request
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route(Constants.REQUEST_PATH)
@PageTitle("J4S - Enviar una solicitud")
public class CreateRequestView extends MainLayout{
	
	private static final String HEADER_TITLE = "Enviar una solicitud";
	private static final String TITLE_TAG = "Título de la solicitud";
	private static final String OFFER_TAG = "Título de la oferta";
	private static final String CONFIRM_TAG = "Enviar solicitud";
	private static final String CONFIRM_HEADER = "Solicitud enviada correctamente";
	private static final String CONFIRM_TEXT = "Podrá visualizar el estado de la solicitud en \"Mis Solicitudes\"";
	private static final String CONTENT_TAG = "Contenido de la solicitud";
	private static final String CANCEL_TEXT = "Se ha cancelado el envío de la solicitud";
	
	private TextField titleField, jobOfferField;
	private TextArea contentField;
	private FormLayout formRequestLayout;
	private VerticalLayout mainLayout, contentLayout;
	private HorizontalLayout buttonsLayout;
	private Dialog dialog;
	private Button confirmButton, cancelButton, goBackButton;
	private Notification notification;
	
	public CreateRequestView() {
		init();
		contentLayout.add(new H1(HEADER_TITLE), mainLayout);
		setContent(contentLayout);
	}
	
	private void init() {
		contentLayout = new VerticalLayout();
		contentLayout.setAlignItems(Alignment.CENTER);
		this.createHeaderLayout();
		this.createContentField();
		this.createButtonsLayout();
		mainLayout = new VerticalLayout();
		mainLayout.setAlignItems(Alignment.CENTER);
		mainLayout.setWidth(Constants.WIDTH);
		mainLayout.add(formRequestLayout, contentField,buttonsLayout);
	}
	
	private void createHeaderLayout() {
		formRequestLayout = new FormLayout();
		titleField = new TextField(TITLE_TAG);
		jobOfferField = new TextField(OFFER_TAG);
		jobOfferField.setReadOnly(true);
		formRequestLayout.add(titleField, jobOfferField);
	}
	
	private void createContentField() {
		contentField = new TextArea(CONTENT_TAG);
		contentField.setWidth(Constants.WIDTH); 
		contentField.setMaxLength(Constants.DESC_LENGTH);
		contentField.setValueChangeMode(ValueChangeMode.EAGER);
		contentField.addValueChangeListener(e -> {
		    e.getSource()
		            .setHelperText(e.getValue().length() + "/" + Constants.DESC_LENGTH);
		});
	}
	
	private void createButtonsLayout() {
		buttonsLayout = new HorizontalLayout();
		confirmButton = new Button(CONFIRM_TAG);
		cancelButton = new Button(Constants.CANCEL_TAG);
		confirmButton.addClickListener(confirmEvent -> {
			if(hasAnyEmptyField()) {
				dialog = new CustomBasicDialog(Constants.ERROR_TAG,
						Constants.ERROR_TEXT);
			} else {
				dialog = new CustomBasicDialog(CONFIRM_HEADER,
						CONFIRM_TEXT);
				confirmButton.getUI().ifPresent(ui -> ui.navigate(Constants.JOBOFFER_PATH));
			}
			dialog.open();
		});
		cancelButton.addClickListener(cancelEvent -> {
			notification = new Notification(CANCEL_TEXT);
			notification.setPosition(Position.TOP_CENTER);
			notification.setDuration(Constants.NOTIF_DURATION);
			notification.open();
			cancelButton.getUI().ifPresent(ui -> ui.navigate(Constants.JOBOFFER_PATH));
		});
		buttonsLayout.add(confirmButton, cancelButton);
	}
	
	private boolean hasAnyEmptyField() {
		if(titleField.isEmpty() || contentField.isEmpty()) {
			return true;
		}
		return false;
	}
}
