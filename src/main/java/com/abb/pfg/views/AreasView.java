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
 * View that administrators use to manage areas
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
	//Etiquetas
	private static final String HEADER_TAG = "Áreas disponibles";
	private static final String NEW_AREA_TAG = "Nuevo área";
	private static final String ADD_AREA_TAG = "Añadir nuevo área";
	private static final String DEL_AREA_TAG = "Eliminar área";
	private static final String ADD_AREA_MSG = "Área añadido con éxito";
	private static final String ADD_AREA_ERR = "No se ha podido añadir el área";
	private static final String DEL_AREA_MSG = "Área eliminado con éxito";
	private static final String DEL_AREA_ERR = "No se ha podido eliminar el área";
	//Componentes
	private VerticalLayout mainLayout;
	private HorizontalLayout buttonsLayout;
	private CustomSelectAreas customSelectAreas;
	private TextField areaField;
	private Button addButton, goBackButton, deleteButton;
	//Atributos
	private String userRole;

	@Override
	public void beforeEnter(BeforeEnterEvent event) {
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		if(authToken == null){
			event.forwardTo(Constants.LOGIN_PATH);
			return;
		}
		userRole = (String) VaadinSession.getCurrent().getAttribute("role");
		if(!userRole.equals(Constants.ADM_ROLE)) {
			event.forwardTo(Constants.LOGIN_PATH);
			return;	
		}
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
		setContentLayout();
		mainLayout.add(customSelectAreas, areaField, buttonsLayout);
		var baseVerticalLayout = new VerticalLayout();
		baseVerticalLayout.add(new H1(HEADER_TAG), mainLayout);
		baseVerticalLayout.setAlignItems(Alignment.CENTER);
		this.setContent(baseVerticalLayout);
	}
	
	/**
	 * Sets up the content of the main layout
	 * 
	 */
	private void setContentLayout() {
		customSelectAreas = new CustomSelectAreas();
		customSelectAreas.setWidth("40%");
		areaField = new TextField(NEW_AREA_TAG);
		areaField.setMaxLength(50);
		areaField.setWidth("40%");
		getButtonsLayout();
	}
	
	/**
	 * Builds the button options layout
	 * 
	 */
	private void getButtonsLayout() {
		buttonsLayout = new HorizontalLayout();
		addButton = new Button(ADD_AREA_TAG);
		addButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		addButton.addClickListener(event -> addButtonListener());
		deleteButton = new Button(DEL_AREA_TAG);
		deleteButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
		deleteButton.addClickListener(event -> deleteButtonListener(customSelectAreas.getValue()));
		goBackButton = new Button(Constants.GOBACK_TAG);
		goBackButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		goBackButton.addClickListener(event -> getUI().ifPresent(ui -> ui.navigate(Constants.OFFERS_PATH)));
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
			customSelectAreas.refreshCustomSelectAreas();
			areaField.setValue("");
			new CustomNotification(ADD_AREA_MSG, NotificationVariant.LUMO_SUCCESS);
			return;
		}
		new CustomNotification(ADD_AREA_ERR, NotificationVariant.LUMO_SUCCESS);
	}
	
	/**
	 * Listener assigned to the delete button option
	 * 
	 * @param areaValue - select's current value
	 * 
	 */
	private void deleteButtonListener(String areaValue) {
		if(areaValue == null) {
			new CustomNotification(Constants.ERROR_TEXT, NotificationVariant.LUMO_WARNING);
			return;
		}
		var httpRequest = new HttpRequest(Constants.AREAS_REQ + "?name=" + areaValue);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		if(httpRequest.executeHttpDelete(authToken)) {
			customSelectAreas.refreshCustomSelectAreas();
			new CustomNotification(DEL_AREA_MSG, NotificationVariant.LUMO_SUCCESS);
			return;
		}
		new CustomNotification(DEL_AREA_ERR, NotificationVariant.LUMO_SUCCESS);
	}
}
