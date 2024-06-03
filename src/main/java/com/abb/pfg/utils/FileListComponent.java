package com.abb.pfg.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Custom list component which represents a resource
 * 
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@Data
@AllArgsConstructor
public class FileListComponent {
	private Long fileCode;
	private String name;
	private String type;
}
