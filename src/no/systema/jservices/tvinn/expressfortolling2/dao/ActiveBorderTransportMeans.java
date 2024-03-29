package no.systema.jservices.tvinn.expressfortolling2.dao;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActiveBorderTransportMeans {
	
	private String identificationNumber;
	private String typeOfIdentification;
	
	private String typeOfMeansOfTransport;
	private String nationalityCode;
	private String modeOfTransportCode;
	private String actualDateAndTimeOfDeparture;
	private String estimatedDateAndTimeOfDeparture;
	private String estimatedDateAndTimeOfArrival;
	
	private Operator operator;
	private List<Crew> crew;
	
	
	
	
	
	

}
