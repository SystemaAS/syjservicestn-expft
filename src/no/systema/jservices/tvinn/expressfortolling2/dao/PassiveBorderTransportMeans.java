package no.systema.jservices.tvinn.expressfortolling2.dao;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class PassiveBorderTransportMeans {

	private String identificationNumber = "";
	private String typeOfIdentification = "";
	private String typeOfMeansOfTransport = "";
	private String country = "";
	
	
	
}
