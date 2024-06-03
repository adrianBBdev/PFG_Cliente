
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
	
	//Componentes
	private VerticalLayout mainLayout;
	private CustomSignUpUserLayout signUpUserLayout;
	private CustomSignUpStudentLayout signUpStudentLayout;
	private CustomSignUpCompanyLayout signUpCompanyLayout;
	//Atributos
	private String userCategory, userRole;

	@Override
	public void setParameter(BeforeEvent event, String parameter) {
		userRole = (String) VaadinSession.getCurrent().getAttribute("role");
		try {
			if(!userRole.equals(Constants.ADM_ROLE) || parameter == null) {
				new CustomNotification(Constants.ACC_REJ_MSG, NotificationVariant.LUMO_ERROR);
				event.forwardTo(LoginView.class);
				return;
			}
		} catch (NullPointerException e) {
			new CustomNotification(Constants.ACC_REJ_MSG, NotificationVariant.LUMO_ERROR);
			event.forwardTo(LoginView.class);
			return;
		}
		userCategory = parameter;
	}
	
	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		if(authToken == null) {
			event.forwardTo(LoginView.class);
			return;
		}
		init();
	}
	
	/**
	 * Initialices all view components
	 * 
	 */
	private void init() {
		mainLayout = new VerticalLayout();		//Inicializamos el layout principal
		mainLayout.setAlignItems(Alignment.CENTER);
		mainLayout.setWidthFull();
		setContentLayout(userCategory);
		var baseVerticalLayout = new VerticalLayout();
		baseVerticalLayout.add(new H1("Crear " + getHeaderTag(userCategory)), mainLayout);
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
	private void handleSignUpIfUserFieldsOk(String userCategory) {																//Si no existen, procedo a realizar peticion de registro
		if(createUser(userCategory)) {														//Registro correcto, aviso a usuario
				if(createUserCategory(userCategory)) { 												//Si registro correcto
					var picName = signUpStudentLayout.getMemoryBuffer().getFileName();
					if(picName != null) {
						processImage(signUpStudentLayout.getMemoryBuffer().getInputStream(), picName);
					}
					new CustomNotification("Usuario creado correctamente", NotificationVariant.LUMO_SUCCESS);
				} else {														//Si registro fallido
					new CustomNotification("No se ha podido crear el usuario", NotificationVariant.LUMO_ERROR);
				}
		} else {															//Fallo en el registro, aviso a usuario
			new CustomNotification("No se ha podido crear el usuario", NotificationVariant.LUMO_ERROR);
		}
		this.getUI().ifPresent(ui -> ui.navigate(Constants.MNG_USERS_PATH));
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
	 * Performs the creation of the user, depending on the role it will have
	 *
	 * @param userCategory - user's role to create
	 * @return boolean - true if it has been created, false if not
	 */
	private boolean createUserCategory(String userCategory) {
		var postUrl = new String();
		var requestBody = new String();
		switch(userCategory) {
			case Constants.STD_ROLE:
				requestBody = createJSONObjectStudent(signUpUserLayout.getEmailField().getValue(), 
						signUpStudentLayout.getNameField().getValue(), signUpStudentLayout.getDniField().getValue(),
						signUpStudentLayout.getPhoneField().getValue(), signUpStudentLayout.getStudiesField().getValue(),
						signUpStudentLayout.getDescriptionField().getValue(), signUpStudentLayout.getMemoryBuffer().getFileName());
				postUrl = Constants.STD_REQ;
				break;
			case Constants.CMP_ROLE:
				requestBody = createJSONObjectCompany(signUpUserLayout.getEmailField().getValue(), 
						signUpCompanyLayout.getNameField().getValue(), signUpCompanyLayout.getCifField().getValue(),
						signUpCompanyLayout.getCountryComboBox().getValue(), signUpCompanyLayout.getDescriptionField().getValue(), 
						signUpCompanyLayout.getMemoryBuffer().getFileName());
				postUrl = Constants.CMP_REQ;
				break;
			default:	//Admin
				requestBody = createJSONObjectAdmin(signUpUserLayout.getEmailField().getValue());
				postUrl = Constants.ADMIN_REQ;		
		}
		return sendCreateUserCategory(postUrl, requestBody);
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
	private String createJSONObjectUser(String email, String password, String role) {
		var jsonUser = new JSONObject();
		jsonUser.put("username", email);
		jsonUser.put("password", password);
		var jsonRole = new JSONObject(sendGetRoleRequest(role));
		jsonUser.put("role", jsonRole);
		return jsonUser.toString();
	}
	
	/**
	 * Creates an AdminDto JSON object from its parameters
	 * 
	 * @param email - user's email
	 * @return String - AdminDto JSON object
	 */
	private String createJSONObjectAdmin(String email) {
		var jsonAdmin = new JSONObject();
		var jsonUser = new JSONObject(sendGetUserRequest(email));
		jsonAdmin.put("user", jsonUser);
		return jsonAdmin.toString();
	}
	
	/**
	 * Creates a StudentDto JSON object from its parameters
	 *
	 * @param email - user's email
	 * @param name - student name
	 * @param dni - studnet DNI
	 * @param phoneNumber - student phone number
	 * @param studies - student studies
	 * @param description - student description
	 * @param profilePicture - student's profile picture
	 * @return String - StudentDto JSON object
	 */
	private String createJSONObjectStudent(String email, String name, String dni, String phoneNumber,
			String studies, String description, String picturePath) {
		var jsonStudent = new JSONObject();
		jsonStudent.put("name", name);
		jsonStudent.put("dni", dni);
		jsonStudent.put("description", description);
		jsonStudent.put("studies", studies);
		jsonStudent.put("phoneNumber", phoneNumber);
		jsonStudent.put("profilePicture", picturePath);
		var jsonUser = new JSONObject(sendGetUserRequest(email));
		jsonStudent.put("user", jsonUser);
		return jsonStudent.toString();
	}
	
	/**
	 * Creates a CompanyDto JSON object from its parameters
	 *
	 * @param email - company's user email
	 * @param name - company name
	 * @param cif - company CIF
	 * @param country - company origin country
	 * @param description - company description
	 * @param logo - company's logo name
	 * @return String - CompanyDTO JSON object
	 */
	private String createJSONObjectCompany(String email, String name, String cif, String country,
			String description, String logo) {
		var jsonCompany = new JSONObject();
		jsonCompany.put("name", name);
		jsonCompany.put("cif", cif);
		jsonCompany.put("country", country);
		jsonCompany.put("description", description);
		jsonCompany.put("profilePicture", logo);
		var jsonUser = new JSONObject(sendGetUserRequest(email));
		jsonCompany.put("user", jsonUser);
		return jsonCompany.toString();
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
		var requestBody = createJSONObjectUser(email, password, role);	//Formamos el objeto JSON del usuarios
		var httpRequest = new HttpRequest(Constants.USERS_REQ);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpPost(authToken, requestBody);
	}
	
	/**
	 * Sends the request to get a specific role object
	 *
	 * @param role - role to get
	 * @return String - role JSON object requested
	 */
	private String sendGetRoleRequest(String role) {
		var httpRequest = new HttpRequest(Constants.AUTH_REQ + "/role?name=" + role);
		return httpRequest.executeHttpGet(null);
	}
	
	/**
	 * Sends a request to verify if a user exists or not
	 *
	 * @param email - user email to verify
	 * @return HttpResponse - request result
	 */
	private String sendGetUserRequest(String email) {
		var httpRequest = new HttpRequest(Constants.USERS_REQ + "/user?username=" + email);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpGet(authToken);
	}
	
	/**
	 * Sends the request to create a student, company or administrator
	 * 
	 * @param postUrl - http url to send the request
	 * @param requestBody - request body needed to send the post request
	 * @return boolean - true if it has been created, false if not
	 */
	private boolean sendCreateUserCategory(String postUrl, String requestBody) {
		if(postUrl == null || requestBody == null) {
			return false;
		}
		var httpRequest = new HttpRequest(postUrl);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpPost(authToken, requestBody);
	}
}
