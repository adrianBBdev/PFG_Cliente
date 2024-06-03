package com.abb.pfg.custom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.FileListComponent;
import com.abb.pfg.utils.HttpRequest;
import com.brownie.videojs.VideoJS;
import com.vaadin.componentfactory.pdfviewer.PdfViewer;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.server.VaadinSession;

/**
 * Custom grid to display a list of resources orderly
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
public class CustomFilesGrid extends Grid<FileListComponent> {

	private static final long serialVersionUID = -6086476472440083476L;
	//Atributos
	private List<FileListComponent> files;
	private String fileCategory;
	
	/**
	 * Constructor for media resources associated with job offers
	 * 
	 * @param contentArray - list of resources
	 * @param userRole - user's role
	 */
	public CustomFilesGrid(JSONArray contentArray, String userRole) {
		fileCategory = Constants.RESOURCE_TAG;
		var gridData = getGridData(contentArray, userRole);
		this.setAllRowsVisible(true);
		this.setItems(gridData);
		this.addColumn(file -> file.getName()).setHeader(Constants.NAME_TAG);
		this.addColumn(file -> file.getType()).setHeader(Constants.RESOURCE_TAG);
		var resourcesGrid = this.addContextMenu();
		resourcesGrid.setOpenOnClick(true);
		resourcesGrid.addItem("Ver recurso", event -> openResourceListener(event.getItem().get()));
		if(userRole.equals(Constants.ADM_ROLE) || userRole.equals(Constants.CMP_ROLE)) {
			resourcesGrid.addItem("Cambiar nombre", event -> editResourceNameListener(event.getItem().get()));
			resourcesGrid.addItem(Constants.DELETE_TAG, event -> deleteResourceListener(event.getItem().get(), 
					Constants.RES_REQ));
		}
	}
	
	/**
	 * Constructor for media resources associated with users
	 * 
	 * @param contentArray - list of resources
	 * @param userRole - user's role
	 * @param username - user's username
	 */
	public CustomFilesGrid(JSONArray contentArray, String userRole, String username) {
		fileCategory = Constants.MEDIA_TAG;
		var gridData = getGridData(contentArray, userRole);
		this.setAllRowsVisible(true);
		this.setItems(gridData);
		this.addColumn(file -> file.getName()).setHeader(Constants.NAME_TAG);
		this.addColumn(file -> file.getType()).setHeader(Constants.MEDIA_TAG);
		var resourcesGrid = this.addContextMenu();
		resourcesGrid.setOpenOnClick(true);
		resourcesGrid.addItem("Ver archivo", event -> openResourceListener(event.getItem().get()));
		var usernameOwner = (String) VaadinSession.getCurrent().getAttribute("username");
		if(userRole.equals(Constants.ADM_ROLE) || username.equals(usernameOwner)) {
			resourcesGrid.addItem("Cambiar nombre", event -> editResourceNameListener(event.getItem().get()));
			resourcesGrid.addItem(Constants.DELETE_TAG, event -> deleteResourceListener(event.getItem().get(), 
					Constants.MEDIA_REQ));
		}
	}
	
	/**
	 * Gets the users list to show
	 *
	 * @param contentArray - JSON array which contains all job offers to show
	 * @param userType - user's type to show
	 * @return List<ResourceListComponent> - resource list
	 */
	private List<FileListComponent> getGridData(JSONArray contentArray, String userRole){
		files = new ArrayList<>();
		for(var i=0; i < contentArray.length(); i++) {
			var jsonObject = contentArray.getJSONObject(i);
			var code = jsonObject.getLong("id");
			var completeName = jsonObject.getString("name");
			var name = completeName.substring(0, completeName.lastIndexOf('.'));
			var type = getFileTypeName(completeName.substring(completeName.lastIndexOf('.') + 1));
			files.add(new FileListComponent(code, name, type));
		}
		return files;
	}
	
	//LISTENERS
	
	/**
	 * Listener assigned to the open resource button
	 * 
	 * @param resource - media resource to display
	 */
	private void openResourceListener(FileListComponent resource) {
		var dialog = getResourceDialog();
		if(resource.getType().contains("Documento")) {
			dialog.add(openPDFfile(resource));
		} else if(resource.getType().contains("Imagen")) {
			dialog.add(openImageFile(resource));
		} else if(resource.getType().contains("Vídeo")) {
			dialog.add(openVideoFile(resource));
		}
		dialog.open();
	}
	
	/**
	 * Edits the media resource's name
	 * 
	 * @param resource - selected resource
	 */
	private void editResourceNameListener(FileListComponent resource) {
		var dialog = new Dialog();
		dialog.getHeader().add(new H2("Cambiar nombre de recurso multimedia"));
		dialog.setModal(true);
		dialog.setDraggable(true);
		var newNameField = new TextField();
		newNameField.setWidth("70%");
		newNameField.setMaxLength(Constants.FIELDS_MAX_LENGTH);
		newNameField.setPlaceholder("Escriba un nuevo nombre para el archivo");
		var saveButton = new Button("Modificar", event -> confirmModifyMediaNameListener(dialog, resource, newNameField.getValue()));
		saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		var cancelButton = new Button(Constants.CANCEL_TAG, event -> dialog.close());
		dialog.getFooter().add(saveButton, cancelButton);
		dialog.add(newNameField);
		dialog.open();
	}
	
	/**
	 * Listener assigned to the confirm button to modify the resource's name
	 * 
	 * @param dialog - dialog where it is being displayed the fields to modify the resource's name
	 * @param resource - selected resource
	 * @param newValue - new name to associated with the resource
	 */
	private void confirmModifyMediaNameListener(Dialog dialog, FileListComponent resource, String newValue) {
		var newValueTmp = newValue + getFileType(resource.getType());
		if(newValue.isBlank() || resource.getName().equals(newValue)) {
			new CustomNotification("Introduza un valor válido para el nombre del archivo", NotificationVariant.LUMO_WARNING);
			return;
		}
		var storedPath = (fileCategory.equals(Constants.MEDIA_TAG)) ? Constants.STORED_MEDIA_PATH : Constants.STORED_FILE_PATH;
		var fileJSON = sendGetFileRequest(resource, newValueTmp);
		if(fileJSON != null && sendUpdateFileNameRequest(fileJSON)) {
			new CustomNotification("El archivo ha sido actualizado correctamente", NotificationVariant.LUMO_WARNING);
			var oldFile = new File(storedPath + "\\" + resource.getName() + getFileType(resource.getType()));
			var newFile = new File(storedPath + "\\" + newValueTmp);
			oldFile.renameTo(newFile);
			resource.setName(newValue);
			this.getDataProvider().refreshAll();
		}
		dialog.close();
	}
	
	/**
	 * Listener associated with the delete option
	 * 
	 * @param resource - resource to delete
	 * @param url - url to send the http request
	 */
	private void deleteResourceListener(FileListComponent resource, String url) {
		var resourceCode = resource.getFileCode();
		var httpRequest = new HttpRequest(url + "?code=" + resourceCode);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		if(httpRequest.executeHttpDelete(authToken)) {
			new CustomNotification("Recurso eliminado correctamente", NotificationVariant.LUMO_SUCCESS);
			files.remove(resource);
			this.getDataProvider().refreshAll();
			return;
		}
		new CustomNotification("El recurso no ha podido ser eliminado", NotificationVariant.LUMO_SUCCESS);
	}
	
	/**
	 * Creates and returns a dialog that will display the selected resources
	 * 
	 * @return Dialog - a dialog
	 */
	private Dialog getResourceDialog() {
		var dialog = new Dialog();
		dialog.getHeader().add(new H2("Recurso multimedia"));
		dialog.setModal(true);
		dialog.setDraggable(true);
		dialog.setWidth("70%");
		dialog.setHeight("70%");
		var closeButton = new Button("Cerrar", event -> dialog.close());
		dialog.getFooter().add(closeButton);
		return dialog;
	}
	
	/**
	 * Returns an object that allows the user opening a PDF file
	 * 
	 * @param resource - resource that points to a PDF file
	 * @return PdfViewer - object that allows the user opening the PDF file
	 */
	private PdfViewer openPDFfile(FileListComponent resource) {
		var pdfViewer = new PdfViewer();
		pdfViewer.setAddDownloadButton(!pdfViewer.isAddDownloadButton());
		var streamResource = getStreamResource(resource);
		if(streamResource == null) {
			new CustomNotification("No se ha podido obtener el archivo seleccionado", NotificationVariant.LUMO_ERROR);
			return null;
		}
		var pdfResource = new StreamResource(resource.getName() + getFileType(resource.getType()), 
				() -> streamResource);
		pdfViewer.setSrc(pdfResource);
		pdfViewer.openThumbnailsView();
		return pdfViewer;
	}
	
	/**
	 * Returns a FileInputStream object that allows to read a file
	 * 
	 * @param resource - resource to read
	 * @return FileInputStream - readable object
	 */
	private FileInputStream getStreamResource(FileListComponent resource) {
		var storedPath = (fileCategory.equals(Constants.MEDIA_TAG)) ? Constants.STORED_MEDIA_PATH : Constants.STORED_FILE_PATH;
		try {
			var file = new File(storedPath + "\\" + resource.getName() + getFileType(resource.getType()));
			return new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Returns an object that allows the user opening an image file
	 * 
	 * @param resource - resource that points to a image file
	 * @return VerticalLayout - layout that includes the image file
	 */
	private VerticalLayout openImageFile(FileListComponent resource) {
		var verticalLayout = new VerticalLayout();
		verticalLayout.setAlignItems(Alignment.CENTER);
		verticalLayout.setWidthFull();
		var imageResource = getStreamResource(resource);
		var imageViewer = new StreamResource(resource.getName() + getFileType(resource.getType()), 
				() -> imageResource);
		var image = new Image(imageViewer, resource.getName());
		verticalLayout.add(image);
		return verticalLayout;
	}
	
	/**
	 * Returns an object that allows the user opening a video file
	 * 
	 * @param resource - video to display
	 * @return VerticalLayout - layout that includes the video file
	 */
	private VerticalLayout openVideoFile(FileListComponent resource) {
		var storedPath = (fileCategory.equals(Constants.MEDIA_TAG)) ? Constants.STORED_MEDIA_PATH : Constants.STORED_FILE_PATH;
		var verticalLayout = new VerticalLayout();
		verticalLayout.setAlignItems(Alignment.CENTER);
		verticalLayout.setWidthFull();
		var videoFile = new File(storedPath + "\\" + resource.getName() + getFileType(resource.getType()));
		var video = new VideoJS(UI.getCurrent().getSession(), videoFile, null);
		verticalLayout.add(video);
		return verticalLayout;
	}
	
	/**
	 * Gets a representative string that allows the user to distinguish the file type
	 * 
	 * @param type - file type
	 * @return String - representative string of the file
	 */
	private String getFileTypeName(String type) {
		if(type.equals("pdf")) {
			return "Documento PDF";
		} else if(type.equals("mp4")) {
			return "Vídeo MP4";
		} else if(type.equals("avi")) {
			return "Vídeo AVI";
		} else if(type.equals("jpg") || type.equals("png")) {
			return "Imagen JPEG";
		} else if(type.equals("png")) {
			return "Imagen PNG";
		}
		return new String("Archivo no definido");
	}
	
	/**
	 * Gets the type of file
	 * 
	 * @param type - string that allows to know what type of file is
	 * @return String - type of file
	 */
	private String getFileType(String type) {
		switch(type) {
			case "Documento PDF":
				return ".pdf";
			case "Vídeo MP4":
				return ".mp4";
			case "Vídeo AVI":
				return ".mp4";
			case "Imagen JPEG":
				return ".jpg";
			default:
				return ".png";
		}
	}
	
	//HTTP REQUESTS
	
	/**
	 * Sends an http request to get a File JSON object and to modify its name
	 * 
	 * @param resource - selected resource
	 * @param newValue - new file's name
	 * @return String - file JSON object
	 */
	private String sendGetFileRequest(FileListComponent resource, String newValue) {
		var getUrl = (fileCategory.equals(Constants.MEDIA_TAG)) ? Constants.MEDIA_REQ + "/media?mediaCode=" 
				: Constants.RES_REQ + "/resource?resourceCode=";
		var httpRequest = new HttpRequest(getUrl + resource.getFileCode());
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		var fileJSON = httpRequest.executeHttpGet(authToken);
		var jsonObject = new JSONObject(fileJSON);
		jsonObject.put("name", newValue);
		return jsonObject.toString();
	}
	
	/**
	 * Sends an http request to update the file info
	 * 
	 * @param requestBody - file JSON object
	 * @return boolean - true if it has been updated, false if not
	 */
	private boolean sendUpdateFileNameRequest(String requestBody) {
		var getUrl = (fileCategory.equals(Constants.MEDIA_TAG)) ? Constants.MEDIA_REQ : Constants.RES_REQ;
		var httpRequest = new HttpRequest(getUrl);
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		return httpRequest.executeHttpPut(authToken, requestBody);
	}
}
