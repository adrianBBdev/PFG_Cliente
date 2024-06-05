package com.abb.pfg.views;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.json.JSONException;
import org.json.JSONObject;

import com.abb.pfg.custom.CustomAppLayout;
import com.abb.pfg.custom.CustomAvatar;
import com.abb.pfg.custom.CustomFilesGrid;
import com.abb.pfg.custom.CustomNavigationOptionsPageLayout;
import com.abb.pfg.custom.CustomNotification;
import com.abb.pfg.custom.CustomNumElementsSelect;
import com.abb.pfg.custom.CustomTextArea;
import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.Countries;
import com.abb.pfg.utils.HttpRequest;
import com.vaadin.flow.component.accordion.Accordion;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.NativeLabel;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouteAlias;
import com.vaadin.flow.server.VaadinSession;

import lombok.NoArgsConstructor;

/**
 * Profile view to see the own user information
 *
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route(Constants.PROFILE_PATH)
@RouteAlias(Constants.PROFILE_PATH_2)
@PageTitle("J4S - Ver perfil")
@NoArgsConstructor
public class ProfileView extends CustomAppLayout implements HasUrlParameter<String>, BeforeEnterObserver{

	private static final long serialVersionUID = 4972186079490524195L;
	//Etiquetas
	private static final String HEADER_TAG = "Ver perfil";
	private static final String PASSWD_MSG = "Se requiere la contraseña para modificaciones de datos de usuario";
	private static final String NEW_PASSWD_MSG = "Introduzca una nueva contraseña si desea cambiarla";
	private static final String UPD_PRF_ERR = "Error al actualizar los datos del usuario";
	private static final String PRF_ERR = "Error al cargar los datos del perfil";
	private static final String AUTH_ERR = "Las credenciales no son correctas. No se pueden actualizar los datos";
	private static final String NEW_PASSWD_ERR = "Las nuevas credenciales no son válidas. Revise los datos e inténtelo de nuevo";
	private static final String PROFILE_TAG = "Carga una nueva foto de perfil";
	private static final String GET_RES_ERR = "No se han podido obtener los recursos solicitados";
	private static final String ADD_RES_TAG = "Añadir archivo";
	private static final String SEL_RES_ERR = "No ha seleccionado ningún recurso";
	private static final String ADD_RES_MSG = "El recurso ha sido añadido correctamente";
	private static final String SHW_RES_ERR = "Error al cargar el archivo";
	private static final String ADD_RES_ERR = "El recurso no ha podido ser añadido";
	//Elementos
	private VerticalLayout mainLayout, userDetailsLayout, profileDetailsLayout, mediaLayout;
	private HorizontalLayout userDetailsButtonsLayout, profileDetailsButtonsLayout;
	private CustomNavigationOptionsPageLayout navigationOptionsPageLayout;
	private Avatar userAvatar;
	private Accordion accordionLayout;
	private EmailField emailField;
	private TextField nameField, idField, studiesField, phoneField;
	private ComboBox<String> countryComboBox;
	private CustomTextArea descField;
	private PasswordField passwordField, repeatPasswordField1, repeatPasswordField2;
	private Button editUserButton, saveUserButton, cancelEditButton,
		editProfileButton, saveProfileButton, cancelProfileButton, newMediaButton;
	private Upload uploadPicture;
	private MemoryBuffer memoryBuffer;
	private CustomNumElementsSelect customSelect;
	private CustomFilesGrid filesGrid;
	private String username, userRole, userJSON, userId, userCategory, profilePic;
	private Integer numPage = 0;

	@Override
	public void setParameter(BeforeEvent event, @OptionalParameter String parameter) {
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		if(authToken == null) {
			new CustomNotification(Constants.ACC_REJ_MSG, NotificationVariant.LUMO_ERROR);
			event.forwardTo(LoginView.class);
			return;
		}
		userRole = (String) VaadinSession.getCurrent().getAttribute("role");
		if(parameter != null && !userRole.equals(Constants.ADM_ROLE)) {
			new CustomNotification(Constants.ACC_REJ_MSG, NotificationVariant.LUMO_ERROR);
			event.forwardTo(AvailableOffersView.class);
			return;
		}
		if(parameter != null) {
			userCategory = parameter;
			userId = (String) VaadinSession.getCurrent().getAttribute("userId");
		}
	}
	
	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		username = (String) VaadinSession.getCurrent().getAttribute("username");
		if(userRole.equals(Constants.GST_ROLE)){
			event.forwardTo(AvailableOffersView.class);
			return;
		}
		init();		//Inicializamos la vista y añadimos el layout principal
	}

	/**
	 * Initializes the view components
	 *
	 */
	private void init() {
		mainLayout = new VerticalLayout();		//Inicializamos el layout principal
		mainLayout.setAlignItems(Alignment.CENTER);
		mainLayout.setWidthFull();
		setAccordionLayout();
		var baseVerticalLayout = new VerticalLayout();
		baseVerticalLayout.add(new H1(HEADER_TAG), mainLayout);
		baseVerticalLayout.setAlignItems(Alignment.CENTER);
		this.setContent(baseVerticalLayout);
	}

	/**
	 * Gets the accordion layout, which will contain the profile details
	 *
	 */
	private void setAccordionLayout() {
		accordionLayout = new Accordion();
		accordionLayout.setMaxWidth("1200px");
		accordionLayout.setWidthFull();
		setUserDetailsLayoutContent();
		accordionLayout.add(Constants.USER_INFO_TAG, userDetailsLayout);
		profileDetailsLayout = new VerticalLayout();
		profileDetailsLayout.setAlignItems(Alignment.CENTER);
		if(userRole.equals(Constants.ADM_ROLE)) {	//ADMIN editing
			mainLayout.add(accordionLayout);
			if(userId != null) {	//Editing other USER
				setProfileAndMediaContent(userId, userCategory);
				return;
			}
			emailField.setValue(username);
		} else {
			setProfileAndMediaContent(username, userRole);
		}
	}
	
	/**
	 * Sets up the profile details fiels
	 * 
	 * @param userId - user id to display
	 * @param userCategory - user's role
	 */
	private void setProfileAndMediaContent(String userId, String userCategory) {
		emailField.setValue(userId);
		if(userCategory.equals(Constants.STD_ROLE)) {
			setStudentContent(userId);					//Si STUDENT
		} else {
			setCompanyContent(userId);					//Si COMPANY
		}
		accordionLayout.add(Constants.PRF_INFO_TAG, profileDetailsLayout);
		setMediaContent(userId);
		accordionLayout.add(Constants.FILE_MEDIA_TAG, mediaLayout);
		mainLayout.add(accordionLayout);
	}

	/**
	 * Gets the user info details
	 *
	 */
	private void setUserDetailsLayoutContent() {
		userDetailsLayout = new VerticalLayout();
		userDetailsLayout.setAlignItems(Alignment.CENTER);
		emailField = new EmailField(Constants.EMAIL_TAG);
		emailField.setReadOnly(true);
		emailField.setWidth("50%");
		passwordField = new PasswordField(Constants.PASSWORD_TAG + " actual");
		passwordField.setWidth("50%");
		passwordField.setPlaceholder(PASSWD_MSG);
		passwordField.setVisible(false);
		repeatPasswordField1 = new PasswordField(Constants.NEW_PASSWD_TAG);
		repeatPasswordField1.setWidth("50%");
		repeatPasswordField1.setVisible(false);
		repeatPasswordField1.setPlaceholder(NEW_PASSWD_MSG);
		repeatPasswordField2 = new PasswordField(Constants.CONFIRMPASS_TAG);
		repeatPasswordField2.setWidth("50%");
		repeatPasswordField2.setVisible(false);
		setUserDetailsButtons();
		userDetailsLayout.add(emailField, passwordField,
				repeatPasswordField1, repeatPasswordField2, userDetailsButtonsLayout);
	}

	/**
	 * Gets the layout which has all view's action buttons
	 *
	 */
	private void setUserDetailsButtons() {
		userDetailsButtonsLayout = new HorizontalLayout();
		editUserButton = new Button(Constants.EDIT_TAG);
		editUserButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		editUserButton.addClickListener(event -> editOrCancelUserDetailsListener());
		cancelEditButton = new Button(Constants.CANCEL_TAG);
		cancelEditButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		cancelEditButton.addClickListener(event -> editOrCancelUserDetailsListener());
		saveUserButton = new Button(Constants.SV_CHG_TAG);
		saveUserButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveUserButton.setVisible(false);
		saveUserButton.addClickListener(event -> saveUserDetailsListener());
		userDetailsButtonsLayout.add(saveUserButton, editUserButton);
	}

	/**
	 * Listener assigned to the edit/cancel user's info
	 *
	 */
	private void editOrCancelUserDetailsListener() {
		if(saveUserButton.isVisible()) {
			userDetailsButtonsLayout.replace(cancelEditButton, editUserButton);
			saveUserButton.setVisible(!saveUserButton.isVisible());
			var emailValue = (userCategory != null && userId != null) ? userId : username;
			emailField.setValue(emailValue);
		} else {
			saveUserButton.setVisible(!saveUserButton.isVisible());
			userDetailsButtonsLayout.replace(editUserButton, cancelEditButton);
		}
		emailField.setReadOnly(!emailField.isReadOnly());
		passwordField.setVisible(!passwordField.isVisible());
		repeatPasswordField1.setVisible(!repeatPasswordField1.isVisible());
		repeatPasswordField2.setVisible(!repeatPasswordField2.isVisible());
	}

	/**
	 * Listener assigned to the save changes button
	 *
	 */
	private void saveUserDetailsListener() {
		if(!areUserFieldsOK()) {
			return;
		}
		var bodyRequest = new JSONObject(userJSON).getJSONObject("user").toString();
		boolean updateOK = sendUpdateProfileInfoRequest(Constants.USERS_REQ, bodyRequest);
		if(updateOK) {
			updateSessionParameters();
			editOrCancelUserDetailsListener();
			new CustomNotification(Constants.UPD_MSG, NotificationVariant.LUMO_PRIMARY);
			return;
		}
		editOrCancelUserDetailsListener();
		new CustomNotification(UPD_PRF_ERR, NotificationVariant.LUMO_ERROR);
	}

	/**
	 * Gets the student profile info content
	 *
	 */
	private void setStudentContent(String userId) {
		var responseBody = sendGetProfileInfoRequest(Constants.STD_REQ + "/student?username=" + userId);
		if(responseBody == null) {
			new CustomNotification(PRF_ERR, NotificationVariant.LUMO_ERROR);
			return;
		}
		setStudentContentFields();
		parseStudentJSON(responseBody);
		setProfileDetailsButtons();
		setProfilePictureField();
		profileDetailsLayout.add(userAvatar, nameField, idField, phoneField,
				studiesField, descField, profileDetailsButtonsLayout);
	}
	
	/**
	 * Sets up the fields that shows the student profile details
	 * 
	 */
	private void setStudentContentFields() {
		nameField = new TextField(Constants.NAME_TAG);
		nameField.setReadOnly(!nameField.isReadOnly());
		nameField.setWidth("50%");
		idField = new TextField(Constants.DNI_TAG);
		idField.setReadOnly(!idField.isReadOnly());
		idField.setWidth("50%");
		phoneField = new TextField(Constants.PHONE_TAG);
		phoneField.setMaxLength(Constants.PHONE_AND_ID_LENGHT);
		phoneField.setErrorMessage(Constants.PHONE_ERROR);
		phoneField.addValueChangeListener(event -> {
			var phone = phoneField.getValue();
			var isInvalid = (phone.matches(Constants.PHONE_REGEX)) ? false : true;
			phoneField.setInvalid(isInvalid);
		});
		phoneField.setReadOnly(!phoneField.isReadOnly());
		phoneField.setWidth("50%");
		studiesField = new TextField(Constants.STUDIES_TAG);
		studiesField.setReadOnly(!studiesField.isReadOnly());
		studiesField.setWidth("50%");
		descField = new CustomTextArea(Constants.DESC_TAG, "");
		descField.setReadOnly(!descField.isReadOnly());
		descField.setWidth("70%");
	}

	/**
	 * Gets the company profile info content
	 *
	 */
	private void setCompanyContent(String userId) {
		var responseBody = sendGetProfileInfoRequest(Constants.CMP_REQ + "/company?username=" + userId);
		if(responseBody == null) {
			new CustomNotification(PRF_ERR, NotificationVariant.LUMO_ERROR);
			return;
		}
		setCompanyContentFields();
		parseCompanyJSON(responseBody);
		setProfileDetailsButtons();
		setProfilePictureField();
		profileDetailsLayout.add(userAvatar, nameField, idField, countryComboBox,
				descField, profileDetailsButtonsLayout);
	}
	
	/**
	 * Sets up the fields that shows the company profile details
	 * 
	 */
	private void setCompanyContentFields() {
		nameField = new TextField(Constants.NAME_TAG);
		nameField.setReadOnly(!nameField.isReadOnly());
		nameField.setWidth("50%");
		idField = new TextField(Constants.CIF_TAG);
		idField.setReadOnly(!idField.isReadOnly());
		idField.setWidth("50%");
		countryComboBox = new ComboBox<>(Constants.COUNTRY_TAG);
		countryComboBox.setReadOnly(!countryComboBox.isReadOnly());
		countryComboBox.setItems(Arrays.stream(Countries.values())
                .map(Countries::getCountryName)
                .collect(Collectors.toList()));
		countryComboBox.setWidth("50%");
		descField = new CustomTextArea(Constants.DESC_TAG, "");
		descField.setReadOnly(!descField.isReadOnly());
		descField.setWidth("70%");
	}
	
	/**
	 * Sets up the list of media resources assigned to the user
	 * 
	 * @param userId - user's id
	 */
	private void setMediaContent(String userId) {
		mediaLayout = new VerticalLayout();
		mediaLayout.setAlignItems(Alignment.CENTER);
		mediaLayout.setMaxWidth("1500px");
		mediaLayout.setWidthFull();
		newMediaButton = new Button(Constants.ADD_FILE_TAG, new Icon(VaadinIcon.PLUS_CIRCLE_O));
		newMediaButton.addClickListener(event -> addNewFileButtonListener());
		mediaLayout.add(newMediaButton);
		customSelect = new CustomNumElementsSelect();
		customSelect.addValueChangeListener(event -> customSelectListener());
		setGridContent(userId);
	}
	
	/**
	 * Sets up the media resources grid
	 * 
	 * @param userId
	 */
	private void setGridContent(String userId) {
		var mediaBody = sendGetListRequest(Constants.MEDIA_REQ + "?username=" + userId, numPage, customSelect.getValue());
		if(mediaBody == null) {
			new CustomNotification(GET_RES_ERR, NotificationVariant.LUMO_ERROR);
			return;
		}
		this.parseJSONMediaList(mediaBody);
	}
	
	/**
	 * Gets the custom layout used to navigate between pages
	 *
	 * @param isShowingFirst - true if it shows the first resource, false if not
	 * @param isShowingLast - true if it shows the last resource, false if not
	 */
	private void setNavigationOptionsPageLayout(boolean isShowingFirst, boolean isShowingLast) {
		if(navigationOptionsPageLayout == null) {
			navigationOptionsPageLayout = new CustomNavigationOptionsPageLayout(isShowingFirst, isShowingLast);
			navigationOptionsPageLayout.getNextPageButton().addClickListener(event -> nextPageListener());
			navigationOptionsPageLayout.getPrevPageButton().addClickListener(event -> prevPageListener());
			return;
		}
		navigationOptionsPageLayout.setEnabledNextButton(isShowingLast);
		navigationOptionsPageLayout.setEnabledPrevButton(isShowingFirst);
	}
	
	/**
	 * Builds the layout which will contain all the user info to show
	 *
	 */
	private void setProfileDetailsButtons() {
		profileDetailsButtonsLayout = new HorizontalLayout();
		editProfileButton = new Button(Constants.EDIT_TAG);
		editProfileButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		editProfileButton.addClickListener(event -> editProfileDetailsListener());
		cancelProfileButton = new Button(Constants.CANCEL_TAG);
		cancelProfileButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		cancelProfileButton.addClickListener(event -> cancelProfileDetailsListener());
		saveProfileButton = new Button(Constants.SV_CHG_TAG);
		saveProfileButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		saveProfileButton.setVisible(false);
		saveProfileButton.addClickListener(event -> saveProfileDetailsListener());
		profileDetailsButtonsLayout.add(saveProfileButton, editProfileButton);
	}
	
	/**
	 * Specifies if the user fields are OK or not.
	 *
	 * @return boolean - true if fields OK, false if not
	 */
	private boolean areUserFieldsOK() {
		if(passwordField.getValue().isBlank() || !areCredentialsOK(passwordField.getValue())) {
			new CustomNotification(AUTH_ERR, NotificationVariant.LUMO_ERROR);
			return false;
		}
		if(repeatPasswordField1.getValue().isBlank() && repeatPasswordField2.getValue().isBlank()) {
			updateUserJSONWithNewValue("password", passwordField.getValue());
		}
		if(!repeatPasswordField1.getValue().isBlank()
				|| !repeatPasswordField2.getValue().isBlank()
				&& repeatPasswordField1.getValue().equals(repeatPasswordField2.getValue())){
			updateUserJSONWithNewValue("password", repeatPasswordField1.getValue());
		} else if(!repeatPasswordField1.getValue().isBlank()
				|| !repeatPasswordField2.getValue().isBlank()
				&& !repeatPasswordField1.getValue().equals(repeatPasswordField2.getValue())){
			new CustomNotification(NEW_PASSWD_ERR,
					NotificationVariant.LUMO_ERROR);
			return false;
		}
		var emailValue = (userCategory != null && userId != null) ? userId : username;
		if(!emailField.getValue().equals(emailValue)) {
			updateUserJSONWithNewValue("username", emailField.getValue());
		}
		return true;
	}
	
	/**
	 * Gets the custom avatar
	 *
	 * @param name - user's name
	 * @param logo - user's profile picture
	 */
	private void setCutsomAvatar(String name, String logo) {
		if(logo.isBlank()) {
			userAvatar = new Avatar(name);
		} else {
			userAvatar = new CustomAvatar(logo);
		}
		userAvatar.setHeight("15%");
		userAvatar.setWidth("15%");
	}
	
	/**
	 * Gets the profile picture or logo upload field to sign up a student or a company
	 *
	 */
	private void setProfilePictureField() {
		memoryBuffer = new MemoryBuffer();
		uploadPicture = new Upload(memoryBuffer);
		uploadPicture.setDropLabel(new NativeLabel(Constants.DROP_TAG));
		uploadPicture.setAcceptedFileTypes(Constants.JPG, Constants.PNG);
		uploadPicture.setUploadButton(new Button(PROFILE_TAG));
		uploadPicture.setMaxFileSize(Constants.MAX_VIDEO_SIZE);
		uploadPicture.addSucceededListener(eventUpload ->			//Asignamos listener de imagen aceptada
			new CustomNotification(Constants.UPLOAD_PIC_SUCC, NotificationVariant.LUMO_SUCCESS));
		uploadPicture.addFileRejectedListener(rejectEvent ->		//Asignamos listener de imagen rechazada
			new CustomNotification(Constants.PROF_FILE_ERROR, NotificationVariant.LUMO_ERROR));
	}
	
	/**
	 * Process the uploaded image to verify if it is OK or not
	 *
	 * @param imageFile - image file data
	 * @param fileName - imagen file name
	 * @param storePath - path where the picture will be stored
	 */
	private void processImage(InputStream imageFile, String fileName) {
		try {
			var file = new File(Constants.STORED_PIC_PATH + "\\" + fileName);
			Files.copy(imageFile, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			new CustomNotification(Constants.UPLOAD_PIC_ERR, NotificationVariant.LUMO_ERROR);
		}
	}
	
	/**
	 * Updates the session parameters when it is called
	 * 
	 */
	private void updateSessionParameters() {
		if(userId != null && userCategory != null) {
			userId = emailField.getValue();
			VaadinSession.getCurrent().setAttribute("userId", userId);
			return;
		}
		username = emailField.getValue();
		VaadinSession.getCurrent().setAttribute("username", username);
		var passwordValue = (repeatPasswordField1.getValue().isEmpty()) 
				? passwordField.getValue() : repeatPasswordField1.getValue();
		var httpRequest = new HttpRequest(Constants.AUTH_REQ + "/login" + "?username=" + username 
				+ "&password=" + passwordValue);
		var responseBody = httpRequest.executeLoginRequest();
		if(responseBody == null) {
			this.getUI().ifPresent(ui -> ui.navigate(Constants.LOGIN_PATH));
			return;
		}
		try {
			var jsonResponse = new JSONObject(responseBody);
			var token = jsonResponse.getString("token");
			VaadinSession.getCurrent().setAttribute("authToken", token);
		} catch(JSONException e) {
			this.getUI().ifPresent(ui -> ui.navigate(Constants.LOGIN_PATH));
		}
	}
	
	private String setUserRoleTmp() {
		if(userRole.equals(Constants.ADM_ROLE) && userId != null) {
			return userCategory;
		}
		return userRole;
	}
	
	//LISTENERS
	
	/**
	 * Listener assigned to the add resource option button
	 * 
	 */
	private void addNewFileButtonListener() {
		var dialog = new Dialog();
		dialog.getHeader().add(new H2(ADD_RES_TAG));
		dialog.setModal(true);
		dialog.setDraggable(true);
		var memoryBuffer = new MemoryBuffer();
		var resource = new Upload(memoryBuffer);
		resource.setDropLabel(new NativeLabel(Constants.DROP_TAG));
		resource.setAcceptedFileTypes(Constants.JPG, Constants.PNG, Constants.PDF, Constants.AVI, Constants.MP4);
		resource.addSucceededListener(eventUpload ->			//Asignamos listener de imagen aceptada
			new CustomNotification(Constants.UPLOAD_PIC_SUCC, NotificationVariant.LUMO_SUCCESS));
		resource.addFileRejectedListener(rejectEvent ->			//Asignamos listener de imagen rechazada
			new CustomNotification(Constants.PROF_FILE_ERROR, NotificationVariant.LUMO_ERROR));
		var saveButton = new Button("Añadir", event -> confirmAddMediaListener(dialog, memoryBuffer));
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		var cancelButton = new Button(Constants.CANCEL_TAG, event -> dialog.close());
		dialog.getFooter().add(saveButton, cancelButton);
		dialog.add(resource);
		dialog.open();
	}
	
	/**
	 * Listener assigned to the confirm add media option button
	 * 
	 * @param dialog - dialog where it is being displayed the button
	 * @param memoryBuffer - memory buffer where it has been loaded the media
	 */
	private void confirmAddMediaListener(Dialog dialog, MemoryBuffer memoryBuffer) {
		var fileName = memoryBuffer.getFileName();
		var fileData = memoryBuffer.getInputStream();
		if(fileData == null) {
			new CustomNotification(SEL_RES_ERR, NotificationVariant.LUMO_WARNING);
			return;
		}
		var mediaJSON = getJSONMediaObject(fileName, userJSON);
		if(sendCreateMediaRequest(mediaJSON)) {
			setGridContent(emailField.getValue());
			new CustomNotification(ADD_RES_MSG, NotificationVariant.LUMO_SUCCESS);
			try {
				var file = new File(Constants.STORED_MEDIA_PATH + "\\" + fileName);
				Files.copy(fileData, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException e) {
				new CustomNotification(SHW_RES_ERR, NotificationVariant.LUMO_ERROR);
			}
		} else {
			new CustomNotification(ADD_RES_ERR, NotificationVariant.LUMO_ERROR);
		}
		dialog.close();
	}

	/**
	 * Listener assigned to the edit profil button
	 *
	 */
	private void editProfileDetailsListener() {
		profileDetailsButtonsLayout.replace(editProfileButton, cancelProfileButton);
		profileDetailsLayout.replace(userAvatar, uploadPicture);
		saveProfileButton.setVisible(!saveProfileButton.isVisible());
		nameField.setReadOnly(!nameField.isReadOnly());
		descField.setReadOnly(!descField.isReadOnly());
		var userRoleTmp = setUserRoleTmp();
		if(userRoleTmp.equals(Constants.STD_ROLE)) {
			studiesField.setReadOnly(!studiesField.isReadOnly());
			phoneField.setReadOnly(!phoneField.isReadOnly());
			return;
		}
		if(userRoleTmp.equals(Constants.CMP_ROLE)) {
			countryComboBox.setReadOnly(!countryComboBox.isReadOnly());
		}
	}

	/**
	 * Listener assigned to the cancel edit profile button
	 *
	 */
	private void cancelProfileDetailsListener() {
		var userRoleTmp = setUserRoleTmp();
		if(userRoleTmp.equals(Constants.STD_ROLE)) {
			parseStudentJSON(userJSON);
		} else if(userRoleTmp.equals(Constants.CMP_ROLE)) {
			parseCompanyJSON(userJSON);
		}
		profileDetailsButtonsLayout.replace(cancelProfileButton, editProfileButton);
		profileDetailsLayout.replace(uploadPicture, userAvatar);
		saveProfileButton.setVisible(!saveProfileButton.isVisible());
		nameField.setReadOnly(!nameField.isReadOnly());
		descField.setReadOnly(!descField.isReadOnly());
		if(userRoleTmp.equals(Constants.STD_ROLE)) {
			studiesField.setReadOnly(!studiesField.isReadOnly());
			phoneField.setReadOnly(!phoneField.isReadOnly());
			return;
		}
		if(userRoleTmp.equals(Constants.CMP_ROLE)) {
			countryComboBox.setReadOnly(!countryComboBox.isReadOnly());
		}
	}
	
	/**
	 * Listener assigned to the save changes button
	 *
	 */
	private void saveProfileDetailsListener() {
		var userRoleTmp = setUserRoleTmp();
		String putUrl = null;
		String bodyRequest = null;
		if(userRoleTmp.equals(Constants.STD_ROLE)) {
			if(nameField.isEmpty() || phoneField.isEmpty() || phoneField.isInvalid() || descField.isEmpty() || studiesField.isEmpty()) {
				new CustomNotification(Constants.UPD_WRN, NotificationVariant.LUMO_WARNING);
				return;
			}
			putUrl = Constants.STD_REQ;
			bodyRequest = setStudentJSONObject();
		} else if (userRoleTmp.equals(Constants.CMP_ROLE)) {
			if(nameField.isEmpty() || countryComboBox.isEmpty() || descField.isEmpty()) {
				new CustomNotification(Constants.UPD_WRN, NotificationVariant.LUMO_WARNING);
				return;
			}
			putUrl = Constants.CMP_REQ;
			bodyRequest = setCompanyJSONObject();
		}
		if(userJSON.equals(bodyRequest)){
			new CustomNotification(Constants.NOT_UPD_MSG, NotificationVariant.LUMO_WARNING);
			return;
		}
		boolean updateOK = sendUpdateProfileInfoRequest(putUrl, bodyRequest);
		handleUpdateProfileInfoRequestResult(updateOK, bodyRequest);
	}
	
	/**
	 * Handles the result of the update media request
	 * 
	 * @param result - boolean - true if it has been updated, false if not
	 * @param bodyRequest - body request sent into the request
	 */
	private void handleUpdateProfileInfoRequestResult(boolean result, String bodyRequest) {
		if(result) {
			userJSON = bodyRequest;
			if(!memoryBuffer.getFileName().isEmpty()) {
				var profilePicTmp = profilePic;
				profilePic = memoryBuffer.getFileName();
				processImage(memoryBuffer.getInputStream(), profilePic);
				deleteFileFromSystem(profilePicTmp);
			}
			cancelProfileDetailsListener();
			new CustomNotification(Constants.UPD_MSG, NotificationVariant.LUMO_SUCCESS);
			return;
		}
		cancelProfileDetailsListener();
		new CustomNotification(UPD_PRF_ERR, NotificationVariant.LUMO_ERROR);
	}
	
	/**
	 * Deletes the selected file from the system
	 * 
	 * @param fileName - file's name to delete
	 * @return boolean - true if it has been deleted, false if not
	 */
	private boolean deleteFileFromSystem(String fileName) {
		try {
			var file = new File(Constants.STORED_MEDIA_PATH + "\\" + fileName);
			return file.delete();
		} catch (SecurityException e) {
			System.err.println("Error: no se ha podido eliminar el archivo del sistema");
			return false;
		}
	}
	
	/**
	 * Listener assigned to the next page button
	 * 
	 */
	private void nextPageListener() {
		numPage++;
		setGridContent(emailField.getValue());
	}
	
	/**
	 * Listener assigned to the previous page button
	 * 
	 */
	private void prevPageListener() {
		numPage--;
		setGridContent(emailField.getValue());
	}
	
	/**
	 * Listener assigned to the select which allows to select the numer of elements to display
	 * 
	 */
	private void customSelectListener() {
		numPage = 0;
		setGridContent(emailField.getValue());
	}
	
	//PARSEOS JSON
	
	/**
	 * Specifies if the student fields are OK or not, and builds the student JSON object
	 *
	 * @return String - student JSON object if fields OK, null if not
	 */
	private String setStudentJSONObject() {		
		try {
			var jsonObject = new JSONObject(userJSON);
			if(!nameField.getValue().isBlank()) {
				jsonObject.put("name", nameField.getValue());
			}
			if(!String.valueOf(phoneField.getValue()).isBlank()) {
				jsonObject.put("phoneNumber", String.valueOf(phoneField.getValue()));
			}
			if(!studiesField.getValue().isBlank()) {
				jsonObject.put("studies", studiesField.getValue());
			}
			if(!descField.getValue().isBlank()) {
				jsonObject.put("description", descField.getValue());
			}
			var picValue = memoryBuffer.getFileName();
			if(!picValue.isEmpty() && !profilePic.equals(picValue)) {
				jsonObject.put("profilePicture", picValue);
			}
			return jsonObject.toString();
		} catch (JSONException e) {
			return null;
		}
	}
	
	/**
	 * Specifies if the company fields are OK or not
	 *
	 * @return String - company JSON object if fields OK, null if not
	 */
	private String setCompanyJSONObject() {
		try {
			var jsonObject = new JSONObject(userJSON);
			if(!nameField.getValue().isBlank()) {
				jsonObject.put("name", nameField.getValue());
			}
			if(!countryComboBox.getValue().isBlank()) {
				jsonObject.put("country", countryComboBox.getValue());
			}
			if(!descField.getValue().isBlank()) {
				jsonObject.put("description", descField.getValue());
			}
			if(!memoryBuffer.getFileName().isEmpty() && !profilePic.equals(memoryBuffer.getFileName())) {
				jsonObject.put("profilePicture", memoryBuffer.getFileName());
			}
			return jsonObject.toString();
		} catch (JSONException e) {
			return null;
		}
	}
	
	/**
	 * Updates any user JSON field
	 * 
	 * @param field - field to update
	 * @param newValue - new value that the field acquires
	 */
	private void updateUserJSONWithNewValue(String field, String newValue) {
		try {
			var jsonObject = new JSONObject(userJSON);
			jsonObject.getJSONObject("user").put(field, newValue);
			userJSON = jsonObject.toString();
		} catch(JSONException e) {
			System.err.println("Error al parsear el objeto JSON: " + e.getMessage());
			new CustomNotification(Constants.ERR_MSG, NotificationVariant.LUMO_ERROR);
		}
	}
	
	/**
	 * Gets the student details from a JSON object
	 *
	 * @param responseBody - response body which contains the JSON object to analyze
	 */
	private void parseStudentJSON (String responseBody) {
		try {
			var studentJSON = new JSONObject(responseBody);
			nameField.setValue(studentJSON.getString("name"));
			idField.setValue(studentJSON.getString("dni"));
			profilePic = studentJSON.getString("profilePicture");
			setCutsomAvatar(nameField.getValue(), profilePic);
			studiesField.setValue(studentJSON.getString("studies"));
			descField.setValue(studentJSON.getString("description"));
			phoneField.setValue(studentJSON.getString("phoneNumber"));
			userJSON = studentJSON.toString();
		} catch (JSONException e) {
			System.err.println("Error al parsear el objeto JSON: " + e.getMessage());
			new CustomNotification(Constants.ERR_MSG, NotificationVariant.LUMO_ERROR);
		}
	}

	/**
	 * Gets the company details from a JSON object
	 *
	 * @param responseBody - response body which contains the JSON object to analyze
	 */
	private void parseCompanyJSON (String responseBody) {
		try {
			var companyJSON = new JSONObject(responseBody);
			nameField.setValue(companyJSON.getString("name"));
			idField.setValue(companyJSON.getString("cif"));
			profilePic = companyJSON.getString("profilePicture");
			setCutsomAvatar(nameField.getValue(), profilePic);
			countryComboBox.setValue(companyJSON.getString("country"));
			descField.setValue(companyJSON.getString("description"));
			userJSON = companyJSON.toString();
		} catch (JSONException e) {
			System.err.println("Error al parsear el objeto JSON: " + e.getMessage());
			new CustomNotification(Constants.ERR_MSG, NotificationVariant.LUMO_ERROR);
		}
	}
	
	/**
	 * Gets the multimedia objects from a JSON response body
	 * 
	 * @param responseBody - list of multimedia objects
	 */
	private void parseJSONMediaList(String responseBody) {
		try {
			var jsonObject = new JSONObject(responseBody);
			var contentArray = jsonObject.getJSONArray("content");
			var isShowingFirst = jsonObject.getBoolean("first");
			var isShowingLast = jsonObject.getBoolean("last");
			setNavigationOptionsPageLayout(isShowingFirst, isShowingLast);
			if(filesGrid == null) {
				filesGrid = new CustomFilesGrid(contentArray, userRole, username);
				mediaLayout.add(filesGrid, customSelect, navigationOptionsPageLayout);
			} else {
				var tempGrid = new CustomFilesGrid(contentArray, userRole, username);
				mediaLayout.replace(filesGrid, tempGrid);
				filesGrid = tempGrid;
			}
			if(contentArray.length() == 0) {
				new CustomNotification("No hay recursos multimedia disponibles.", NotificationVariant.LUMO_WARNING);
			}
		} catch(JSONException e) {
			new CustomNotification(GET_RES_ERR, NotificationVariant.LUMO_ERROR);
			return;
		}
	}
	
	/**
	 * Builds a media JSON obejct from the mulimedia's values
	 * 
	 * @param name - media's file name
	 * @param user - media's file owner
	 * @return String - media JSON object
	 */
	private String getJSONMediaObject(String name, String user) {
		try {
			var mediaJSON = new JSONObject();
			mediaJSON.put("name", name);
			mediaJSON.put("user", new JSONObject(user).getJSONObject("user"));
			return mediaJSON.toString();
		} catch (JSONException e) {
			System.err.println(Constants.JSON_ERR + e.getMessage());
			new CustomNotification(Constants.ERR_MSG, NotificationVariant.LUMO_ERROR);
			return null;
		}
	}

	//HTTP REQUESTS
	
	/**
	 * Sends a http request to get the user's profile info
	 *
	 * @param getUrl - url request to get the profile info details
	 * @return String - response body
	 */
	private String sendGetProfileInfoRequest(String getUrl) {
		var httpRequest = new HttpRequest(getUrl);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpGet(authToken);
	}

	/**
	 * Sends the http update request with the new info
	 *
	 * @param putUrl - update URL request
	 * @param bodyRequest - body request required in the put request
	 * @return boolean - true if fields are updated, false if not
	 */
	private boolean sendUpdateProfileInfoRequest(String putUrl, String bodyRequest) {
		var httpRequest = new HttpRequest(putUrl);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpPut(authToken, bodyRequest);
	}
	
	/**
	 * Specifies if the credentials are ok or not
	 *
	 * @param password - password to analyze
	 * @return boolean - true if password is OK, false if not
	 */
	private boolean areCredentialsOK(String password) {
		if(password.isEmpty()) {
			return false;
		}
		var httpRequest = new HttpRequest(Constants.AUTH_REQ + "/login?username=" + username + "&password=" + password);
		return httpRequest.executeHttpPost(null, null);
	}
	
	/**
	 * Sends the http request to get the list of user's media files
	 * 
	 * @param getUrl - url needed to send the request
	 * @param numPage - page number to display
	 * @param numElements - number of elements to display
	 * @return Stirng - response body
	 */
	private String sendGetListRequest(String getUrl, Integer numPage, Integer numElements) {
		var httpRequest = new HttpRequest(getUrl + "&page=" + numPage);
		getUrl = (numElements == null) ? getUrl : getUrl + "&size=" + numElements;
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpGet(authToken);
	}
	
	/**
	 * Sends an http post request to create a new media file
	 * 
	 * @param requestBody - the media file to create
	 * @return boolean - true if it has been created, false if not
	 */
	private boolean sendCreateMediaRequest(String requestBody) {
		var httpRequest = new HttpRequest(Constants.MEDIA_REQ);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpPost(authToken, requestBody);
	}
}