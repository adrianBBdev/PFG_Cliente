package com.abb.pfg.custom;

import org.json.JSONObject;

import com.abb.pfg.utils.Constants;
import com.abb.pfg.utils.HttpRequest;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.sidenav.SideNav;
import com.vaadin.flow.component.sidenav.SideNavItem;
import com.vaadin.flow.server.VaadinSession;

/**
 * Custom Side Navigation to show all possible optins of the web app
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
public class CustomSideNav extends SideNav{
	
	private static final long serialVersionUID = 3663947315965552005L;
	//Etiquetas
	private static final String OFF_TAB = "Buscar ofertas";
	private static final String MY_OFF_TAB = "Mis ofertas";
	private static final String OFF_SV_TAB = "Ofertas guardadas";
	private static final String REQ_TAB = "Mis solicitudes";
	private static final String CHT_TAB = "Mis chats";
	private static final String PRF_TAB = "Mi perfil";
	private static final String CR_OFF_TAB = "Crear nueva oferta";
	private static final String NO_ACT_LABEL = "No hay acciones disponibles";
	//Atributos
	private String userRole, username;
	
	/**
	 * Default class constructor
	 * 
	 * @param userRole - user's role
	 */
	public CustomSideNav(String username, String userRole) {
		this.username = username;
		this.userRole = userRole;
		init();
	}
	
	/**
	 * Initizalices all components
	 * 
	 */
	private void init() {
		switch(userRole) {
			case Constants.STD_ROLE:
				var stPendingRequests = sendGetPendingRequests(username, userRole);
				getStudentOrCompanySideNavItems(userRole, stPendingRequests);
				break;
			case Constants.CMP_ROLE:
				var cmPendingRequests = sendGetPendingRequests(username, userRole);
				getStudentOrCompanySideNavItems(userRole, cmPendingRequests);
				break;
			case Constants.ADM_ROLE:
				getAdminSideNavItems();
				break;
			default:
				noActionsSideNav();
		}
	}
	
	/**
	 * Gets the side navigation items depending on the user logged in
	 * 
	 * @param userRole - user's role
	 */
	private void getStudentOrCompanySideNavItems(String userRole, Integer pendingRequests) {
		var sideNavItemOff = new SideNavItem(OFF_TAB, Constants.OFFERS_PATH, VaadinIcon.SEARCH.create());
		var sideNavItemReq = new SideNavItem(REQ_TAB, Constants.REQ_PATH, VaadinIcon.ENVELOPE_OPEN.create());
		if(pendingRequests > 0) {
			var requestsCounter = new Span(String.valueOf(pendingRequests));
			requestsCounter.getElement().getThemeList().add("badge contrast pill");
			sideNavItemReq.setSuffixComponent(requestsCounter);
		}
		var sideNavItemCht = new SideNavItem(CHT_TAB, Constants.CHT_PATH, VaadinIcon.CHAT.create());
		var sideNavItemPrf = new SideNavItem(PRF_TAB, Constants.PROFILE_PATH, VaadinIcon.USER.create());
		if(userRole.equals(Constants.STD_ROLE)) {
			this.addItem(sideNavItemOff, 
						new SideNavItem(OFF_SV_TAB, Constants.SV_OFFERS_PATH, VaadinIcon.BOOKMARK.create()),
						sideNavItemReq, sideNavItemCht, sideNavItemPrf);
		} else {
			sideNavItemOff.setLabel(MY_OFF_TAB);
			this.addItem(sideNavItemOff, 
						new SideNavItem(CR_OFF_TAB, Constants.CREATE_OFF_PATH, VaadinIcon.CLIPBOARD_CROSS.create()), 
						sideNavItemReq, sideNavItemCht, sideNavItemPrf);
		}
	}
	
	/**
	 * Gets the side navigation items if the user is an admin
	 * 
	 */
	private void getAdminSideNavItems() {
		var sideNavItemUsr = new SideNavItem("Gestionar usuarios", Constants.MNG_USERS_PATH, VaadinIcon.USERS.create());
		var sideNavItemOff = new SideNavItem("Gestionar ofertas", Constants.OFFERS_PATH, VaadinIcon.LINES_LIST.create());
		var sideNavItemReq = new SideNavItem("Gestionar solicitudes", Constants.REQ_PATH, VaadinIcon.ENVELOPES.create());
		var sideNavItemCht = new SideNavItem("Gestionar chats", Constants.CHT_PATH, VaadinIcon.CHAT.create());
		var sideNavItemAre = new SideNavItem("Gestionar áreas", Constants.AREAS_PATH, VaadinIcon.ARCHIVE.create());
		var sideNavItemPrf = new SideNavItem(PRF_TAB, Constants.PROFILE_PATH, VaadinIcon.USER.create());
		this.addItem(sideNavItemUsr, sideNavItemOff, sideNavItemReq, sideNavItemCht, sideNavItemAre, sideNavItemPrf);
	}
	
	/**
	 * Gets the side navigation bar items if the user is a guest
	 * 
	 */
	private void noActionsSideNav() {
		this.setLabel(NO_ACT_LABEL);
	}
	
	private Integer sendGetPendingRequests(String username, String userRole) {
		var httpRequest = new HttpRequest(Constants.REQ_REQ + "?requestStatus=" + Constants.PDG_TAG);
		if(userRole.equals(Constants.STD_ROLE)) {
			httpRequest.setUrl(httpRequest.getUrl() + "&userId=" + username);
		} else if(userRole.equals(Constants.CMP_ROLE)) {
			httpRequest.setUrl(httpRequest.getUrl() + "&userId=" + username);
		}
		var authToken = (String) VaadinSession.getCurrent().getAttribute("authToken");
		var responseBody = httpRequest.executeHttpGet(authToken);
		var jsonObject = new JSONObject(responseBody);
		var pendingRequests = jsonObject.getInt("totalElements");
		return pendingRequests;
	}
}
