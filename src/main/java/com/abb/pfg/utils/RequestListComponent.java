package com.abb.pfg.utils;

import java.time.LocalDate;

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
public class RequestListComponent {
	private Long requestCode;
	private String title;
	private String companyName;
	private LocalDate requestDate;
	private String requestStatus;
	private String studentName;
	private String studentUsername;
}
