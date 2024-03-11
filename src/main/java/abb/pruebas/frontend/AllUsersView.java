package abb.pruebas.frontend;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

import abb.pruebas.backend.Administrator;
import abb.pruebas.backend.Company;
import abb.pruebas.backend.Student;
import abb.pruebas.backend.User;
import abb.pruebas.frontend.commons.Constants;
import abb.pruebas.frontend.components.MainLayout;

/**
 * Class which represents the list of all users for the admin role
 * 
 * @author Adrián Barco Barona
 * @version
 *
 */
@Route("/allUsers")
@PageTitle("J4S - Todos los usuarios")
public class AllUsersView extends MainLayout{
	
	private static final String HEADER_TITLE = "Lista de todos los usuarios";
	private static final String ADMIN_TAG = "Administradores";
	private static final String COMPANY_TAG = "Empresas";
	private static final String STUDENT_TAG = "Estudiantes";
	
	private VerticalLayout mainLayout, contentLayout, tabLayout;
	private Grid<Administrator> adminGrid;
	private Grid<Company> companyGrid;
	private Grid<Student> studentGrid;
	private Tabs userTabs;
	private Tab adminTab, companyTab, studentTab;
	
	public AllUsersView () {
		init();
		contentLayout.add(new H1(HEADER_TITLE), mainLayout);
		contentLayout.setAlignItems(Alignment.CENTER);
		setContent(contentLayout);
	}
	
	private void init() {
		createTabs();
		contentLayout = new VerticalLayout();
		mainLayout = new VerticalLayout();
		mainLayout.setWidth(Constants.WIDTH);
		mainLayout.setAlignItems(Alignment.CENTER);
		mainLayout.add(userTabs);
	}
	
	private void createTabs() {
		this.createGrids();
		tabLayout = new VerticalLayout();
		tabLayout.setVisible(false);
		adminTab = new Tab(ADMIN_TAG);
		companyTab = new Tab(COMPANY_TAG);
		studentTab = new Tab(STUDENT_TAG);
		userTabs = new Tabs(adminTab, companyTab, studentTab);
		userTabs.setAutoselect(false);	
		userTabs.addSelectedChangeListener(event -> {
			if(tabLayout.isVisible()) {
				mainLayout.remove(tabLayout);
			} else {
				tabLayout.setVisible(true);
			}
			this.buildTab();
		});
	}
	
	private void buildTab() {
		tabLayout = new VerticalLayout();
		if(userTabs.getSelectedTab().getLabel().equals(ADMIN_TAG)) {
			tabLayout.add(new H2("Lista de administradores"), adminGrid);
		} else if(userTabs.getSelectedTab().getLabel().equals(COMPANY_TAG)) {
			tabLayout.add(new H2("Lista de empresas"), companyGrid);
		} else if(userTabs.getSelectedTab().getLabel().equals(STUDENT_TAG)) {
			tabLayout.add(new H2("Lista de estudiantes"), studentGrid);
		}
		tabLayout.setAlignItems(Alignment.CENTER);
		mainLayout.add(tabLayout);
	}
	
	private void createGrids() {
		adminGrid = new Grid<>(Administrator.class, false);
		adminGrid.addColumn(Administrator::getUsername).setHeader(Constants.NAME_TAG);
		adminGrid.addColumn(Administrator::getEmail).setHeader(Constants.EMAIL_TAG);
		adminGrid.addColumn(Administrator::isEnable).setHeader(Constants.STATUS_TAG);
		adminGrid.addItemClickListener(adminClickEvent -> 
				adminGrid.getUI().ifPresent(ui -> ui.navigate(Constants.ADMIN_PATH
						+ adminClickEvent.getItem().getUsername())));
		companyGrid = new Grid<>(Company.class, false);
		companyGrid.addColumn(Company::getName).setHeader(Constants.NAME_TAG);
		companyGrid.addColumn(Company::getEmail).setHeader(Constants.EMAIL_TAG);
		companyGrid.addColumn(Company::getCif).setHeader(Constants.CIF_TAG);
		companyGrid.addColumn(Company::isEnable).setHeader(Constants.STATUS_TAG);
		companyGrid.addItemClickListener(companyClickEvent ->
			companyGrid.getUI().ifPresent(ui -> ui.navigate(Constants.COMPANY_PATH 
					+ companyClickEvent.getItem().getName()))
		);
		studentGrid = new Grid<>(Student.class, false);
		studentGrid.addColumn(Student::getName).setHeader(Constants.NAME_TAG);
		studentGrid.addColumn(Student::getEmail).setHeader(Constants.EMAIL_TAG);
		studentGrid.addColumn(Student::getDni).setHeader(Constants.DNI_TAG);
		studentGrid.addColumn(Student::isEnable).setHeader(Constants.STATUS_TAG);
		studentGrid.addItemClickListener(studentClickEvent -> 	
			studentGrid.getUI().ifPresent(ui -> ui.navigate(Constants.STUDENT_PATH
					+ studentClickEvent.getItem().getName())));
		this.insertInitialData();
	}
	
	private void insertInitialData() {
		User user3 = new User("admin", "admin", "ROLE_ADMIN", true);
		Administrator admin1 = new Administrator(user3, "admin@gmail.com", null, "Administrador de la plataforma");
		adminGrid.setItems(admin1);
		User user1 = new User("cristian.molina", "caja", "ROLE_ESTUDIANTE", true);
		Student student1 = new Student(user1, "cristian.molina@gmail.com", null, "Estudiante", "Cristian Molina", "50998457H", "Teleco");
		studentGrid.setItems(student1);
		User user2 = new User("inetum", "windows", "ROLE_EMPRESA", true);
		Company company1 = new Company(user2, "inetum@inetum.com", null, "Empresa tecnologica", "Inetum España, S.A", "A25178965", "Francia");
		companyGrid.setItems(company1);
	}
	

}
