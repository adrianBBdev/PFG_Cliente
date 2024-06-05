package com.abb.pfg.custom;

import com.vaadin.flow.component.select.Select;

/**
 * Custom Select component to select the number of items to show
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
public class CustomNumElementsSelect extends Select<Integer> {
	
	private static final long serialVersionUID = -3593102680160984252L;

	/**
	 * Default class constructor
	 * 
	 */
	public CustomNumElementsSelect() {
		setEmptySelectionAllowed(true);
		this.setMaxWidth("100px");
		this.setWidthFull();
		this.setLabel("Mostrar");
		setItems(5, 10, 20, 40);
	}
}
