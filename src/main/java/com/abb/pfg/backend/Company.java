package com.abb.pfg.backend;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Class which represents the Company Role in the web app
 * 
 * @author Adrián Barco Barona
 * @version 1.0
 *
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper=false)
public class Company extends GenericUser{
	
	private String name;
	
	private String cif;
	
	private String country;
	
	/**
	 * Default class constructor
	 * 
	 * @param id - company's id
	 * @param email - comapny's user email
	 * @param password - company's user password
	 * @param profilePicture - comapany's user profile picture
	 * @param description - company's description
	 * @param name - company's name
	 * @param cif - company's cif
	 * @param country - company's origin country
	 */
	public Company(User user, String email, byte[] profilePicture, String description, 
			String name, String cif, String country) {
		super(user, email, profilePicture, description);
		this.name = name;
		this.cif = cif;
		this.country = country;
	}
	
	public String isEnable() {
		if(this.getUser().isEnable()) {
			return "Disponible";
		} else {
			return "Bloqueado";
		}
	}
}
