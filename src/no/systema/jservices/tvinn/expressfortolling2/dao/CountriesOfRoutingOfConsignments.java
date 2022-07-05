package no.systema.jservices.tvinn.expressfortolling2.dao;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CountriesOfRoutingOfConsignments {
	
	private Integer sequenceNumber = 0;
	private String country = "";
	

}
