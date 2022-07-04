package no.systema.jservices.tvinn.expressfortolling2.dao;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {

	private String city = "";
	private String country = "";
	private String postcode = "";
	private String streetLine = "";
	private String number = "";
	private String poBox = "";
	
	
}
