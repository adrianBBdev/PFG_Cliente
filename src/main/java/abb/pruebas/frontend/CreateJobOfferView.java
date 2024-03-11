package abb.pruebas.frontend;

import java.io.InputStream;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import abb.pruebas.frontend.commons.Constants;
import abb.pruebas.frontend.components.CustomBasicDialog;
import abb.pruebas.frontend.components.MainLayout;

/**
 * Class which represents the creation's view of a new job offer
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route(Constants.CREATEJOBOFFER_PATH)
@PageTitle("J4S - Crear nueva oferta")
public class CreateJobOfferView extends MainLayout{
	
	private static final String HEADER_TAG = "Crear una nueva oferta de trabajo";
	private static final String TITLE_TAG = "Título de la oferta";
	private static final String DESC_TAG = "Descripción de la oferta";
	private static final String UPLOAD_TAG = "Sube archivos relativos a la oferta, que estarán visibles para los estudiantes";
	private static final String CONFIRM_TEXT = "Oferta creada correctamente";
	private static final String CANCEL_TEXT = "Se ha cancelado la creación de la oferta";
	private static final String CREATE_TAG = "Crear oferta";
	
	private FormLayout formOfferLayout;
	private TextField titleField, addressField, cityField;
	private TextArea descriptionField;
	private DatePicker startDate, endDate;
	private IntegerField vacanciesField;
	private ComboBox<String> comboModality, comboArea;
	private Button confirmButton, cancelButton;
	private VerticalLayout mainLayout, contentLayout;
	private HorizontalLayout buttonsLayout;
	private Dialog dialog;
	private MultiFileMemoryBuffer resources;
	private Upload upload;
	private Label uploadLabel;
	private Notification notification;

	public CreateJobOfferView() {
		init();
		contentLayout.add(new H1(HEADER_TAG), mainLayout);
		setContent(contentLayout);
	}
	
	private void init() {
		contentLayout = new VerticalLayout();
		contentLayout.setAlignItems(Alignment.CENTER);
		formOfferLayout = new FormLayout();
		titleField = new TextField(TITLE_TAG);
		vacanciesField = new IntegerField(Constants.VACANCIES_TAG);
		vacanciesField.setValue(1);
		vacanciesField.setStepButtonsVisible(true);
		vacanciesField.setMin(1);
		addressField = new TextField(Constants.ADDRESS_TAG);
		cityField = new TextField(Constants.CITY_TAG);
		startDate = new DatePicker(Constants.START_TAG);
		endDate = new DatePicker(Constants.END_TAG);
		comboModality = new ComboBox<>(Constants.MODALITY_TAG);
		comboModality.setItems("Presencial", "Teletrabajo", "Híbrido");
		comboArea = new ComboBox<>(Constants.AREA_TAG);
		comboArea.setItems("IT", "COMPUTACIÓN", "INDUSTRIALES");
		formOfferLayout.add(titleField, vacanciesField,addressField, cityField, comboModality, comboArea, startDate, endDate);
		createUpload();
		createDecriptionField();
		mainLayout = new VerticalLayout();
		mainLayout.setAlignItems(Alignment.CENTER);
		mainLayout.setWidth(Constants.WIDTH);
		createButtonsLayout();
		mainLayout.add(formOfferLayout, uploadLabel, upload, descriptionField, buttonsLayout);
	}
	
	private void createDecriptionField() {
		descriptionField = new TextArea(DESC_TAG);
		descriptionField.setWidth(Constants.WIDTH);
		descriptionField.setMaxLength(Constants.DESC_LENGTH);
		descriptionField.setValueChangeMode(ValueChangeMode.EAGER);
		descriptionField.addValueChangeListener(e -> {
		    e.getSource()
		            .setHelperText(e.getValue().length() + "/" + Constants.DESC_LENGTH);
		});
	}
	
	private void createUpload() {
		uploadLabel = new Label(UPLOAD_TAG);
		resources = new MultiFileMemoryBuffer();
		upload = new Upload(resources);
		upload.setUploadButton(new Button(Constants.UPLOAD_TAG));
		upload.addSucceededListener(eventUpload -> {
		    String fileName = eventUpload.getFileName();
		    InputStream inputStream = resources.getInputStream(fileName);
		});
	}
	
	private void createButtonsLayout() {
		buttonsLayout = new HorizontalLayout();
		confirmButton = new Button(CREATE_TAG);
		cancelButton = new Button(Constants.CANCEL_TAG);
		confirmButton.addClickListener(event -> {
			if(hasAnyEmptyField()) {
				dialog = new CustomBasicDialog(Constants.ERROR_TAG,
						Constants.ERROR_TEXT);
				dialog.open();
			} else {
				this.setNotification(CONFIRM_TEXT);
			}
		});
		
		cancelButton.addClickListener(event -> {
			this.setNotification(CANCEL_TEXT);
		});
		buttonsLayout.add(confirmButton, cancelButton);
	}
	
	private void setNotification(String text) {
		notification = new Notification(text);
		notification.setPosition(Position.TOP_CENTER);
		notification.setDuration(Constants.NOTIF_DURATION);
		notification.open();
	}
	
	private boolean hasAnyEmptyField() {
		if(titleField.isEmpty() || addressField.isEmpty() || cityField.isEmpty() 
				|| comboModality.isEmpty() || comboArea.isEmpty() || startDate.isEmpty() 
				|| endDate.isEmpty() || descriptionField.isEmpty()) {
			return true;
		}
		return false;
	}
}
