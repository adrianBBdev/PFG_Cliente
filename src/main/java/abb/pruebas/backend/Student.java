package abb.pruebas.backend;


import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Class which represents the student rol in the web app
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper=false)
public class Student extends GenericUser{
	
	private String name;

	private String dni;
	
	private String studies;
	
	/**
	 * Default class constructor
	 * 
	 * @param id - student's id
	 * @param email - student's user email
	 * @param profilePicture - student's user profile picture
	 * @param description - stduent's personal description
	 * @param name - student's name
	 * @param surnames - student's surnames
	 * @param dni - students's dni
	 * @param studies - degree that the student has completed or is finishing
	 */
	public Student(User user, String email, byte[] profilePicture, String description, 
			String name, String dni, String studies) {
		super(user, email, profilePicture, description);
		this.name = name;
		this.dni = dni;
		this.studies = studies;
	}
	
	public String isEnable() {
		if(this.getUser().isEnable()) {
			return "Disponible";
		} else {
			return "Bloqueado";
		}
	}
}
