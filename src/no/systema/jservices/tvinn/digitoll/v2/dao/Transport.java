package no.systema.jservices.tvinn.digitoll.v2.dao;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonPropertyOrder(alphabetic = true)
public class Transport {
	
	//Mandatory*
	private String documentIssueDate;
	
	//Optional
	private Representative representative;
	
	//Mandatory*
	@JsonProperty("activeBorderTransportMeans")
	private ActiveBorderTransportMeansTransport activeBorderTransportMeans;
	
	//Mandatory*
	private Carrier carrier;
	
	//Mandatory*
	private CustomsOfficeOfFirstEntry customsOfficeOfFirstEntry;
	
	//Optional
	private String estimatedDateAndTimeOfArrival;
	
	//Optional
	private String scheduledDateAndTimeOfArrival;
	
	//Mandatory*
	//TODO --> private List<ConsignmentMasterLevel> consignmentMasterLevel;
	
	
	
}
