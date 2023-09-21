package no.systema.jservices.tvinn.digitoll.v2.dao;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Address {

	private String city;
	private String country;
	private String postcode;
	private String streetLine;
	private String streetAdditionalLine;
	private String number;
	private String poBox;
	
	
}
