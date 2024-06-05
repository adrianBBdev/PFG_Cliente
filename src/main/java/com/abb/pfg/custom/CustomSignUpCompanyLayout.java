package com.abb.pfg.custom;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.Countries;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
 * Layout with all components needed to sign up a company
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class CustomSignUpCompanyLayout extends VerticalLayout {
	
	private static final long serialVersionUID = -7542824432714907208L;
	//Components
	private TextField nameField, cifField;
	private ComboBox<String> countryComboBox;
	private MemoryBuffer memoryBuffer;
	private Upload profilePicture;
	private CustomTextArea descriptionField;
	private Button confirmButton, cancelButton;
	
	public CustomSignUpCompanyLayout() {
		this.setAlignItems(Alignment.CENTER);
		this.setWidthFull();
		setNameField();
		setCifField();									
		setComboBox();
		setProfilePictureField();						//Creamos el campo de carga de foto de perfil
		setDescriptionField();
		var verticalLayout = new VerticalLayout(getButtonsLayout());
		verticalLayout.setAlignItems(Alignment.CENTER);
		this.add(nameField, cifField, countryComboBox, profilePicture, descriptionField, verticalLayout);
	}
	
	private void setNameField() {
		nameField = new TextField(Constants.NAME_TAG);			//Creamos el campo del nombre
		nameField.setMaxLength(Constants.FIELDS_MAX_LENGTH);
		nameField.setWidth("30%");
	}
	
	/**
	 * Gets the CIF field to sign up a company
	 *
	 */
	private void setCifField() {
		cifField = new TextField(Constants.CIF_TAG);
		cifField.setMinLength(Constants.PHONE_AND_ID_LENGHT);
		cifField.setMaxLength(Constants.PHONE_AND_ID_LENGHT);
		cifField.setErrorMessage(Constants.ID_ERROR);
		cifField.addClientValidatedEventListener(event -> {
			var cif = cifField.getValue();
			var isInvalid = (cif.matches(Constants.CIF_REGEX)) ? false : true;
			cifField.setInvalid(isInvalid);
		});
		cifField.setWidth("30%");
	}
	
	private void setComboBox() {
		countryComboBox = new ComboBox<>(Constants.COUNTRY_TAG);	//Creamos el campo de país de origen
		countryComboBox.setItems(Arrays.stream(Countries.values())
                .map(Countries::getCountryName)
                .collect(Collectors.toList()));
		countryComboBox.setWidth("30%");
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
		profilePicture.addFileRejectedListener(rejectEvent -> {			//Asignamos listener de imagen rechazada
			new CustomNotification(Constants.PROF_FILE_ERROR, NotificationVariant.LUMO_ERROR);
		});
	}
	
	private void setDescriptionField() {
		descriptionField = new CustomTextArea(Constants.DESC_TAG, "");	//Creamos el campo de la descripcion personal
		descriptionField.setWidth("30%");
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
