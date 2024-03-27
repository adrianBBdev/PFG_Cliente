package com.abb.pfg.frontend.components;

import java.util.Optional;

import com.abb.pfg.frontend.AllJobOffersView;
import com.abb.pfg.frontend.ChatView;
import com.abb.pfg.frontend.CreateRequestView;
import com.abb.pfg.frontend.commons.Constants;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.component.tabs.Tabs.Orientation;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;

public class MainLayout extends AppLayout {

	private static final String OFFERS_TAB = "Ver ofertas";
	private static final String REQUESTS_TAB = "Mis solicitudes";
	private static final String CHATS_TAB = "Mis conversaciones";
	private static final String PROFILE_TAB = "Mi perfil";
	private static final String SIGNOUT_TAG = "Cerrar sesión";
	
	private VerticalLayout menuLayout, signOutLayout, titleLayout, drawerLayout; 
	private HorizontalLayout headerLayout;
	private DrawerToggle drawerToggle;
	private Tabs tabs;
	private Button signOutButton;
	private HorizontalLayout buttonLayout;
	
	public MainLayout() {
		setPrimarySection(Section.DRAWER);
		addToNavbar(true, createHeaderContent());
		addToDrawer(createDrawerContent(setTabs()));
	}
	
	private Component createHeaderContent() {
		drawerToggle = new DrawerToggle();
		headerLayout = new HorizontalLayout();
		headerLayout.setWidthFull();
		headerLayout.setSpacing(false);
		titleLayout = new VerticalLayout();
		titleLayout.add(new H2(Constants.APP_NAME));
		drawerLayout = new VerticalLayout();
		drawerLayout.add(drawerToggle);
		drawerLayout.setWidth("100px");
		this.setSignOutButton();
		headerLayout.add(drawerLayout, titleLayout, signOutLayout);
		return headerLayout;
	}
	
	private Component createDrawerContent(Tabs tabs) {
		menuLayout = new VerticalLayout();
		menuLayout.setSizeFull();
		menuLayout.setPadding(false);
	    menuLayout.setSpacing(false);
	    menuLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
	    menuLayout.add(tabs);
		return menuLayout;
	}
	
	private void setSignOutButton() {
		signOutButton = new Button(SIGNOUT_TAG);
		signOutButton.addClickListener(signOutEvent -> 
				signOutButton.getUI().ifPresent(ui -> ui.navigate(Constants.LOGIN_PATH)));
		signOutLayout = new VerticalLayout();
		signOutLayout.setWidth(Constants.WIDTH+100);
		signOutLayout.add(signOutButton);
		signOutLayout.setAlignItems(FlexComponent.Alignment.END);
	}
	
	private Tabs setTabs() {
		tabs = new Tabs();
		tabs.add(createTab(OFFERS_TAB, AllJobOffersView.class, VaadinIcon.LIST_OL.create()),
				createTab(REQUESTS_TAB, CreateRequestView.class, VaadinIcon.INBOX.create()),
				createTab(CHATS_TAB, ChatView.class, VaadinIcon.CHAT.create()));
//				createTab(PROFILE_TAB, StudentView.class));
//				VaadinIcon.EXCLAMATION_CIRCLE.create() --> AYUDA
//				VaadinIcon.CLIPBOARD_USER.create() --> PERFIL
//				VaadinIcon.CLIPBOARD_TEXT.create() --> DOCUMENTOS DE INTERES
		tabs.setOrientation(Orientation.VERTICAL);
		return tabs;
	}
	
	private static Tab createTab(String text,
	        Class<? extends Component> navigationTarget, Icon icon) {
	    final Tab tab = new Tab();
	    tab.add(icon, new RouterLink(text, navigationTarget));
	    ComponentUtil.setData(tab, Class.class, navigationTarget);
	    return tab;
	}
	
	private Optional<Tab> getTabForComponent(Component component) {
	    return tabs.getChildren()
	            .filter(tab -> ComponentUtil.getData(tab, Class.class)
	                    .equals(component.getClass()))
	            .findFirst().map(Tab.class::cast);
	}
	
	@Override
	protected void afterNavigation() {
	    super.afterNavigation();

	    // Select the tab corresponding to currently shown view
	    getTabForComponent(getContent()).ifPresent(tabs::setSelectedTab);
	}

}
