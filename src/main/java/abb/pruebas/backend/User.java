package abb.pruebas.backend;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Class which represents a user in the web app
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 * 
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User{

	private String username;
	
	private String password;
	
	private String role;
	
	private boolean enable;
}
