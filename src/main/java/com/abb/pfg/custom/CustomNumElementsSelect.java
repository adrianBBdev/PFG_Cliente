/**
 * 
 */
package com.abb.pfg.custom;

import com.abb.pfg.utils.Constants;
import com.vaadin.flow.component.select.Select;

/**
 * Custom display select to select the number of items to show
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
public class CustomNumElementsSelect extends Select<Integer> {
	
	private static final long serialVersionUID = -3593102680160984252L;

	public CustomNumElementsSelect() {
		setEmptySelectionAllowed(true);
		setLabel(Constants.ITEMS_PER_PAGE_TAG);
		setItems(5, 10, 20, 40);
	}
}
