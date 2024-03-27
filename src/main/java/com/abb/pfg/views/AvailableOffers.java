package com.abb.pfg.views;

import com.vaadin.flow.component.Composite;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;

/**
 * Default view when user access to the app. Shows a list of job offers
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Route("/availableJobOffers")
@PageTitle("J4S - Ofertas disponibles")
public class AvailableOffers extends Composite<VerticalLayout> implements HasUrlParameter<String>{
	
	private static final long serialVersionUID = -6549352517346464385L;
	//Etiquetas o títulos
	private static final String HEADER_TAG = "Ofertas disponibles";
	//Elementos
	private VerticalLayout mainLayout;
	
	/**
	 * Default view class constructor
	 * 
	 */
	public AvailableOffers() {
		init();		//Inicializamos la vista y añadimos el layout principal
		getContent().add(new H1(HEADER_TAG), mainLayout);
		getContent().setAlignItems(Alignment.CENTER);
	}
	
	@Override
	public void setParameter(BeforeEvent event, String parameter) {
		var token = event.getLocation().getFirstSegment();
	}
	
	private void init() {
		mainLayout = new VerticalLayout();		//Inicializamos el layout principal
		mainLayout.setAlignItems(Alignment.CENTER);
		mainLayout.setWidthFull();
	}

	
}
