package com.abb.pfg.backend;


import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class which represents the Multimedia Content of each user in the web app
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Data
@NoArgsConstructor
public class Multimedia {
	private Long id;
	
	private String name;
	
	private GenericUser genericUser;
	
	private byte[] file;

	/**
	 * Default class constructor
	 * 
	 * @param name - multimedia's name file
	 * @param user - multimedia's owner
	 * @param file - multimedia's file
	 */
	public Multimedia(String name, GenericUser genericUser, byte[] file) {
		this.name = name;
		this.genericUser = genericUser;
		this.file = file;
	} 
}
