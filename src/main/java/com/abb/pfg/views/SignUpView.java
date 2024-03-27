package com.abb.pfg.views;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import com.abb.pfg.frontend.commons.Constants;
import com.abb.pfg.utils.Countries;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.FlexComponent.JustifyContentMode;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility.Gap;

/**
 * 
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route("/signUp")
@PageTitle("J4S - Registro")
public class SignUpView extends Composite<VerticalLayout>{
	
	private static final long serialVersionUID = -9126111749350713592L;
	//Etiquetas
	private static final String HEADER_TAG = "Proceso de registro";
	private static final String CONFIRMPASS_TAG = "Repita la contraseña";
	private static final String REGISTER_TAG = "¿Cómo desea registrarse?";
	private static final String PROFILE_TAG = "Carga tu foto de perfil";
	private static final String LOGO_TAG = "Carga el logo de la empresa";
	private static final String CONFIRM_TAG = "Confirmar registro";
	private static final String PERS_TAG = "Descripción personal";
	private static final String ABOUT_TAG = "Acerca de la empresa";
	private static final String PHONE_TAG = "Teléfono de contacto";
	private static final String DROP_TAG = "Arrastra la imagen aquí";
	//Tamaños
	private static final String STRD_SIZE = "30%";
	private static final String MIN_SIZE = "10%"; 
	private static final int FIELDS_MAX_LENGTH = 50;
	private static final int PASSWD_MAX_LENGTH = 20;
	private static final int PASSWD_MIN_LENGTH = 12;
	private static final int PHONE_AND_ID_LENGHT = 9;
	private static final int MAX_FILE_SIZE = 2 * 1024 *1024;
	//Notificaciones o mensajes de error
	private static final String CONFIRM_TEXT = "Se ha registrado correctamente. Pruebe a iniciar sesión";
	private static final String PASSWD_ERROR = "Las contraseñas deben coincidir";
	private static final String PASSWD_LENGTH_ERROR = "La contraseña debe tener entre 12 y 20 caracteres";
	private static final String EMAIL_FORMAT_ERROR = "Correo electrónico no válido";
	private static final String EMAIL_ERROR = "Email no válido por estar ya en uso";
	private static final String CANCEL_TEXT = "Se ha cancelado el proceso de registro";
	private static final String SIGNUP_ERROR = "Algo falló durante el proceso de registro";
	private static final String ID_ERROR = "Introduzca un ID identificativo válido";
	private static final String DESC_LENGTH_ERROR = "Has superado la cantidad máxima de caracteres";
	private static final String PHONE_ERROR = "Introduzca un teléfono de contacto válido";
	private static final String PROF_PIC_ERROR = "La imagen cargada no cumple con las condiciones establecidas";
	private static final String UPLOAD_PIC_SUCC = "Imagen cargada correctamente";
	private static final String UPLOAD_PIC_ERR = "Error al cargar la imagen";
	//URLs
	private static final String GET_USER_REQ = "http://localhost:8081/auth/user?username=";
	private static final String GET_ROLE_REQ = "http://localhost:8081/auth/role?name=";
	private static final String SIGNUP_REQ = "http://localhost:8081/auth/signUp";
	private static final String GET_STDT_REQ = "http://localhost:8081/auth/student?dni=";
	private static final String GET_COMP_REQ = "http://localhost:8081/auth/company?cif=";
	private static final String POST_STDT_REQ = "http://localhost:8081/auth/student";
	private static final String POST_COMP_REQ = "http://localhost:8081/auth/company";
	//Otros
	private static final String DNI_REGEX = "[0-9]{8}[A-Za-z]";
	private static final String CIF_REGEX = "[A-Za-z][0-9]{8}";
	private static final String PHONE_REGEX = "[0-9]{9}";
	//Layout fields
	private VerticalLayout mainLayout, userDataLayout, studentOptionLayout, companyOptionLayout;
	private EmailField emailField;
	private PasswordField passwordField1, passwordField2;
	private RadioButtonGroup<String> radioButton;
	private HorizontalLayout buttonsLayout;
	private Button confirmButton, cancelButton;
	private boolean isStudentLayoutCreated = false;
	private boolean isCompanyLayoutCreated = false;
	private TextField nameField, dniField, studiesField, phoneField, cifField;
	private ComboBox<String> countryComboBox;
	private TextArea descriptionField;
	private MemoryBuffer memoryBuffer;
	private Upload profilePicture;
	private String picturePath;
	
	/**
	 * Default view class constructor
	 * 
	 */
	public SignUpView() {
		init();		//Inicializamos la vista y añadimos el layout principal
		getContent().add(new H1(HEADER_TAG), mainLayout);
		getContent().setAlignItems(Alignment.CENTER);
	}
	
	/**
	 * Initialices the view components
	 * 
	 */
	private void init() {
		mainLayout = new VerticalLayout();		//Inicializamos el layout principal
		mainLayout.setAlignItems(Alignment.CENTER);
		mainLayout.setWidthFull();
		getUserDataForm();						//Creamos layout de formulario de registro inicial
		mainLayout.add(userDataLayout);			//Añadimos layout del formulario inicial
	}
	
	/**
	 * Gets the user data form for authentication
	 * 
	 */
	private void getUserDataForm() {
		userDataLayout = new VerticalLayout();				//Inicializamos el layout del formulario inicial
		userDataLayout.setAlignItems(Alignment.CENTER);
		userDataLayout.setWidthFull();
		emailField = new EmailField(Constants.EMAIL_TAG);	//Creamos el campo del email para el registro
		emailField.setMaxLength(FIELDS_MAX_LENGTH);
		emailField.setClearButtonVisible(true);
		emailField.setPrefixComponent(VaadinIcon.ENVELOPE.create());
		emailField.setErrorMessage(EMAIL_FORMAT_ERROR);
		emailField.setWidth(STRD_SIZE);
		getPasswordFields();								//Creamos campos de passwords
		radioButton = new RadioButtonGroup<>();				//Creamos la multiopcion para el rol en el registro
		radioButton.setLabel(REGISTER_TAG);
		radioButton.setItems(Constants.STUDENT_TAG, Constants.COMPANY_TAG);
		radioButton.addValueChangeListener(event -> {
			radioButtonListener(radioButton.getOptionalValue());
		});
		userDataLayout.add(emailField, passwordField1, passwordField2, radioButton);
	}
	
	/**
	 * Gets the password fields for authentication
	 * 
	 */
	private void getPasswordFields() {
		passwordField1 = new PasswordField(Constants.PASSWORD_TAG);		//Creamos el campo de la password para el registro
		passwordField1.setMinLength(PASSWD_MIN_LENGTH);
		passwordField1.setMaxLength(PASSWD_MAX_LENGTH);
		passwordField1.setWidth(STRD_SIZE);
		passwordField1.setErrorMessage(PASSWD_LENGTH_ERROR);
		passwordField2 = new PasswordField(CONFIRMPASS_TAG);			//Creamos el campo de la repeticion de la password para el registro
		passwordField1.setMinLength(PASSWD_MIN_LENGTH);
		passwordField2.setMaxLength(PASSWD_MAX_LENGTH);
		passwordField2.setWidth(STRD_SIZE);
		passwordField2.addClientValidatedEventListener(event -> {
			var isInvalid = (passwordField2.getValue().equals(passwordField1.getValue())) ? false : true;
			passwordField2.setInvalid(isInvalid);
		});
		passwordField2.setErrorMessage(PASSWD_ERROR);
	}
	
	/**
	 * Performs the actions to set when radioButton is clicked
	 * 
	 */
	private void radioButtonListener(Optional<String> option) {
		switch (option.get()) {
			case Constants.STUDENT_TAG:
				handleStudentOption();
				break;
			case Constants.COMPANY_TAG:
				handleCompanyOption();
				break;
			default:
				showNotification(SIGNUP_ERROR, NotificationVariant.LUMO_PRIMARY);
				break;
		}
	}
	
	/**
	 * Manages the actions when student radio button is selected
	 * 
	 */
	private void handleStudentOption() {
		//Comprobamos si el layout de la opcion estudiante esta creado
		if(!isStudentLayoutCreated) {					//No esta creado, lo creamos
			getStudentOptionLayout();
			if(isCompanyLayoutCreated) {				//Si ya esta creado, es visible --> se oculta
				companyOptionLayout.setVisible(false);
			}
		} else {										//Si esta creado, lo hacemos visible
			if(companyOptionLayout != null) {
				companyOptionLayout.setVisible(false);
			}
			studentOptionLayout.setVisible(true);
		}
	}
	
	/**
	 * Manages the actions when company radio button is selected
	 * 
	 */
	private void handleCompanyOption() {
		//Comprobamos si el layout de la opcion empresa esta creado
		if(!isCompanyLayoutCreated) {				//No está creado, lo creamos
			this.getCompanyOptionLayout();
			if(isStudentLayoutCreated) {			//Si ya esta creado, es visible --> se oculta
				studentOptionLayout.setVisible(false);
			}
		} else {									//esta creado, lo hacemos visible
			if(studentOptionLayout != null) {
				studentOptionLayout.setVisible(false);
			}
			companyOptionLayout.setVisible(true);
		}
	}
	
	/**
	 * Enables the student option layout
	 * 
	 */
	private void getStudentOptionLayout() {
		studentOptionLayout = new VerticalLayout();				//Creamos el layout de la opcion estudiante
		studentOptionLayout.setAlignItems(Alignment.CENTER);
		studentOptionLayout.setWidthFull();
		nameField = new TextField(Constants.NAME_TAG);			//Creamos el campo del nombre
		nameField.setMaxLength(FIELDS_MAX_LENGTH);
		nameField.setWidth(STRD_SIZE);
		getDniField();											//Creamos el campo del DNI
		studiesField = new TextField(Constants.STUDIES_TAG);	//Creamos el campo de los estudios
		studiesField.setMaxLength(FIELDS_MAX_LENGTH);
		studiesField.setWidth(STRD_SIZE);
		getDescriptionTextArea(PERS_TAG);						//Creamos el campo de la descripcion personal
		getPhoneField();										//Creamos el campo del numero de telefono
		getProfilePictureField(PROFILE_TAG);					//Creamos el campo de carga de foto de perfil
		getButtonsLayout();										//Creamos el layout de los botones
		//Añadimos los componentes al layout
		studentOptionLayout.add(nameField, dniField, phoneField, studiesField, profilePicture, descriptionField, buttonsLayout);
		mainLayout.add(studentOptionLayout);					//Añadimos el layout a la vista principal
		isStudentLayoutCreated = true;							//Activamos la creacion del layout de la opcion de estudiante
	}
	
	/**
	 * Enables the company option layout
	 * 
	 */
	private void getCompanyOptionLayout() {
		companyOptionLayout = new VerticalLayout();				//Creamos el layout de la opcion empresa
		companyOptionLayout.setAlignItems(Alignment.CENTER);
		companyOptionLayout.setWidthFull();
		nameField = new TextField(Constants.NAME_TAG);			//Creamos el campo del nombre
		nameField.setWidth(STRD_SIZE);
		getCifField();											//Creamos el campo del CIF
		countryComboBox = new ComboBox<String>(Constants.COUNTRY_TAG);	//Creamos el campo de país de origen
		countryComboBox.setItems(Arrays.stream(Countries.values())
                .map(Countries::getCountryName)
                .collect(Collectors.toList()));
		countryComboBox.setWidth(STRD_SIZE);
		getProfilePictureField(LOGO_TAG);						//Creamos el campo de carga de foto de perfil
		getDescriptionTextArea(ABOUT_TAG);						//Creamos el campo de la descripcion personal
		getButtonsLayout();										//Creamos el layout de los botones
		//Añadimos los componentes al layout
		companyOptionLayout.add(nameField, cifField, countryComboBox, profilePicture, descriptionField, buttonsLayout);
		mainLayout.add(companyOptionLayout);					//Añadimos el layout a la vista principal
		isCompanyLayoutCreated = true;							//Activamos la creacion del layout de la opcion de empresa
	}
	
	/**
	 * Gets the DNI field to sign up a student
	 * 
	 */
	private void getDniField() {
		dniField = new TextField(Constants.DNI_TAG);
		dniField.setMinLength(PHONE_AND_ID_LENGHT);
		dniField.setMaxLength(PHONE_AND_ID_LENGHT);
		dniField.setErrorMessage(ID_ERROR);
		dniField.addClientValidatedEventListener(event -> {
			var dni = dniField.getValue();
			var isInvalid = (dni.matches(DNI_REGEX)) ? false : true;
			dniField.setInvalid(isInvalid);
		});
		dniField.setWidth(STRD_SIZE);
	}
	
	/**
	 * Gets the CIF field to sign up a company
	 * 
	 */
	private void getCifField() {
		cifField = new TextField(Constants.CIF_TAG);
		cifField.setMinLength(PHONE_AND_ID_LENGHT);
		cifField.setMaxLength(PHONE_AND_ID_LENGHT);
		cifField.setErrorMessage(ID_ERROR);
		cifField.addClientValidatedEventListener(event -> {
			var cif = cifField.getValue();
			var isInvalid = (cif.matches(CIF_REGEX)) ? false : true;
			cifField.setInvalid(isInvalid);
		});
		cifField.setWidth(STRD_SIZE);
	}
	
	/**
	 * Creates the text area depending on the option selected
	 * 
	 * @param option - student or company option selected
	 */
	private void getDescriptionTextArea(String option) {
		descriptionField = new TextArea(option);
		descriptionField.setWidth(STRD_SIZE);
		descriptionField.setValueChangeMode(ValueChangeMode.EAGER);
		descriptionField.setMaxLength(Constants.DESC_LENGTH);
		descriptionField.addValueChangeListener(e -> {
		    e.getSource()
		            .setHelperText(e.getValue().length() + "/" + Constants.DESC_LENGTH);
		    if(e.getValue().length() == Constants.DESC_LENGTH) {
		    	showNotification(DESC_LENGTH_ERROR, NotificationVariant.LUMO_PRIMARY);
		    }
		});
	}
	
	/**
	 * Gets the phone number field to sign up a student
	 * 
	 */
	private void getPhoneField() {
		phoneField = new TextField(PHONE_TAG);
		phoneField.setMaxLength(PHONE_AND_ID_LENGHT);
		phoneField.setErrorMessage(PHONE_ERROR);
		phoneField.addValueChangeListener(event -> {
			var phone = phoneField.getValue();
			var isInvalid = (phone.matches(PHONE_REGEX)) ? false : true;
			phoneField.setInvalid(isInvalid);
		});
		phoneField.setWidth(STRD_SIZE);
	}
	
	/**
	 * Gets the profile picture or logo upload field to sign up a student or a company
	 * 
	 * @param fieldTag - tag to show depending on the option selected
	 */
	private void getProfilePictureField(String fieldTag) {
		memoryBuffer = new MemoryBuffer();
		profilePicture = new Upload(memoryBuffer);
		profilePicture.setDropLabel(new Label(DROP_TAG));
		profilePicture.setAcceptedFileTypes(Constants.JPG, Constants.PNG);
		profilePicture.setUploadButton(new Button(fieldTag));
		profilePicture.setMaxFileSize(MAX_FILE_SIZE);
		profilePicture.addSucceededListener(eventUpload -> {			//Asignamos listener de imagen aceptada
			var fileData = memoryBuffer.getInputStream();
			var fileName = eventUpload.getFileName();
			var storePath = (fieldTag.equals(PROFILE_TAG)) ? Constants.STORED_PIC_PATH : Constants.STORED_LOGO_PATH;
			processImage(fileData, fileName, storePath);				//Procesar imagen
		});
		profilePicture.addFileRejectedListener(rejectEvent -> {			//Asignamos listener de imagen rechazada
			showNotification(PROF_PIC_ERROR, NotificationVariant.LUMO_ERROR);
		});
	}
	
	/**
	 * Process the uploaded image to
	 * 
	 * @param imageFile
	 * @param fileName
	 */
	private void processImage(InputStream imageFile, String fileName, String storePath) {
		picturePath = storePath + "\\" + fileName;
		try {
			var file = new File(picturePath);
			Files.copy(imageFile, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
			showNotification(UPLOAD_PIC_SUCC, NotificationVariant.LUMO_PRIMARY);
		} catch (IOException e) {
			showNotification(UPLOAD_PIC_ERR, NotificationVariant.LUMO_ERROR);
		}
	}
	
	/**
	 * Gets the layout with buttons needed to confirm or cancel the sign up process
	 * 
	 */
	private void getButtonsLayout() {
		buttonsLayout = new HorizontalLayout();				//Creamos el layout para los botones
		getContent().setFlexGrow(1.0, buttonsLayout);
		buttonsLayout.addClassName(Gap.MEDIUM);
		buttonsLayout.getStyle().set("flex-grow", "1");
		buttonsLayout.setAlignItems(Alignment.CENTER);
		buttonsLayout.setJustifyContentMode(JustifyContentMode.CENTER);
		buttonsLayout.setWidthFull();
		confirmButton = new Button(CONFIRM_TAG);			//Creamos boton de confirmar registro
		confirmButton.setWidth(MIN_SIZE);
		confirmButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		cancelButton = new Button(Constants.CANCEL_TAG);	//Creamos boton de cancelar registro
		cancelButton.setWidth(MIN_SIZE);
		cancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		setClickListenersToButtons();						//Asignamos listener a los botones
		buttonsLayout.add(confirmButton, cancelButton);		//Agregamos botones al layout
	}
	
	/**
	 * Performs the actions needed when buttons are clicked
	 * 
	 */
	private void setClickListenersToButtons() {
		confirmButton.addClickListener(event -> {
			if(emailField.isEmpty() || emailField.isInvalid() 
					|| passwordField1.isEmpty() || passwordField1.isInvalid()
					|| passwordField2.isEmpty() || passwordField2.isInvalid()
					|| !isOptionLayoutOk()) {	//Verificacion de campos de usuario
				showNotification(Constants.ERROR_TEXT, NotificationVariant.LUMO_ERROR);
			} else {	// Si primera verificacion OK:
				handleSignUpIfUserFieldsOk();
			}
		});
		cancelButton.addClickListener(cancelEvent -> {
			showNotification(CANCEL_TEXT, NotificationVariant.LUMO_PRIMARY);
			this.getUI().ifPresent(ui -> ui.navigate(Constants.LOGIN_PATH));
		});
	}
	
	/**
	 * Verifies if the fields of each user option are correctly comlpeted
	 * 
	 * @return boolean - true if fields OK, false if not
	 */
	private boolean isOptionLayoutOk() {
		var isStudentSelected = (radioButton.getOptionalValue().get().equals(Constants.STUDENT_TAG)) ? true : false;
		var fieldsOK = true;
		if(isStudentSelected) {		//Si opcion de estudiante seleccionada
			fieldsOK = (nameField.isEmpty() || dniField.isEmpty() || dniField.isInvalid()
					|| phoneField.isEmpty() || phoneField.isInvalid()
					|| studiesField.isEmpty()) ? false : true;
		} else {					//Si opcion de empresa seleccionada
			fieldsOK = (nameField.isEmpty() || cifField.isEmpty() || cifField.isInvalid() 
					|| countryComboBox.isEmpty()) ? false : true;
		}
		return fieldsOK;
	}
	
	/**
	 * Performs the sign up if the user fields are OK
	 * 
	 */
	private void handleSignUpIfUserFieldsOk() {
		var isStudentSelected = (radioButton.getOptionalValue().get().equals(Constants.STUDENT_TAG)) ? true : false;
		var id = (isStudentSelected) ? dniField.getValue() : cifField.getValue();
		if(isEmailUsedRequest(emailField.getOptionalValue().get()) 
				|| isDniOrCifUsedRequest(isStudentSelected, id)) { 				//Si email o DNI/CIF están en uso, se notifica al usuario
			showNotification(EMAIL_ERROR, NotificationVariant.LUMO_PRIMARY);
		} else { 																//Si no existen, procedo a realizar peticion de registro
			if(signUp()) {														//Registro correcto, aviso a usuario
				var signUpResult = createStudentOrCompany(isStudentSelected);
				if(signUpResult) { 												//Si registro correcto
					showNotification(CONFIRM_TEXT, NotificationVariant.LUMO_SUCCESS);
				} else {														//Si registro fallido
					showNotification(SIGNUP_ERROR, NotificationVariant.LUMO_ERROR);
				}
			} else {															//Fallo en el registro, aviso a usuario
				showNotification(SIGNUP_ERROR, NotificationVariant.LUMO_ERROR);
			}
			this.getUI().ifPresent(ui -> ui.navigate(Constants.LOGIN_PATH));	//Volvemos a pagina de inicio tanto si ha ido bien como si ha ido mal
		}
	}
	
	/**
	 * Performs the user sign up
	 * 
	 * @return boolean - true if 201 CREATED, false if not
	 */
	private boolean signUp() {
		var username = emailField.getOptionalValue().get();			//Extraemos valores necesarios para el registro
		var password = passwordField1.getOptionalValue().get();
		var role = parseRoles(radioButton.getOptionalValue().get());
		return sendSignUpRequest(username, password, role);				//Ejecutamos peticion de registro
	}
	
	/**
	 * Performs the signing up of the student or company depending on the param value
	 * 
	 * @param isStudent - true if it has to create a student, false if it has to create a company
	 * @return boolean - true if 201 CREATED, false if not
	 */
	private boolean createStudentOrCompany(boolean isStudent) {
		var isOptionSignUpOk = true;
		if(isStudent) {																	//Si es estudiante
			isOptionSignUpOk = sendSignUpStudentRequest(nameField.getValue(), 			//Tramitar datos de estudiante
					dniField.getValue(), phoneField.getValue(), studiesField.getValue(), 
					descriptionField.getValue(), emailField.getValue());
		} else {																		//Si es empresa
			isOptionSignUpOk = sendSignUpCompanyRequest(nameField.getValue(), 			//Tramitar datos de empresa
					cifField.getValue(), countryComboBox.getOptionalValue().get(),descriptionField.getValue(), 
					emailField.getValue());
		}
		return isOptionSignUpOk;
	}
	
	/**
	 * Sends a request to verify if a user exists or not
	 * 
	 * @param email - user email to verify
	 * @return HttpResponse - request result
	 */
	private HttpResponse sendGetUserRequest(String email) {
		var httpClient = HttpClients.createDefault();			//Peticion a url correspondiente para obtener el usuario
		var urlGetUser = GET_USER_REQ + email;
		var httpGet = new HttpGet(urlGetUser);
		try {													//Ejecutamos peticion GET
			var httpResponse = httpClient.execute(httpGet);
			return httpResponse;
		} catch (IOException e) {
			return null;
		}
	}
	
	/**
	 * Verifies if the email gived is already in use
	 * 
	 * @param email - email to verify
	 * @return boolean - true if exists, false if not
	 */
	private boolean isEmailUsedRequest(String email) {
		try {
			var httpResponse = sendGetUserRequest(email);					//Peticion a url correspondiente para verificar si existe el email
			var httpStatus = httpResponse.getStatusLine().getStatusCode();
			//Verificar si peticion exitosa o fallida
			if(httpStatus == HttpStatus.SC_NOT_FOUND) {						//NOT FOUND, no está usado
				return false;	
			}
			return true;													//Cualquier otra respuesta, cancelamos proceso de registro
		} catch (NullPointerException e) {
			return true;
		}
	}
	
	/**
	 * Sends the request to get a specific role object
	 * 
	 * @param role - role to get
	 * @return String - role JSON object requested
	 */
	private String sendGetRoleRequest(String role) {
		var httpClient = HttpClients.createDefault();					//Peticion para obtener el rol
		var urlgetRole = GET_ROLE_REQ + role;
		var httpGet = new HttpGet(urlgetRole);
		try {															//Ejecutamos peticion
			var httpResponse = httpClient.execute(httpGet);
			var httpStatus = httpResponse.getStatusLine().getStatusCode();
			if(httpStatus == HttpStatus.SC_OK) {						//OK, extraemos objeto Rol
				return EntityUtils.toString(httpResponse.getEntity());
			}
			return null;											//Resto, devolvemos NULL
		} catch (IOException | NullPointerException e) {
			return null;
		}
	}
	
	/**
	 * Sends the user sign up request
	 * 
	 * @param email - user email
	 * @param password - user password
	 * @param role - user role
	 * @return boolean - true if 201 CREATED, false if not
	 */
	private boolean sendSignUpRequest(String email, String password, String role) {
		var requestBody = createJSONObjectUser(email, password, role);	//Formamos el objeto JSON del usuarios
		var httpClient = HttpClients.createDefault();					//Peticion para obtener el objeto rol
		var urlSignUp = SIGNUP_REQ;
		var httpPost = new HttpPost(urlSignUp);
		//Construimos la cabecera de la petición POST
		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		try {
			var requestEntity = new StringEntity(requestBody);			//Construimos el cuerpo de la peticion POST
			httpPost.setEntity(requestEntity);
			var httpResponse = httpClient.execute(httpPost);			//Ejecutamos peticion POST
			var httpStatus = httpResponse.getStatusLine().getStatusCode();
			if(httpStatus == HttpStatus.SC_CREATED) {					//Si CREATED, devolvemos true
				return true;
			}
			return false;												//Resto, devolvemos false;
		} catch (IOException | NullPointerException e) {
			return false;
		}
	}
	
	/**
	 * Verifies if the DNI or the CIF given as parameter already exists
	 * 
	 * @param isStudent - true if it has to verify a DNI, false if it has to verify a CIF
	 * @param idCode - DNI value or CIF value
	 * @return true if the identification code exists, false if not
	 */
	private boolean isDniOrCifUsedRequest(boolean isStudent, String idCode) {
		var httpClient = HttpClients.createDefault();	//Peticion a url correspondiente para verificar si existe el dni
		var urlIsIdCodeUsed = (isStudent) ? GET_STDT_REQ + idCode : GET_COMP_REQ + idCode;
		var httpGet = new HttpGet(urlIsIdCodeUsed);
		try {											//Ejecutamos la peticion GET
			var httpResponse = httpClient.execute(httpGet);
			var httpStatus = httpResponse.getStatusLine().getStatusCode();
			//Verificar si peticion exitosa o fallida
			if(httpStatus == HttpStatus.SC_NOT_FOUND) {	//NOT FOUND, no está usado
				return false;	
			}
			return true;								//Cualquier otra respuesta, cancelamos proceso de registro
		} catch (IOException | NullPointerException e) {
			return true;
		}
	}
	
	/**
	 * Sends the POST request to create a student
	 * 
	 * @param name - student name
	 * @param dni - student DNI
	 * @param phone - student phone number
	 * @param studies - student studies
	 * @param description - student description
	 * @param email - student username
	 * @return boolean - true if 201 CREATED, false if not
	 */
	private boolean sendSignUpStudentRequest(String name, String dni, String phone, 
			String studies, String description, String email) {
		var userEntity = new String();
		try {	//Peticion para obtener el objeto usuario
			userEntity = EntityUtils.toString(sendGetUserRequest(email).getEntity());
		} catch (ParseException | IOException e) {
			return false;
		}
		//Formamos el objeto JSON del usuario
		var finalPicturePath = (memoryBuffer.getFileName().isEmpty()) ? "" : picturePath;
		var requestBody = createJSONObjectStudent(name, dni, phone, studies, description, finalPicturePath, userEntity);
		//Construimos la petición POST y la ejecutamos
		return executeSignUpStudentOrCompanyRequest(POST_STDT_REQ, requestBody);
	}
	
	/**
	 * Sends the POST request to create a Company
	 * 
	 * @param name - company name
	 * @param cif - company CIF
	 * @param country - company origin country
	 * @param description - company description
	 * @param email - company user email
	 * @return boolean - true if completed, false if not
	 */
	private boolean sendSignUpCompanyRequest(String name, String cif, String country, String description, String email) {
		var userEntity = new String();
		try {
			userEntity = EntityUtils.toString((sendGetUserRequest(email).getEntity()));
		} catch (ParseException | IOException e) {
			return false;
		}
		//Formamos el objeto JSON del usuario
		var finalPicturePath = (memoryBuffer.getFileName().isEmpty()) ? "" : picturePath;
		var requestBody = createJSONObjectCompany(name, cif, country, description, finalPicturePath, userEntity);
		//Construimos la petición POST y la ejecutamos
		return executeSignUpStudentOrCompanyRequest(POST_COMP_REQ, requestBody);
	}
	
	/**
	 * Executes POST request to create a Student or a Company
	 * 
	 * @param url - POST request URL to create the Student or Company
	 * @param requestBody - required body request to complete POST request
	 * @return boolean - true if 201 CREATED, false if not
	 */
	private boolean executeSignUpStudentOrCompanyRequest(String url, String requestBody) {
		var httpClient = HttpClients.createDefault();				//Construimos la petición POST
		var httpPost = new HttpPost(url);
		//Construimos la cabecera de la petición POST
		httpPost.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
		try {
			var requestEntity = new StringEntity(requestBody);		//Construimos el cuerpo de la peticion POST
			httpPost.setEntity(requestEntity);
			var httpResponse = httpClient.execute(httpPost);		//Ejecutamos peticion POST
			var httpStatus = httpResponse.getStatusLine().getStatusCode();
			if(httpStatus == HttpStatus.SC_CREATED) {				//Si CREATED, devolvemos true
				return true;
			}
			return false;											//Resto, devolvemos false;
		} catch (IOException | NullPointerException e) {			//Cualquier error, devuelve false
			return false;
		}
	}
	
	/**
	 * Creates a UserDto JSON object from its parameters
	 * 
	 * @param email - user email
	 * @param password - user password
	 * @param role - user role
	 * @return String - UserDto JSON object
	 */
	private String createJSONObjectUser(String email, String password, String role) {
		var jsonUser = new JSONObject();
		jsonUser.put("username", email);
		jsonUser.put("password", password);
		var jsonRole = new JSONObject(sendGetRoleRequest(role));
		jsonUser.put("role", jsonRole);
		return jsonUser.toString();
	}
	
	/**
	 * Creates a StudentDto JSON object from its parameters
	 * 
	 * @param name - student name
	 * @param dni - studnet DNI
	 * @param phoneNumber - student phone number
	 * @param studies - student studies
	 * @param description - student description
	 * @param user - student user
	 * @return String - StudentDto JSON object
	 */
	private String createJSONObjectStudent(String name, String dni, String phoneNumber,
			String studies, String description, String picturePath, String user) {
		var jsonStudent = new JSONObject();
		jsonStudent.put("name", name);
		jsonStudent.put("dni", dni);
		jsonStudent.put("description", description);
		jsonStudent.put("studies", studies);
		jsonStudent.put("phoneNumber", phoneNumber);
		jsonStudent.put("profilePicture", picturePath);
		var jsonUser = new JSONObject(user);
		jsonStudent.put("user", jsonUser);
		return jsonStudent.toString();
	}
	
	/**
	 * Creates a CompanyDto JSON object from its parameters
	 * 
	 * @param name - company name
	 * @param cif - company CIF
	 * @param country - company origin country
	 * @param description - company description
	 * @param user - company user
	 * @return String - CompanyDTO JSON object
	 */
	private String createJSONObjectCompany(String name, String cif, String country, 
			String description, String logo, String user) {
		var jsonCompany = new JSONObject();
		jsonCompany.put("name", name);
		jsonCompany.put("cif", cif);
		jsonCompany.put("country", countryComboBox.getOptionalValue().get());
		jsonCompany.put("description", description);
		jsonCompany.put("logo", logo);
		var jsonUser = new JSONObject(user);
		jsonCompany.put("user", jsonUser);
		return jsonCompany.toString();
	}
		
	/**
	 * Parses the type of user to the role that is going to have
	 * 
	 * @param role - type of user selected
	 * @return String - the role the user is going to have
	 */
	private String parseRoles(String role) {
		switch(role) {
		 	case Constants.STUDENT_TAG:
		 		return "STUDENT";
		 	case Constants.COMPANY_TAG:
		 		return "COMPANY";
		 	default:
		 		return null;
		}
	}
	
	/**
	 * Shows a notification from the text and the theme provided
	 * 
	 * @param text - text to show in the notification
	 * @param notificationVariant - theme to customize the notification
	 */
	private void showNotification(String text, NotificationVariant notificationVariant) {
		var notification = new Notification(text);
		notification.setPosition(Position.TOP_CENTER);
		notification.setDuration(Constants.NOTIF_DURATION);
		notification.addThemeVariants(notificationVariant);
		notification.open();
	}
}
