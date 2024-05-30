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
@JsonPropertyOrder({"requestId", "customsOfficeOfPresentation", "mrn"})
public class Ics2Ens {
	
	//Mandatory*
	private String requestId;
	
	//Mandatory*
	private String customsOfficeOfPresentation;
	
	//Mandatory*
	private String mrn;
		
	
	
	
}
