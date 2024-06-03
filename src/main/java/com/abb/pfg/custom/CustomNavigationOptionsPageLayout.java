package com.abb.pfg.custom;

import com.abb.pfg.utils.Constants;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Custom horizontal layout that contains the buttons to navigate between pages
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Data
@EqualsAndHashCode(callSuper=false)
public class CustomNavigationOptionsPageLayout extends HorizontalLayout{

	private static final long serialVersionUID = -3112875593122873684L;
	//Components
	private Button nextPageButton, prevPageButton;
	
	public CustomNavigationOptionsPageLayout(boolean isShowingFirst, boolean isShowingLast) {
		nextPageButton = new Button(Constants.NEXT_TAG, new Icon(VaadinIcon.ANGLE_DOUBLE_RIGHT));
		nextPageButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		nextPageButton.setIconAfterText(true);
		nextPageButton.setEnabled(!isShowingLast);
		prevPageButton = new Button(Constants.PREV_TAG, new Icon(VaadinIcon.ANGLE_DOUBLE_LEFT));
		prevPageButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
		prevPageButton.setEnabled(!isShowingFirst);
		this.add(nextPageButton, prevPageButton);
	}
	
	/**
	 * Sets the next page button enabled or disabled
	 * 
	 * @param isShowingLast - boolean that modify the button status
	 */
	public void setEnabledNextButton(boolean isShowingLast) {
		nextPageButton.setEnabled(!isShowingLast);
	}
	
	/**
	 * Sets the previous page button enabled or disabled
	 * 
	 * @param isShowingLast - boolean that modify the button status
	 */
	public void setEnabledPrevButton(boolean isShowingFirst) {
		prevPageButton.setEnabled(!isShowingFirst);
	}
}
