package no.systema.jservices.tvinn.expressfortolling2.dao;


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
public class HouseConsignment {
	
	//Mandatory*
	private String documentIssueDate;
	//Mandatory*
	private Declarant declarant;
	//Mandatory*
	private Representative representative;
	//Mandatory*
	@JsonProperty("consignmentHouseLevel")
	private HouseConsignmentConsignmentHouseLevel houseConsignmentConsignmentHouseLevel; 
	
	
}
