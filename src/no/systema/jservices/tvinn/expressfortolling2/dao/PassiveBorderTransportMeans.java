package no.systema.jservices.tvinn.expressfortolling2.dao;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PassiveBorderTransportMeans {

	private String identificationNumber;
	private Integer typeOfIdentification = 0;
	private String typeOfMeansOfTransport;
	private String country;
	
	
	
}
