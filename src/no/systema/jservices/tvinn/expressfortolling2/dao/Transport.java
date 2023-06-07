package no.systema.jservices.tvinn.expressfortolling2.dao;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;


/**
 * 
 * @author oscardelatorre
 * June 2023
 * 
 * Only V2
 * 
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Transport {
	//Mandatory*
	private String documentIssueDate;
	//Optional
	private Representative representative;
	
	//Mandatory
	@JsonProperty("activeBorderTransportMeans")
	private ActiveBorderTransportMeansTransport activeBorderTransportMeansTransport;
	
	//Mandatory
	private Carrier carrier;
	
	//Mandatory
	private CustomsOfficeOfFirstEntry customsOfficeOfFirstEntry;
	
	//Optional
	private String estimatedDateAndTimeOfArrival;
	//Optional
	private String scheduledDateAndTimeOfArrival;
	
	//Optional (only one since SYSPED does not support many ( a list )
	@JsonProperty("consignmentMasterLevel")
	private ConsignmentMasterLevelTransport consignmentMasterLevel;
		
		

}
