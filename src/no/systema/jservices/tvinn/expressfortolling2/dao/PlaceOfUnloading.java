package no.systema.jservices.tvinn.expressfortolling2.dao;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PlaceOfUnloading {

	private String location = "";
	private String unloCode = "";
	private AddressCountry address;
	
	
	
}
