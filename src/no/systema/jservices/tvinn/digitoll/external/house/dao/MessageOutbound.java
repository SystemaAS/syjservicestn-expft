package no.systema.jservices.tvinn.digitoll.external.house.dao;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;


import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
//@JsonPropertyOrder(alphabetic = true)
//we need this line in order to follow the order as in the properties. 
//@JsonPropertyOrder({"documentIssueDate", "representative", "consignmentHouseLevel"})
public class MessageOutbound {
	@JsonIgnore
	private String uuid;
	
	//Mandatory*
	private String messageType;
	private String version;
	private String messageNumber;
	private String messageIssueDate;
	private String documentID;
	private String note;
	//Message parties
	private Sender sender;
	private Receiver receiver;
	//Customs office
	private CustomsOfficeOfFirstEntry customsOfficeOfFirstEntry;
	private String estimatedDateAndTimeOfArrival;
	private ActiveBorderTransportMeans activeBorderTransportMeans;
	
	private ConsignmentMasterLevel consignmentMasterLevel;
	//Merchand parties
	private Consignor consignor;
	private Consignee consignee;
	
	
}
