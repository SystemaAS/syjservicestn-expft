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
//we need this line in order to follow the order as in the properties. For some reason the activeBorderTransportMeans is placed at the end because of the JsonProperty that fucks up the order
@JsonPropertyOrder({"documentIssueDate", "representative", "activeBorderTransportMeans"})
public class Transport {
	
	//Mandatory*
	@JsonProperty("documentIssueDate")
	private String documentIssueDate;
	
	//Optional
	@JsonProperty("representative")
	private Representative representative;
	
	//Mandatory*
	@JsonProperty("activeBorderTransportMeans")
	private ActiveBorderTransportMeansTransport activeBorderTransportMeansTransport;
	
	//Mandatory*
	private Carrier carrier;
	
	//Mandatory*
	private CustomsOfficeOfFirstEntry customsOfficeOfFirstEntry;
	
	//Optional
	private String estimatedDateAndTimeOfArrival;
	
	//Optional
	private String scheduledDateAndTimeOfArrival;
	
	//Mandatory*
	@JsonProperty("consignmentMasterLevel")
	private List<ConsignmentMasterLevelTransport> consignmentMasterLevelTransport;
	
	
	
}
