package com.abb.pfg.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents the request list component simplified
 *
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@AllArgsConstructor
@Data
public class UserListComponent {
	private String username;
	private String name;
	private String profilePicture;
}
