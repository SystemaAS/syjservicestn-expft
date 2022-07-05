package no.systema.jservices.tvinn.expressfortolling2.dao;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GoodsMeasure {
	
	private Double netMass = 0.00;
	private Double grossMass = 0.00;
	private String supplementaryUnits = "";
	

}
