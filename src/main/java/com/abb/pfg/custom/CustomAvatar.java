package com.abb.pfg.custom;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import com.abb.pfg.utils.Constants;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.server.AbstractStreamResource;
import com.vaadin.flow.server.StreamResource;

/**
 * Custom avatar to show the profile picture
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
public class CustomAvatar extends Avatar {

	private static final long serialVersionUID = 5254921588880543689L;
	private String picture, storedPath;
	
	/**
	 * Default class constructor
	 * 
	 * @param picture - profile picture's file name
	 */
	public CustomAvatar(String picture) {
		storedPath = Constants.STORED_PIC_PATH;
		this.picture = picture;
		createImageAvatar();
	}
	
	/**
	 * Creates the avatar from the resource
	 * 
	 */
	private void createImageAvatar() {
		var imageResource = new StreamResource(picture, () -> getResource());
	    setImageResource(imageResource);
	}
	
	/**
	 * Gets the resource to create the avatar
	 * 
	 * @return FileInputStream - the resource needed
	 */
	private FileInputStream getResource() {
		try {
    		return new FileInputStream(new File(storedPath + "\\" + picture));
        } catch (FileNotFoundException e) {
            return null;
        }
	}
	
	/**
	 * Gets the resource to create the avatar
	 * 
	 * @return AbstractStreamResource - the resource needed
	 */
	public AbstractStreamResource getImageResource() {
		return new StreamResource(picture, () -> getResource());
	}
}
