package no.systema.jservices.tvinn.expressfortolling2.dao;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PassiveTransportMeans {

	private String identificationNumber = "";
	private Integer typeOfIdentification = 0;
	private String typeOfMeansOfTransport = "";
	private String countryCode = "";
	
	
	
}
