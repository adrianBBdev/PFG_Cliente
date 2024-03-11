package abb.pruebas.backend;


import abb.pruebas.backend.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class which represents the generic user in the web app
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GenericUser {

	private User user;
	
	private String email;
	
	private byte[] profilePicture;
	
	private String description;
}
