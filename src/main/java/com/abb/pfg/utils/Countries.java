package com.abb.pfg.utils;

/**
 * Countries enumeration
 *
 * @author Adrian Barco Barona
 * @version 1.0
 *
 */
public enum Countries {

	AUSTRALIA("Australia"),
	BRAZIL("Brasil"),
	CANADA("Canada"),
	CHILE("Chile"),
	CHINA("China"),
	FINLAND("Finlandia"),
	FRANCE("Francia"),
	GERMANY("Alemania"),
	JAPON("Japon"),
	INDIA("India"),
	IRLAND("Irlanda"),
	ISRAEL("Israel"),
	MALAYSIA("Malasia"),
	MEXICO("Mexico"),
	NETHERLANDS("Países Bajos"),
	NORWAY("Noruega"),
	PORTUGAL("Portugal"),
	RUSSIA("Rusia"),
	SOUTH_AFRICA("Sudafrica"),
	SOUTH_COREA("Corea del Sur"),
	SPAIN("España"),
	SWEEDEN("Suecia"),
	SWITZERLAND("Suiza"),
	TURKEY("Turquía"),
	UKRAINE("Ucrania"),
	UNITED_KINGDOM("Reino Unido"),
	UNITED_STATES("Estados Unidos");

	private final String countryName;

	Countries(String countryName) {
		this.countryName = countryName;
	}

	public String getCountryName() {
		return countryName;
	}
}
