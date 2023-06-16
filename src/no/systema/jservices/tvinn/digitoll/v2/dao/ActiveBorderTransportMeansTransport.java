package no.systema.jservices.tvinn.digitoll.v2.dao;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;


/**
 * 
 * @author oscardelatorre
 * Jun 2023
 * Only V2
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ActiveBorderTransportMeansTransport {
	
	//Mandatory
	private String identificationNumber;
	//Mandatory
	private String typeOfIdentification;
	//Mandatory
	private String typeOfMeansOfTransport;
	//Mandatory
	private String conveyanceReferenceNumber;
	//Mandatory
	private String countryCode;
	//Mandatory
	private String modeOfTransportCode;
	//Mandatory
	private Operator operator;
	

}
