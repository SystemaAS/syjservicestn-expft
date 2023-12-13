package no.systema.jservices.tvinn.digitoll.external.house.dao;

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
	//Mandatory*
	private String messageType;
	private String version;
	private String messageNumber;
	private String messageIssueDate;
	private String documentID;
	//Message parties
	private Sender sender;
	private Receiver receiver;
	//Merchand parties
	private Consignor consignor;
	private Consignee consignee;
	
	
}
