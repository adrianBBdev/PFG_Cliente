/**
 *
 */
package com.abb.pfg.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Represents the job offer list component simplified
 *
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
@AllArgsConstructor
@Data
public class JobOfferListComponent {
	private Long offerCode;
	private Long favoriteCode;
	private String title;
	private String company;
	private String area;
	private String city;
	private String status;
	private Integer numRequests;
}
