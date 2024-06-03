package com.abb.pfg.views;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.abb.pfg.custom.CustomNotification;
import com.abb.pfg.custom.CustomSignUpCompanyLayout;
import com.abb.pfg.custom.CustomSignUpStudentLayout;
import com.abb.pfg.custom.CustomSignUpUserLayout;
import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.HttpRequest;
import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import lombok.NoArgsConstructor;

/**
 * View used to sign up the new users
 *
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route(Constants.SIGNUP_PATH)
@PageTitle("J4S - Registro")
@NoArgsConstructor
public class SignUpView extends Composite<VerticalLayout> implements BeforeEnterObserver{

	private static final long serialVersionUID = -9126111749350713592L;
	//Etiquetas
	private static final String HEADER_TAG = "Proceso de registro";
	//Notificaciones o mensajes de error
	private static final String CANCEL_TEXT = "Se ha cancelado el proceso de registro";
	//Layout fields
	private VerticalLayout mainLayout, userDataLayout;
	private CustomSignUpUserLayout signUpUserLayout;
	private CustomSignUpStudentLayout signUpStudentLayout;
	private CustomSignUpCompanyLayout signUpCompanyLayout;
	private RadioButtonGroup<String> radioButton;
	private Button tempCancelButton;
	private boolean isStudentLayoutCreated = false;
	private boolean isCompanyLayoutCreated = false;

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		init();
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
		getContent().add(new H1(HEADER_TAG), mainLayout);
		getContent().setAlignItems(Alignment.CENTER);
	}

	/**
	 * Gets the user data form for authentication
	 *
	 */
	private void getUserDataForm() {
		userDataLayout = new VerticalLayout();				//Inicializamos el layout del formulario inicial
		userDataLayout.setAlignItems(Alignment.CENTER);
		signUpUserLayout = new CustomSignUpUserLayout(Constants.STD_ROLE);
		addListenerToPasswordFields();
		radioButton = new RadioButtonGroup<>();				//Creamos la multiopcion para el rol en el registro
		radioButton.setLabel(Constants.REGISTER_TAG);
		radioButton.setItems(Constants.STUDENT_TAG, Constants.COMPANY_TAG);
		radioButton.addValueChangeListener(event -> {
			radioButtonListener(radioButton.getOptionalValue());
		});
		tempCancelButton = new Button(Constants.CANCEL_TAG);
		tempCancelButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		this.tempCancelButton.addClickListener(cancelEvent -> 
			this.getUI().ifPresent(ui -> ui.navigate(Constants.LOGIN_PATH)));
		userDataLayout.add(signUpUserLayout, radioButton, tempCancelButton);
	}
	
	/**
	 * Gets the password fields for authentication
	 *
	 */
	private void addListenerToPasswordFields() {
		signUpUserLayout.getPasswordField2().addClientValidatedEventListener(event -> {
			var isInvalid = (signUpUserLayout.getPasswordField2().getValue().equals(signUpUserLayout.getPasswordField1().getValue())) ? false : true;
			signUpUserLayout.getPasswordField2().setInvalid(isInvalid);
		});
		signUpUserLayout.getPasswordField2().setErrorMessage(Constants.PASSWD_ERROR);
	}
	
	/**
	 * Performs the actions to set when radioButton is clicked
	 *
	 */
	private void radioButtonListener(Optional<String> option) {
		userDataLayout.remove(this.tempCancelButton);
		var selectedOption = option.get();
		if(selectedOption.equals(Constants.STUDENT_TAG)) {
			handleStudentOption(selectedOption);
		} else if(selectedOption.equals(Constants.COMPANY_TAG)) {
			handleCompanyOption(selectedOption);
		}
	}
	
	/**
	 * Manages the actions when student radio button is selected
	 *
	 */
	private void handleStudentOption(String selectedOption) {
		//Comprobamos si el layout de la opcion estudiante esta creado
		if(!isStudentLayoutCreated) {					//No esta creado, lo creamos
			getStudentOptionLayout(selectedOption);
			if(isCompanyLayoutCreated) {				//Si ya esta creado, es visible --> se oculta
				signUpCompanyLayout.setVisible(false);
			}
		} else {										//Si esta creado, lo hacemos visible
			if(signUpCompanyLayout != null) {
				signUpCompanyLayout.setVisible(false);
			}
			signUpStudentLayout.setVisible(true);
		}
	}

	/**
	 * Manages the actions when company radio button is selected
	 *
	 */
	private void handleCompanyOption(String selectedOption) {
		//Comprobamos si el layout de la opcion empresa esta creado
		if(!isCompanyLayoutCreated) {				//No está creado, lo creamos
			this.getCompanyOptionLayout(selectedOption);
			if(isStudentLayoutCreated) {			//Si ya esta creado, es visible --> se oculta
				signUpStudentLayout.setVisible(false);
			}
		} else {									//esta creado, lo hacemos visible
			if(signUpStudentLayout != null) {
				signUpStudentLayout.setVisible(false);
			}
			signUpCompanyLayout.setVisible(true);
		}
	}

	/**
	 * Enables the student option layout
	 *
	 */
	private void getStudentOptionLayout(String selectedOption) {
		signUpStudentLayout = new CustomSignUpStudentLayout();
		setClickListenersToButtons(selectedOption);
		mainLayout.add(signUpStudentLayout);					//Añadimos el layout a la vista principal
		isStudentLayoutCreated = true;							//Activamos la creacion del layout de la opcion de estudiante
	}

	/**
	 * Enables the company option layout
	 *
	 */
	private void getCompanyOptionLayout(String selectedOption) {
		signUpCompanyLayout = new CustomSignUpCompanyLayout();
		setClickListenersToButtons(selectedOption);	
		mainLayout.add(signUpCompanyLayout);					//Añadimos el layout a la vista principal
		isCompanyLayoutCreated = true;							//Activamos la creacion del layout de la opcion de empresa
	}

	/**
	 * Process the uploaded image to save it
	 *
	 * @param imageFile - picture file
	 * @param fileName - picture file name
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
	 * Performs the actions needed when buttons are clicked
	 *
	 */
	private void setClickListenersToButtons(String selectedOption) {
		if(selectedOption.equals(Constants.STUDENT_TAG)) {
			signUpStudentLayout.getConfirmButton().addClickListener(event -> setConfirmButtonListener(selectedOption));
			signUpStudentLayout.getCancelButton().addClickListener(cancelEvent -> {
				new CustomNotification(CANCEL_TEXT, NotificationVariant.LUMO_PRIMARY);
				this.getUI().ifPresent(ui -> ui.navigate(Constants.LOGIN_PATH));
			});
			return;
		}
		signUpCompanyLayout.getConfirmButton().addClickListener(event -> setConfirmButtonListener(selectedOption));
		signUpCompanyLayout.getCancelButton().addClickListener(cancelEvent -> {
			new CustomNotification(CANCEL_TEXT, NotificationVariant.LUMO_PRIMARY);
			this.getUI().ifPresent(ui -> ui.navigate(Constants.LOGIN_PATH));
		});
	}
	
	/**
	 * Listener assigned to the confirm sign up button
	 * 
	 * @param selectedOption - option selected on the radio button
	 */
	private void setConfirmButtonListener(String selectedOption) {
		if(signUpUserLayout.getEmailField().isEmpty() || signUpUserLayout.getEmailField().isInvalid()
			|| signUpUserLayout.getPasswordField1().isEmpty() || signUpUserLayout.getPasswordField1().isInvalid()
			|| signUpUserLayout.getPasswordField2().isEmpty() || signUpUserLayout.getPasswordField2().isInvalid()
			|| !isOptionLayoutOk(selectedOption)) {	//Verificacion de campos de usuario
			new CustomNotification(Constants.ERROR_TEXT, NotificationVariant.LUMO_ERROR);
		} else {	// Si primera verificacion OK:
			handleSignUpIfUserFieldsOk(selectedOption);
		}
	}

	/**
	 * Verifies if the fields of each user option are correctly comlpeted
	 *
	 * @return boolean - true if fields OK, false if not
	 */
	private boolean isOptionLayoutOk(String selectedOption) {
		var isStudentSelected = (selectedOption.equals(Constants.STUDENT_TAG)) ? true : false;
		var fieldsOK = true;
		if(isStudentSelected) {		//Si opcion de estudiante seleccionada
			fieldsOK = (signUpStudentLayout.getNameField().isEmpty() 
					|| signUpStudentLayout.getDniField().isEmpty() 
					|| signUpStudentLayout.getDniField().isInvalid() 
					|| signUpStudentLayout.getPhoneField().isEmpty()
					|| signUpStudentLayout.getPhoneField().isInvalid() 
					|| signUpStudentLayout.getStudiesField().isEmpty()) ? false : true;
		} else {					//Si opcion de empresa seleccionada
			fieldsOK = (signUpCompanyLayout.getNameField().isEmpty() 
					|| signUpCompanyLayout.getCifField().isEmpty() 
					|| signUpCompanyLayout.getCifField().isInvalid() 
					|| signUpCompanyLayout.getCountryComboBox().isEmpty() 
					|| signUpCompanyLayout.getMemoryBuffer().getFileName().isEmpty()) ? false : true;
		}
		return fieldsOK;
	}

	/**
	 * Performs the sign up if the user fields are OK
	 *
	 */
	private void handleSignUpIfUserFieldsOk(String selectedOption) {
		var id = (selectedOption.equals(Constants.STUDENT_TAG)) ? signUpStudentLayout.getDniField().getValue() 
				: signUpCompanyLayout.getCifField().getValue();
		if(isEmailUsedRequest(signUpUserLayout.getEmailField().getOptionalValue().get())
				|| isDniOrCifUsedRequest(selectedOption, id)) { 				//Si email o DNI/CIF están en uso, se notifica al usuario
			new CustomNotification(Constants.EMAIL_ERROR, NotificationVariant.LUMO_PRIMARY);
		} else { 																//Si no existen, procedo a realizar peticion de registro
			if(signUp()) {														//Registro correcto, aviso a usuario
				var signUpResult = createStudentOrCompany(selectedOption);
				if(signUpResult) { 												//Si registro correcto
					new CustomNotification(Constants.SIGN_UP_SUCC, NotificationVariant.LUMO_SUCCESS);
				} else {														//Si registro fallido
					new CustomNotification(Constants.SIGNUP_ERROR, NotificationVariant.LUMO_ERROR);
				}
			} else {															//Fallo en el registro, aviso a usuario
				new CustomNotification(Constants.SIGNUP_ERROR, NotificationVariant.LUMO_ERROR);
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
		var username = signUpUserLayout.getEmailField().getOptionalValue().get();			//Extraemos valores necesarios para el registro
		var password = signUpUserLayout.getPasswordField1().getOptionalValue().get();
		var role = parseRoles(radioButton.getOptionalValue().get());
		return sendSignUpRequest(username, password, role);				//Ejecutamos peticion de registro
	}

	/**
	 * Performs the signing up of the student or company depending on the param value
	 *
	 * @param isStudent - true if it has to create a student, false if it has to create a company
	 * @return boolean - true if 201 CREATED, false if not
	 */
	private boolean createStudentOrCompany(String selectedOption) {
		var isOptionSignUpOk = true;
		if(selectedOption.equals(Constants.STUDENT_TAG)) {																	//Si es estudiante
			isOptionSignUpOk = sendSignUpStudentRequest(signUpStudentLayout.getNameField().getValue(), 			//Tramitar datos de estudiante
					signUpStudentLayout.getDniField().getValue(), signUpStudentLayout.getPhoneField().getValue(), 
					signUpStudentLayout.getStudiesField().getValue(), signUpStudentLayout.getDescriptionField().getValue(), 
					signUpUserLayout.getEmailField().getValue());
		} else {																		//Si es empresa
			isOptionSignUpOk = sendSignUpCompanyRequest(signUpCompanyLayout.getNameField().getValue(), 			//Tramitar datos de empresa
					signUpCompanyLayout.getCifField().getValue(), signUpCompanyLayout.getCountryComboBox().getOptionalValue().get(),
					signUpCompanyLayout.getDescriptionField().getValue(), signUpUserLayout.getEmailField().getValue());
		}
		return isOptionSignUpOk;
	}

	/**
	 * Sends a request to verify if a user exists or not
	 *
	 * @param email - user email to verify
	 * @return HttpResponse - request result
	 */
	private String sendGetUserRequest(String email) {
		var httpRequest = new HttpRequest(Constants.AUTH_REQ + "/user?username=" + email);
		return httpRequest.executeHttpGet(null);
	}

	/**
	 * Verifies if the email gived is already in use
	 *
	 * @param email - email to verify
	 * @return boolean - true if exists, false if not
	 */
	private boolean isEmailUsedRequest(String email) {
		var responseBody = sendGetUserRequest(email);
		if(responseBody.isEmpty()) {
			return false;
		}
		return true;
	}

	/**
	 * Sends the request to get a specific role object
	 *
	 * @param role - role to get
	 * @return String - role JSON object requested
	 */
	private String sendGetRoleRequest(String role) {
		var httpClient = HttpClients.createDefault();					//Peticion para obtener el rol
		var urlgetRole = Constants.AUTH_REQ + "/role?name=" + role;
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
		var httpRequest = new HttpRequest(Constants.AUTH_REQ + "/signUp");
		var requestBody = createJSONObjectUser(email, password, role);	//Formamos el objeto JSON del usuarios
		return httpRequest.executeHttpPost(null, requestBody);
	}

	/**
	 * Verifies if the DNI or the CIF given as parameter already exists
	 *
	 * @param isStudent - true if it has to verify a DNI, false if it has to verify a CIF
	 * @param idCode - DNI value or CIF value
	 * @return true if the identification code exists, false if not
	 */
	private boolean isDniOrCifUsedRequest(String selectedOption, String idCode) {
		var getUrl = (selectedOption.equals(Constants.STUDENT_TAG)) ? Constants.AUTH_REQ + "/student?dni=" + idCode 
				: Constants.AUTH_REQ + "/company?cif=" + idCode;
		var httpRequest = new HttpRequest(getUrl);
		var responseBody = httpRequest.executeHttpGet(null);
		if(responseBody.isEmpty()) {
			return false;
		}
		return true;
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
		var responseBody = sendGetUserRequest(email);
		//Formamos el objeto JSON del usuario
		var picName = signUpStudentLayout.getMemoryBuffer().getFileName();
		if(!picName.isEmpty()) {
			processImage(signUpStudentLayout.getMemoryBuffer().getInputStream(), picName);
		}
		var requestBody = createJSONObjectStudent(name, dni, phone, studies, description, picName, responseBody);
		//Construimos la petición POST y la ejecutamos
		return executeSignUpStudentOrCompanyRequest(Constants.AUTH_REQ + "/student", requestBody);
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
		var userEntity = sendGetUserRequest(email);
		var logoName = signUpCompanyLayout.getMemoryBuffer().getFileName();
		processImage(signUpCompanyLayout.getMemoryBuffer().getInputStream(), logoName);
		//Formamos el objeto JSON del usuario
		var requestBody = createJSONObjectCompany(name, cif, country, description, logoName, userEntity);
		//Construimos la petición POST y la ejecutamos
		return executeSignUpStudentOrCompanyRequest(Constants.AUTH_REQ + "/company", requestBody);
	}

	/**
	 * Executes POST request to create a Student or a Company
	 *
	 * @param url - POST request URL to create the Student or Company
	 * @param requestBody - required body request to complete POST request
	 * @return boolean - true if 201 CREATED, false if not
	 */
	private boolean executeSignUpStudentOrCompanyRequest(String url, String requestBody) {
		var httpRequest = new HttpRequest(url);
		return httpRequest.executeHttpPost(null, requestBody);
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
	 * @param logo - company's logo name
	 * @param user - company user
	 * @return String - CompanyDTO JSON object
	 */
	private String createJSONObjectCompany(String name, String cif, String country,
			String description, String logo, String user) {
		var jsonCompany = new JSONObject();
		jsonCompany.put("name", name);
		jsonCompany.put("cif", cif);
		jsonCompany.put("country", country);
		jsonCompany.put("description", description);
		jsonCompany.put("profilePicture", logo);
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
		 		return Constants.STD_ROLE;
		 	case Constants.COMPANY_TAG:
		 		return Constants.CMP_ROLE;
		 	default:
		 		return null;
		}
	}
}