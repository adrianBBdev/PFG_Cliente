package abb.pruebas.frontend;

import java.io.InputStream;
import java.util.Optional;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.Notification.Position;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.UploadI18N;
import com.vaadin.flow.component.upload.receivers.MultiFileMemoryBuffer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import abb.pruebas.frontend.commons.Constants;
import abb.pruebas.frontend.components.CustomBasicDialog;

@Route("/signIn")
@PageTitle("J4S - Registro")
public class SignInView extends VerticalLayout{
	
	private static final String HEADER_TAG = "Proceso de registro";
	private static final String CONFIRMPASS_TAG = "Repita la contraseña";
	private static final String REGISTER_TAG = "¿Cómo desea registrarse?";
	private static final String PROFILE_TAG = "Carga tu foto de perfil";
	private static final String LOGO_TAG = "Carga el logo de la empresa";
	private static final String CONFIRM_TAG = "Confirmar registro";
	private static final String CONFIRM_TEXT = "Se ha registrado correctamente";
	private static final String CANCEL_TEXT = "Se ha cancelado el proceso de registro";
	private static final String PASSWD_ERROR = "La contraseña debe coincidir";
	private static final String PERS_TAG = "Descripción personal";
	private static final String ABOUT_TAG = "Acerca de la empresa";
	private static final String EMAIL_ERROR = "Correo electrónico no válido";
	
	private VerticalLayout mainLayout, userDataLayout, studentOptionLayout, companyOptionLayout;
	private EmailField emailField;
	private PasswordField passwordField1, passwordField2;
	private RadioButtonGroup<String> radioButton;
	private HorizontalLayout buttonsLayout;
	private Button confirmButton, cancelButton;
	private boolean isStudentLayoutCreated,isCompanyLayoutCreated = false;
	private TextField nameField, dniField, studiesField, cifField;
	
	private MultiFileMemoryBuffer profilePicture;
	
	private FormLayout formSelectionLayout;
	
	private ComboBox<String> comboBox;
	private TextArea descriptionField;
	
	private Upload upload;
	private Dialog dialog;
	private Notification notification;
	
	/**
	 * View constructor by default
	 * 
	 */
	public SignInView () {
		//Inicializamos la vista y añadimos el layout principal
		init();
		add(new H1(HEADER_TAG), mainLayout);
		setAlignItems(Alignment.CENTER);
	}
	
	/**
	 * Initialices the view components
	 * 
	 */
	private void init() {
		//Inicializamos el layout principal
		mainLayout = new VerticalLayout();
		mainLayout.setAlignItems(Alignment.CENTER);
		mainLayout.setWidthFull();
		//Creamos layout de formulario de registro inicial
		getUserDataForm();
		//Añadimos layout del formulario inicial
		mainLayout.add(userDataLayout);
	}
	
	private void getUserDataForm() {
		//Inicializamos el layout del formulario inicial
		userDataLayout = new VerticalLayout();
		userDataLayout.setAlignItems(Alignment.CENTER);
		userDataLayout.setWidthFull();
		//Creamos el campo del email para el registro
		emailField = new EmailField(Constants.EMAIL_TAG);
		emailField.setClearButtonVisible(true);
		emailField.setPrefixComponent(VaadinIcon.ENVELOPE.create());
		emailField.setErrorMessage(EMAIL_ERROR);
		emailField.setWidth("30%");
		//Creamos el campo de la password para el registro
		passwordField1 = new PasswordField(Constants.PASSWORD_TAG);
		passwordField1.setWidth("30%");
		//Creamos el campo de la repeticion de la password para el registro
		passwordField2 = new PasswordField(CONFIRMPASS_TAG);
		passwordField2.setWidth("30%");
		//Creamos la multiopcion para el rol en el registro
		radioButton = new RadioButtonGroup<>();
		radioButton.setLabel(REGISTER_TAG);
		radioButton.setItems(Constants.STUDENT_TAG, Constants.COMPANY_TAG);
		radioButton.addValueChangeListener(event -> {
			this.radioButtonListener(radioButton.getOptionalValue());
		});
		userDataLayout.add(emailField, passwordField1, passwordField2, radioButton);
	}
	
	/**
	 * Performs the actions to set when radioButton is clicked.
	 * 
	 */
	private void radioButtonListener(Optional<String> option) {
		//Se comprueba la opcion seleccionada
		if(radioButton.getValue().equals(Constants.STUDENT_TAG)) {
			//Comprobamos si el layout de la opcion estudiante esta creado
			if(!isStudentLayoutCreated) {	//lo creamos
				this.getStudentOptionLayout();
			} else {	//esta creado, lo hacemos visible
				if(companyOptionLayout != null) {
					companyOptionLayout.setVisible(false);
				}
				studentOptionLayout.setVisible(true);
			}
		} else if (radioButton.getValue().equals(Constants.COMPANY_TAG)) {
			//Comprobamos si el layout de la opcion empresa esta creado
			if(!isCompanyLayoutCreated) {	//lo creamos
				this.getCompanyOptionLayout();
			} else {	//esta creado, lo hacemos visible
				if(studentOptionLayout != null) {
					studentOptionLayout.setVisible(false);
				}
				companyOptionLayout.setVisible(true);
			}
		}
	}
	
	/**
	 * Enables the student option layout
	 * 
	 */
	private void getStudentOptionLayout() {
		//Creamos el layout de la opcion estudiante
		studentOptionLayout = new VerticalLayout();
		studentOptionLayout.setAlignItems(Alignment.CENTER);
		studentOptionLayout.setWidthFull();
		//Creamos el campo del nombre
		nameField = new TextField(Constants.NAME_TAG);
		nameField.setWidth("30%");
		//Creamos el campo del DNI
		dniField = new TextField(Constants.DNI_TAG);
		dniField.setWidth("30%");
		//Creamos el campo de los estudios
		studiesField = new TextField(Constants.STUDIES_TAG);
		studiesField.setWidth("30%");
		//Creamos el campo de la descripcion personal
		getDescriptionTextArea(PERS_TAG);
		//Creamos el layout de los botones
		getButtonsLayout();
		//Añadimos los componentes al layout
		studentOptionLayout.add(nameField, dniField, studiesField, descriptionField, 
				buttonsLayout);
		//Añadimos el layout a la vista principal
		mainLayout.add(studentOptionLayout);
		//Activamos la creacion del layout de la opcion de estudiante
		isStudentLayoutCreated = true;
	}
	
	/**
	 * Enables the company option layout
	 * 
	 */
	private void getCompanyOptionLayout() {
		//Creamos el layout de la opcion empresa
		companyOptionLayout = new VerticalLayout();
		companyOptionLayout.setAlignItems(Alignment.CENTER);
		companyOptionLayout.setWidthFull();
		//Creamos el campo del nombre
		nameField = new TextField(Constants.NAME_TAG);
		nameField.setWidth("30%");
		//Creamos el campo del DNI
		cifField = new TextField(Constants.CIF_TAG);
		cifField.setWidth("30%");
		//Creamos el campo de la descripcion personal
		getDescriptionTextArea(ABOUT_TAG);
		//Creamos el layout de los botones
		getButtonsLayout();
		//Añadimos los componentes al layout
		companyOptionLayout.add(nameField, cifField, descriptionField, buttonsLayout);
		//Añadimos el layout a la vista principal
		mainLayout.add(companyOptionLayout);
		//Activamos la creacion del layout de la opcion de empresa
		isCompanyLayoutCreated = true;
	}
	
//	private void createForms() {
//		userLayout = new VerticalLayout();
//		formSelectionLayout = new FormLayout();
//		formSelectionLayout.setVisible(false);
//		emailField = new EmailField(Constants.EMAIL_TAG);
//		emailField.setPlaceholder(PLH_TAG);
//		emailField.setClearButtonVisible(true);
//		emailField.setPrefixComponent(VaadinIcon.ENVELOPE.create());
//		emailField.setErrorMessage(EMAIL_ERROR);
//		passwordField1 = new PasswordField(Constants.PASSWORD_TAG);
//		//passwordField1.setWidth(Constants.SMALL_WIDTH);
//		passwordField2 = new PasswordField(CONFIRMPASS_TAG);
//		//passwordField2.setWidth(Constants.SMALL_WIDTH);
//		radioButton = new RadioButtonGroup<>();
//		radioButton.setLabel(REGISTER_TAG);
//		radioButton.setItems(Constants.STUDENT_TAG, Constants.COMPANY_TAG);
//		userLayout.add(emailField, passwordField1, passwordField2, radioButton);
//		userLayout.setAlignItems(Alignment.CENTER);
//		mainLayout.add(userLayout);
//		radioButton.addValueChangeListener(event -> {
//			if(formSelectionLayout.isVisible()) {
//				mainLayout.remove(formSelectionLayout);
//				mainLayout.remove(upload);
//				mainLayout.remove(descriptionField);
//				mainLayout.remove(buttonsLayout);
//				formSelectionLayout = new FormLayout();
//			}
//			if(radioButton.getValue().equals(Constants.STUDENT_TAG)) {
//				this.buildStudentOption();
//			} else if (radioButton.getValue().equals(Constants.COMPANY_TAG)) {
//				this.buildCompanyOption();
//			}
//			formSelectionLayout.setVisible(true);
//			mainLayout.add(formSelectionLayout, upload,descriptionField, buttonsLayout);
//		});
//	}
	
	/**
	 * Creates the text area depending on the option selected
	 * 
	 * @param option - student or company option selected
	 */
	private void getDescriptionTextArea(String option) {
		descriptionField = new TextArea(option);
		descriptionField.setWidth("30%");
		descriptionField.setValueChangeMode(ValueChangeMode.EAGER);
		descriptionField.addValueChangeListener(e -> {
		    e.getSource()
		            .setHelperText(e.getValue().length() + "/" + Constants.DESC_LENGTH);
		});
	}
	
//	private void buildStudentOption() {
//		nameField = new TextField(Constants.NAME_TAG);
//		profilePicture = new MultiFileMemoryBuffer();
//		upload = new Upload(profilePicture);
//		upload.setAcceptedFileTypes(Constants.JPG, Constants.PNG);
//		upload.setUploadButton(new Button(PROFILE_TAG));
//		upload.addSucceededListener(eventUpload -> {
//		    String fileName = eventUpload.getFileName();
//		    InputStream inputStream = profilePicture.getInputStream(fileName);
//		});
//		dniField = new TextField(Constants.DNI_TAG);
//		this.createTextArea();
//		studiesField = new TextField(Constants.STUDIES_TAG);
//		formSelectionLayout.add(nameField, emailField, dniField, studiesField);
//	}
	
//	private void buildCompanyOption() {
//		nameField = new TextField(Constants.NAME_TAG);
//		upload = new Upload(profilePicture);
//		upload.setAcceptedFileTypes(Constants.JPG, Constants.PNG);
//		upload.setUploadButton(new Button(LOGO_TAG));
//		
//		upload.addSucceededListener(eventUpload -> {
//		    String fileName = eventUpload.getFileName();
//		    InputStream inputStream = profilePicture.getInputStream(fileName);
//		});
//		cifField = new TextField(Constants.CIF_TAG);
//		comboBox = new ComboBox<>(Constants.COUNTRY_TAG);
//		comboBox.setItems("Alemania", "España", "Italia", "Portugal", "Suiza");
//		this.createTextArea();
//		formSelectionLayout.add(nameField, cifField, comboBox);
//	}
	
//	private void createButtonsLayout() {
//		buttonsLayout = new HorizontalLayout();
//		confirmButton = new Button(CONFIRM_TAG);
//		confirmButton.setSizeFull();
//		cancelButton = new Button(Constants.CANCEL_TAG);
//		cancelButton.setSizeFull();
//		confirmButton.addClickListener(event -> {	//Si algun campo esta vacío, salta dialogo de error
//			if(this.hasAnyEmptyField()) {
//				dialog = new CustomBasicDialog(Constants.ERROR_TAG,
//						Constants.ERROR_TEXT);
//				dialog.open();
//			} else {	//Si las password no esta OK, salta error
//				if(!isPasswordOk()) {
//					dialog = new CustomBasicDialog(Constants.ERROR_TAG,
//							PASSWD_ERROR);
//					dialog.open();
//					passwordField1.setValue(new String());
//					passwordField2.setValue(new String());
//				} else {	//Se registra con exito
//					notification = new Notification(CONFIRM_TEXT);
//					notification.setPosition(Position.TOP_CENTER);
//					notification.setDuration(Constants.NOTIF_DURATION);
//					notification.open();
//					this.getUI().ifPresent(ui -> ui.navigate(Constants.LOGIN_PATH));
//				}				
//			}
//		});
//		cancelButton.addClickListener(event -> {
//			notification = new Notification(CANCEL_TEXT);
//			notification.setPosition(Position.TOP_CENTER);
//			notification.setDuration(Constants.NOTIF_DURATION);
//			notification.open();
//			this.getUI().ifPresent(ui -> ui.navigate(Constants.LOGIN_PATH));
//		});
//		buttonsLayout.add(confirmButton, cancelButton);
//	}
	
//	private boolean hasAnyEmptyField() {		//Si algun campo esta vacio
//		boolean isEmpty = false;
//		
//		if(emailField.isEmpty() || emailField.isInvalid() || passwordField1.isEmpty() || passwordField2.isEmpty() || radioButton.isEmpty()) {
//			isEmpty = true;
//		} else {
//			if(radioButton.getOptionalValue().get().equals(Constants.STUDENT_TAG)) {
//				if(nameField.isEmpty() ||  dniField.isEmpty() || descriptionField.isEmpty() || studiesField.isEmpty()) {
//					isEmpty = true;
//				}
//			} else if(radioButton.getOptionalValue().get().equals(Constants.COMPANY_TAG)) {
//				if(nameField.isEmpty() || cifField.isEmpty() || comboBox.isEmpty() || descriptionField.isEmpty()) {
//					isEmpty = true;
//				}
//			}
//		}
//		return isEmpty;
//	}
	
	/**
	 * Obtains the layout with buttons needed to confirm or cancel the sign in process
	 * 
	 */
	private void getButtonsLayout() {
		//Creamos el layout para los botones
		buttonsLayout = new HorizontalLayout();
		buttonsLayout.setAlignItems(Alignment.CENTER);
		buttonsLayout.setWidthFull();
		//Creamos boton de confirmar registro
		confirmButton = new Button(CONFIRM_TAG);
		confirmButton.setWidth("10%");
		//Creamos boton de cancelar registro
		cancelButton = new Button(Constants.CANCEL_TAG);
		cancelButton.setWidth("10%");
		//Agregamos botones al layout
		buttonsLayout.add(confirmButton, cancelButton);
	}
	
	/**
	 * True if both password fields are exactly the same
	 * 
	 * @return true if equal, false if not
	 */
	private boolean isPasswordOk() {
		return passwordField1.getValue().equals(passwordField2.getValue());
	}
}
