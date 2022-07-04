package no.systema.jservices.tvinn.expressfortolling2.dao;


import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
//@JsonPropertyOrder(alphabetic = true)
public class MasterConsignment {

	private String documentIssueDate = "";
	private ActiveBorderTransportMeans activeBorderTransportMeans;
	private CustomsOfficeOfFirstEntry customsOfficeOfFirstEntry;
	private ConsignmentMasterLevel consignmentMasterLevel;
	
	private Declarant declarant;
	private List<ReleasedConfirmation> releasedConfirmation;
	
	
}
