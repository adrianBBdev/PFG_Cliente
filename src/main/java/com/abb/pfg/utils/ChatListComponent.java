package com.abb.pfg.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents the chat list component simplified
 *
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@AllArgsConstructor
@Data
public class ChatListComponent {
	
	private Long chatCode;
	private String studentName;
	private String studentPicture;
	private String companyName;
	private String companyLogo;
}
