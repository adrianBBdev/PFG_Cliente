package com.abb.pfg.views;

import org.json.JSONObject;

import com.abb.pfg.custom.CustomAppLayout;
import com.abb.pfg.custom.CustomNotification;
import com.abb.pfg.custom.CustomSelectAreas;
import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.HttpRequest;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import lombok.NoArgsConstructor;
/**
 * View which admins uses to create new job offer areas
 *
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route(Constants.AREAS_PATH)
@PageTitle("J4S - Areas")
@NoArgsConstructor
public class AreasView extends CustomAppLayout implements BeforeEnterObserver {

	private static final long serialVersionUID = 2715604459727286136L;
	private static final String HEADER_TAG = "Áreas disponibles";
	private VerticalLayout mainLayout;
	private HorizontalLayout buttonsLayout;
	private CustomSelectAreas customSelect;
	private TextField areaField;
	private Button addButton, goBackButton, deleteButton;
	private String userType;

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		userType = (String) VaadinSession.getCurrent().getAttribute("role");
		if(authToken == null || !userType.equals(Constants.ADM_ROLE)){
			event.forwardTo(Constants.LOGIN_PATH);
			return;
		}
		userType = (String) VaadinSession.getCurrent().getAttribute("role");
		init();		//Inicializamos la vista y añadimos el layout principal
	}

	/**
	 * Initialices the view components
	 *
	 */
	private void init() {
		mainLayout = new VerticalLayout();		//Inicializamos el layout principal
		mainLayout.setAlignItems(Alignment.CENTER);
		mainLayout.setWidthFull();
		customSelect = new CustomSelectAreas();
		customSelect.setWidth("40%");
		areaField = new TextField("Nuevo área");
		areaField.setMaxLength(50);
		areaField.setWidth("40%");
		getButtonsLayout();
		mainLayout.add(customSelect, areaField, buttonsLayout);
		var baseVerticalLayout = new VerticalLayout();
		baseVerticalLayout.add(new H1(HEADER_TAG), mainLayout);
		baseVerticalLayout.setAlignItems(Alignment.CENTER);
		this.setContent(baseVerticalLayout);
	}
	
	/**
	 * Builds the button options layout
	 * 
	 */
	private void getButtonsLayout() {
		buttonsLayout = new HorizontalLayout();
		addButton = new Button("Añadir nuevo área");
		addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addButton.addClickListener(event -> addButtonListener());
		deleteButton = new Button("Eliminar área");
		deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
		deleteButton.addClickListener(event -> deleteButtonListener());
		goBackButton = new Button(Constants.GOBACK_TAG);
		goBackButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		goBackButton.addClickListener(event -> goBackButtonListener());
		buttonsLayout.add(addButton, deleteButton, goBackButton);
	}
	
	/**
	 * Listener assigned to the add new area button option
	 * 
	 */
	private void addButtonListener() {
		if(areaField.getValue().isBlank()) {
			new CustomNotification(Constants.ERROR_TEXT, NotificationVariant.LUMO_WARNING);
			return;
		}
		var httpRequest = new HttpRequest(Constants.AREAS_REQ);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		var jsonObject = new JSONObject();
		jsonObject.put("name", areaField.getValue());
		var requestBody = jsonObject.toString();
		if(httpRequest.executeHttpPost(authToken, requestBody)) {
			customSelect.refreshCustomSelectAreas();
			areaField.setValue("");
			new CustomNotification("Área añadido con éxito", NotificationVariant.LUMO_SUCCESS);
			return;
		}
		new CustomNotification("No se ha podido añadir el área", NotificationVariant.LUMO_SUCCESS);
	}
	
	/**
	 * Listener assigned to the delete button option
	 * 
	 */
	private void deleteButtonListener() {
		if(customSelect.getValue() == null) {
			new CustomNotification(Constants.ERROR_TEXT, NotificationVariant.LUMO_WARNING);
			return;
		}
		var httpRequest = new HttpRequest(Constants.AREAS_REQ + "?name=" + customSelect.getValue());
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		if(httpRequest.executeHttpDelete(authToken)) {
			customSelect.refreshCustomSelectAreas();
			new CustomNotification("Área eliminado con éxito", NotificationVariant.LUMO_SUCCESS);
			return;
		}
		new CustomNotification("No se ha podido eliminar el área", NotificationVariant.LUMO_SUCCESS);
	}
	
	/**
	 * Listener assigned to the go back button option
	 * 
	 */
	private void goBackButtonListener() {
		this.getUI().ifPresent(ui -> ui.navigate(Constants.OFFERS_PATH));
	}
}
