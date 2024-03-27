package com.abb.pfg.backend;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Class which represents the Administrator Role in the web app
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Data
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class Administrator extends GenericUser{
	
	/**
	 * Default class constructor
	 *
	 * @param id - admin's id
	 * @param email - admin's email
	 * @param profilePicture - admin's profile picture
	 * @param description - admin's description
	 */
	public Administrator(User user, String email, byte[] profilePicture, String description) {
		super(user, email, profilePicture, description);
	}
	
	public String isEnable() {
		if(this.getUser().isEnable()) {
			return "Disponible";
		} else {
			return "Bloqueado";
		}
	}
	
	public String getUsername() {
		return this.getUser().getUsername();
	}
}
