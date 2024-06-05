/**
 * 
 */
package com.abb.pfg.custom;

import com.abb.pfg.utils.Constants;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Layout with all components needed to sign up a student
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class CustomSignUpStudentLayout extends VerticalLayout{

	private static final long serialVersionUID = 3100443946518634341L;
	//Componentes
	private TextField nameField, studiesField, dniField, phoneField;
	private CustomTextArea descriptionField;
	private MemoryBuffer memoryBuffer;
	private Upload profilePicture;
	private Button confirmButton, cancelButton;
	
	public CustomSignUpStudentLayout() {
		this.setAlignItems(Alignment.CENTER);
		this.setWidthFull();
		setNameField();
		setDniField();											//Creamos el campo del DNI
		setStudiesField();
		setDescriptionField();
		setPhoneField();										//Creamos el campo del numero de telefono
		setProfilePictureField();								//Creamos el campo de carga de foto de perfil
		var verticalLayout = new VerticalLayout(getButtonsLayout());
		verticalLayout.setAlignItems(Alignment.CENTER);
		this.add(nameField, dniField, phoneField, studiesField, profilePicture, descriptionField, verticalLayout);
	}
	
	private void setNameField() {
		nameField = new TextField(Constants.NAME_TAG);			//Creamos el campo del nombre
		nameField.setMaxLength(Constants.FIELDS_MAX_LENGTH);
		nameField.setWidth("30%");
	}
	
	private void setDniField() {
		dniField = new TextField(Constants.DNI_TAG);
		dniField.setMinLength(Constants.PHONE_AND_ID_LENGHT);
		dniField.setMaxLength(Constants.PHONE_AND_ID_LENGHT);
		dniField.setErrorMessage(Constants.ID_ERROR);
		dniField.addClientValidatedEventListener(event -> {
			var dni = dniField.getValue();
			var isInvalid = (dni.matches(Constants.DNI_REGEX)) ? false : true;
			dniField.setInvalid(isInvalid);
		});
		dniField.setWidth("30%");
	}
	
	private void setStudiesField() {
		studiesField = new TextField(Constants.STUDIES_TAG);	//Creamos el campo de los estudios
		studiesField.setMaxLength(Constants.FIELDS_MAX_LENGTH);
		studiesField.setWidth("30%");
	}
	
	private void setDescriptionField() {
		descriptionField = new CustomTextArea(Constants.DESC_TAG, "");	//Creamos el campo de la descripcion personal
		descriptionField.setWidth("40%");
	}
	
	/**
	 * Gets the phone number field to sign up a student
	 *
	 */
	private void setPhoneField() {
		phoneField = new TextField(Constants.PHONE_TAG);
		phoneField.setMaxLength(Constants.PHONE_AND_ID_LENGHT);
		phoneField.setErrorMessage(Constants.PHONE_ERROR);
		phoneField.addValueChangeListener(event -> {
			var phone = phoneField.getValue();
			var isInvalid = (phone.matches(Constants.PHONE_REGEX)) ? false : true;
			phoneField.setInvalid(isInvalid);
		});
		phoneField.setWidth("30%");
	}
	
	/**
	 * Gets the profile picture or logo upload field to sign up a student or a company
	 *
	 * @param fieldTag - tag to show depending on the option selected
	 */
	private void setProfilePictureField() {
		memoryBuffer = new MemoryBuffer();
		profilePicture = new Upload(memoryBuffer);
		profilePicture.setDropLabel(new NativeLabel(Constants.DROP_TAG));
		profilePicture.setAcceptedFileTypes(Constants.JPG, Constants.PNG);
		profilePicture.setUploadButton(new Button(Constants.PROFILE_TAG));
		profilePicture.setMaxFileSize(Constants.MAX_IMAGE_SIZE);
		profilePicture.addSucceededListener(eventUpload ->			//Asignamos listener de imagen aceptada
			new CustomNotification(Constants.UPLOAD_PIC_SUCC, NotificationVariant.LUMO_SUCCESS));
		profilePicture.addFileRejectedListener(rejectEvent ->			//Asignamos listener de imagen rechazada
			new CustomNotification(Constants.PROF_FILE_ERROR, NotificationVariant.LUMO_ERROR));
	}
	
	/**
	 * Gets the layout with buttons needed to confirm or cancel the sign up process
	 *
	 */
	private HorizontalLayout getButtonsLayout() {
		var buttonsLayout = new HorizontalLayout();				//Creamos el layout para los botones
		confirmButton = new Button(Constants.CONFIRM_TAG);			//Creamos boton de confirmar registro
		confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		cancelButton = new Button(Constants.CANCEL_TAG);	//Creamos boton de cancelar registro
		cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		buttonsLayout.add(confirmButton, cancelButton);		//Agregamos botones al layout
		return buttonsLayout;
	}

}
