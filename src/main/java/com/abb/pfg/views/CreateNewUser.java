
package com.abb.pfg.views;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.json.JSONObject;

import com.abb.pfg.custom.CustomAppLayout;
import com.abb.pfg.custom.CustomNotification;
import com.abb.pfg.custom.CustomSignUpCompanyLayout;
import com.abb.pfg.custom.CustomSignUpStudentLayout;
import com.abb.pfg.custom.CustomSignUpUserLayout;
import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.HttpRequest;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

/**
 * View which allow administrators to create new users
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route(Constants.CREATE_USER_PATH)
@PageTitle("J4S - Gestionar usuarios")
public class CreateNewUser extends CustomAppLayout implements HasUrlParameter<String>, BeforeEnterObserver{

	private static final long serialVersionUID = -2074592320666001568L;
	//Etiquetas
	private static final String CRE_TAG = "Crear ";
	private static final String USR_CRE_MSG = "Usuario creado correctamente";
	private static final String USR_CRE_ERR = "No se ha podido crear el usuario";
	//Componentes
	private VerticalLayout mainLayout;
	private CustomSignUpUserLayout signUpUserLayout;
	private CustomSignUpStudentLayout signUpStudentLayout;
	private CustomSignUpCompanyLayout signUpCompanyLayout;
	//Atributos
	private String userCategory, userRole;

	@Override
	public void setParameter(BeforeEvent event, String parameter) {
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		if(authToken == null) {
			event.forwardTo(LoginView.class);
			return;
		}
		userRole = (String) VaadinSession.getCurrent().getAttribute("role");
		if(!userRole.equals(Constants.ADM_ROLE) || parameter == null) {
			new CustomNotification(Constants.ACC_REJ_MSG, NotificationVariant.LUMO_ERROR);
			event.forwardTo(LoginView.class);
			return;
		}
		userCategory = parameter;
	}
	
	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		init();
	}
	
	/**
	 * Initializes all view components
	 * 
	 */
	private void init() {
		mainLayout = new VerticalLayout();		//Inicializamos el layout principal
		mainLayout.setAlignItems(Alignment.CENTER);
		mainLayout.setWidthFull();
		setContentLayout(userCategory);
		var baseVerticalLayout = new VerticalLayout();
		baseVerticalLayout.add(new H1(CRE_TAG + getHeaderTag(userCategory)), mainLayout);
		baseVerticalLayout.setAlignItems(Alignment.CENTER);
		this.setContent(baseVerticalLayout);
	}
	
	/**
	 * Sets up the content of the view main layout
	 * 
	 * @param userCategory - user's role to create
	 */
	private void setContentLayout(String userCategory) {
		signUpUserLayout = new CustomSignUpUserLayout(userCategory);
		addListenerToPasswordFields();
		mainLayout.add(signUpUserLayout);
		if(userCategory.equals(Constants.STD_ROLE)) {
			signUpStudentLayout = new CustomSignUpStudentLayout();
			mainLayout.add(signUpStudentLayout);
		} else if (userCategory.equals(Constants.CMP_ROLE)) {
			signUpCompanyLayout = new CustomSignUpCompanyLayout();
			mainLayout.add(signUpCompanyLayout);
		}
		setClickListenersToButtons(userCategory);
	}
	
	/**
	 * Adds validation listeners to passwords fields
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
	 * Performs the actions needed when buttons are clicked
	 *
	 */
	private void setClickListenersToButtons(String userCategory) {
		switch(userCategory) {
			case Constants.STD_ROLE:
				signUpStudentLayout.getConfirmButton().addClickListener(event -> setConfirmButtonListener(userCategory));
				signUpStudentLayout.getCancelButton().addClickListener(cancelEvent -> 
					this.getUI().ifPresent(ui -> ui.navigate(Constants.MNG_USERS_PATH)));
				break;
			case Constants.CMP_ROLE:
				signUpCompanyLayout.getConfirmButton().addClickListener(event -> setConfirmButtonListener(userCategory));
				signUpCompanyLayout.getCancelButton().addClickListener(cancelEvent -> 
					this.getUI().ifPresent(ui -> ui.navigate(Constants.MNG_USERS_PATH)));
				break;
			default: 
				signUpUserLayout.getSaveButton().addClickListener(event -> setConfirmButtonListener(userCategory));
				signUpUserLayout.getCancelButton().addClickListener(cancelEvent -> 
					this.getUI().ifPresent(ui -> ui.navigate(Constants.MNG_USERS_PATH)));
		}
	}
	
	/**
	 * Listener assigned to the confirm user creation button
	 * 
	 * @param userCategory - user's role to create
	 */
	private void setConfirmButtonListener(String userCategory) {
		if(userCategory.equals(Constants.ADM_ROLE)) {
			if(areUserFieldsOK()) {
				handleSignUpIfUserFieldsOk(userCategory);
				return;
			}
		} else {
			if(areUserFieldsOK() && areFieldsCategoryOK(userCategory)) {
				handleSignUpIfUserFieldsOk(userCategory);
				return;
			}
		}
		new CustomNotification(Constants.ERROR_TEXT, NotificationVariant.LUMO_ERROR);
	}
	
	/**
	 * Verifies if the user authentication fields are OK to set up the sign up
	 * 
	 * @return boolean - true if fields are OK, false if not
	 */
	private boolean areUserFieldsOK() {
		if(signUpUserLayout.getEmailField().isEmpty()
				|| signUpUserLayout.getEmailField().isInvalid()
				|| isEmailUsedRequest(signUpUserLayout.getEmailField().getOptionalValue().get())
				|| signUpUserLayout.getPasswordField1().isEmpty()
				|| signUpUserLayout.getPasswordField1().isInvalid()
				|| signUpUserLayout.getPasswordField2().isEmpty()
				|| signUpUserLayout.getPasswordField2().isInvalid()) {
			return false;
		}
		return true;
	}
	
	/**
	 * Verifies if the fields of each user option are correctly comlpeted
	 *
	 * @param userCategory - user's role to create
	 * @return boolean - true if fields OK, false if not
	 */
	private boolean areFieldsCategoryOK(String userCategory) {
		var isCreatingStudent = (userCategory.equals(Constants.STD_ROLE)) ? true : false;
		var fieldsOK = true;
		if(isCreatingStudent) {		//Si opcion de estudiante seleccionada
			fieldsOK = (signUpStudentLayout.getNameField().isEmpty()
					|| signUpStudentLayout.getDniField().isEmpty()
					|| signUpStudentLayout.getDniField().isInvalid()
					|| isUserIdUsedRequest(userCategory)
					|| signUpStudentLayout.getPhoneField().isEmpty()
					|| signUpStudentLayout.getPhoneField().isInvalid() 
					|| signUpStudentLayout.getStudiesField().isEmpty()) ? false : true;
		} else {					//Si opcion de empresa seleccionada
			fieldsOK = (signUpCompanyLayout.getNameField().isEmpty() 
					|| signUpCompanyLayout.getCifField().isEmpty() 
					|| signUpCompanyLayout.getCifField().isInvalid() 
					|| isUserIdUsedRequest(userCategory)
					|| signUpCompanyLayout.getCountryComboBox().isEmpty() 
					|| signUpCompanyLayout.getMemoryBuffer().getFileName().isEmpty()) ? false : true;
		}
		return fieldsOK;
	}
	
	/**
	 * Performs the sign up if the user fields are OK
	 * 
	 * @param userCategory - user's role to create
	 */
	private void handleSignUpIfUserFieldsOk(String userCategory) {							//Si no existen, procedo a realizar peticion de registro
		if(createUser(userCategory)) {														//Registro correcto, aviso a usuario
			if(userCategory.equals(Constants.STD_ROLE) && !signUpStudentLayout.getMemoryBuffer().getFileName().isEmpty()) {
				processImage(signUpStudentLayout.getMemoryBuffer().getInputStream(), signUpStudentLayout.getMemoryBuffer().getFileName());
			} else if(userCategory.equals(Constants.CMP_ROLE)) {
				processImage(signUpCompanyLayout.getMemoryBuffer().getInputStream(), signUpCompanyLayout.getMemoryBuffer().getFileName());
			}
			new CustomNotification(USR_CRE_MSG, NotificationVariant.LUMO_SUCCESS);
		} else {															//Fallo en el registro, aviso a usuario
			new CustomNotification(USR_CRE_ERR, NotificationVariant.LUMO_ERROR);
		}
		getUI().ifPresent(ui -> ui.navigate(Constants.MNG_USERS_PATH));
	}
	
	/**
	 * Performs the user creation
	 *
	 * @param userCategory - user's role
	 * @return boolean - true if creation is OK, false if not
	 */
	private boolean createUser(String userCategory) {
		var username = signUpUserLayout.getEmailField().getOptionalValue().get();			//Extraemos valores necesarios para el registro
		var password = signUpUserLayout.getPasswordField1().getOptionalValue().get();
		var role = userCategory;
		return sendCreateUserRequest(username, password, role);							//Ejecutamos peticion de registro
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
	 * Gets the header tag depending on the user's role to create
	 * 
	 * @param userCategory - user's role
	 * @return String - the header tag
	 */
	private String getHeaderTag(String userCategory) {
		switch(userCategory) {
			case Constants.STD_ROLE:
				return Constants.STUDENT_TAG;
			case Constants.CMP_ROLE:
				return Constants.COMPANY_TAG;
			default:
				return Constants.ADMIN_TAG;
		}
	}
	
	//PARSEOS JSON
	
	/**
	 * Creates a UserDto JSON object from its parameters
	 *
	 * @param email - user email
	 * @param password - user password
	 * @param role - user role
	 * @return String - UserDto JSON object
	 */
	private String createJSONObjectRegistration(String email, String password, String role) {
		var jsonObject = new JSONObject();
		var jsonUser = new JSONObject();
		jsonUser.put("username", email);
		jsonUser.put("password", password);
		jsonObject.put("userDto", jsonUser);
		jsonObject.put("roleName", role);
		switch(role) {
			case Constants.STD_ROLE:
				jsonObject.put("studentDto", createJSONObjectStudent());
				break;
			case Constants.CMP_ROLE:
				jsonObject.put("companyDto", createJSONObjectCompany());
				break;
			default:
				break;
		}
		return jsonObject.toString();
	}
	
	/**
	 * Creates a StudentDto JSON object from its parameters
	 *
	 * @return JSONObject - StudentDto JSON object
	 */
	private JSONObject createJSONObjectStudent() {
		var jsonStudent = new JSONObject();
		jsonStudent.put("name", signUpStudentLayout.getNameField().getValue());
		jsonStudent.put("dni", signUpStudentLayout.getDniField().getValue());
		jsonStudent.put("description", signUpStudentLayout.getDescriptionField().getValue());
		jsonStudent.put("studies", signUpStudentLayout.getStudiesField().getValue());
		jsonStudent.put("phoneNumber", signUpStudentLayout.getPhoneField().getValue());
		jsonStudent.put("profilePicture", signUpStudentLayout.getMemoryBuffer().getFileName());
		return jsonStudent;
	}
	
	/**
	 * Creates a CompanyDto JSON object from its parameters
	 *
	 * @return JSONObject - CompanyDTO JSON object
	 */
	private JSONObject createJSONObjectCompany() {
		var jsonCompany = new JSONObject();
		jsonCompany.put("name", signUpCompanyLayout.getNameField().getValue());
		jsonCompany.put("cif", signUpCompanyLayout.getCifField().getValue());
		jsonCompany.put("country", signUpCompanyLayout.getCountryComboBox().getOptionalValue().get());
		jsonCompany.put("description", signUpCompanyLayout.getDescriptionField().getValue());
		jsonCompany.put("profilePicture", signUpCompanyLayout.getMemoryBuffer().getFileName());
		return jsonCompany;
	}
	
	//HTTP REQUESTS
	
	/**
	 * Verifies if the email gived is already in use
	 *
	 * @param email - email to verify
	 * @return boolean - true if exists, false if not
	 */
	private boolean isEmailUsedRequest(String email) {
		if(email == null) {
			return true;
		}
		var httpRequest = new HttpRequest(Constants.USERS_REQ + "/user" + "?username=" + email);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		var responseBody = httpRequest.executeHttpGet(authToken);
		if(responseBody.isEmpty()) {
			return false;
		}
		new CustomNotification(Constants.EMAIL_ERROR, NotificationVariant.LUMO_PRIMARY);
		return true;
	}
	
	/**
	 * Verifies if the DNI or the CIF given as parameter already exists
	 *
	 * @param userCategory - user's role to create
	 * @return boolean - true if the identification code exists, false if not
	 */
	private boolean isUserIdUsedRequest(String userCategory) {
		var getUrl = new String();
		var id = new String();
		if(userCategory.equals(Constants.STD_ROLE)) {
			getUrl = Constants.STD_REQ + "/student?dni=";
			id = signUpStudentLayout.getDniField().getValue();
		} else {
			getUrl = Constants.CMP_REQ + "/company?cif=";
			id = signUpCompanyLayout.getCifField().getValue();
		}
		if(id == null) {
			return true;
		}
		var httpRequest = new HttpRequest(getUrl + id);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		var responseBody = httpRequest.executeHttpGet(authToken);
		if(responseBody == null) {
			return false;
		}
		return true;
	}
	
	/**
	 * Sends the user sign up request
	 *
	 * @param email - user email
	 * @param password - user password
	 * @param role - user role
	 * @return boolean - true if 201 CREATED, false if not
	 */
	private boolean sendCreateUserRequest(String email, String password, String role) {
		var httpRequest = new HttpRequest(Constants.USERS_REQ);
		var requestBody = createJSONObjectRegistration(email, password, role);	//Formamos el objeto JSON del usuarios
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpPost(authToken, requestBody);
	}
}
