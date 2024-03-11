package abb.pruebas.frontend;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import abb.pruebas.backend.Multimedia;
import abb.pruebas.frontend.commons.Constants;
import abb.pruebas.frontend.components.MainLayout;

/**
 * Class which represents the profile view of a student
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@PageTitle("J4S - Student")
@Route(Constants.STUDENT_PATH)
public class StudentView extends MainLayout implements HasUrlParameter<String>{

	private static final String HEADER_TITLE = "Perfil de estudiante";
	private static final String STARTCAHT_TAG = "Iniciar chat";
	
	private VerticalLayout mainLayout, contentLayout, detailsLayout, gridLayout;
	private HorizontalLayout buttonsLayout;
	private TextField nameField, emailField, dniField, studiesField;
	private TextArea descriptionField;
	private FormLayout formLayout;
	private Grid<Multimedia> multimediaGrid;
	private Button initChatButton, cancelButton;

	public StudentView() {
		init();
		contentLayout.add(new H1(HEADER_TITLE), mainLayout);
		setContent(contentLayout);
	}
	
	private void init() {
		contentLayout = new VerticalLayout();
		contentLayout.setAlignItems(Alignment.CENTER);
		mainLayout = new VerticalLayout();
		this.createStudentFields();
		gridLayout = new VerticalLayout();
		multimediaGrid = new Grid<>();
		multimediaGrid.addColumn(Multimedia::getName).setHeader(Constants.FILE_TAG);
		multimediaGrid.addColumn(Multimedia::getFile).setHeader(Constants.FILETYPE_TAG);
		gridLayout.add(multimediaGrid);
		gridLayout.setWidth(Constants.WIDTH);
		gridLayout.setAlignItems(Alignment.CENTER);
		this.createButtonsLayout();
		mainLayout.add(detailsLayout, descriptionField, gridLayout, buttonsLayout);
		mainLayout.setAlignItems(Alignment.CENTER);
	}
	
	private void createStudentFields() {
		nameField = new TextField("Nombre completo");
		nameField.setReadOnly(true);
		nameField.setWidth(Constants.SMALL_WIDTH);
		emailField = new TextField(Constants.EMAIL_TAG);
		emailField.setReadOnly(true);
		emailField.setWidth(Constants.SMALL_WIDTH);
		dniField = new TextField(Constants.DNI_TAG);
		dniField.setReadOnly(true);
		studiesField = new TextField("Estudios");
		studiesField.setReadOnly(true);
		descriptionField = new TextArea("Descripción personal");
		descriptionField.setReadOnly(true);
		descriptionField.setWidth(Constants.WIDTH);
		formLayout = new FormLayout();
		formLayout.add(nameField, emailField, dniField, studiesField);
		detailsLayout = new VerticalLayout();
		detailsLayout.add(formLayout);
		detailsLayout.setAlignItems(Alignment.CENTER);
		detailsLayout.setWidth(Constants.WIDTH);
	}
	
	private void createButtonsLayout() {
		buttonsLayout = new HorizontalLayout();
		Button initChatButton = new Button(STARTCAHT_TAG);
		cancelButton = new Button(Constants.CANCEL_TAG);
		cancelButton.addClickListener(event -> 
			this.getUI().ifPresent(ui -> ui.navigate(Constants.ALLUSERS_PATH)));
		buttonsLayout.add(initChatButton, cancelButton);
	}
	
	@Override
	public void setParameter(BeforeEvent event, String parameter) {
		this.nameField.setValue(parameter);
	}

}
