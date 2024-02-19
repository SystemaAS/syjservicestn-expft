package no.systema.jservices.tvinn.digitoll.external.house.dao;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CustomsOfficeOfFirstEntry {
	private String referenceNumber;
	private String estimatedDateAndTimeOfArrival;
	
	
}
